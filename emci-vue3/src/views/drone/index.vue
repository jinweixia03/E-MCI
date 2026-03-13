<template>
  <div class="drone-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Aim /></el-icon>
        <div class="title-content">
          <h1>无人机管理</h1>
          <p>智能巡检路径规划与任务管理</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="dronePositionChanged && selectedDrone"
          type="warning"
          size="large"
          @click="resetDronePosition"
        >
          <el-icon><RefreshLeft /></el-icon>重置位置
        </el-button>
        <el-button size="large" @click="showTaskList = true">
          <el-icon><List /></el-icon>任务列表
        </el-button>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <!-- 左侧无人机列表 -->
      <div class="drone-list-section">
        <h3 class="section-title">
          <el-icon><Aim /></el-icon>
          无人机列表
        </h3>
        <div v-loading="loading" class="drone-list">
          <div
            v-for="drone in droneList"
            :key="drone.id"
            class="drone-card"
            :class="{ active: selectedDrone?.id === drone.id, inspecting: drone.status === 1 }"
            @click="selectDrone(drone)"
          >
            <div class="drone-header">
              <el-tag :type="getDroneStatusType(drone.status)" size="small">
                {{ drone.statusText }}
              </el-tag>
              <span class="battery" :class="{ low: (drone.battery || 0) < 30 }">
                <el-icon><Lightning /></el-icon>{{ drone.battery || 0 }}%
              </span>
            </div>
            <div class="drone-body">
              <el-icon class="drone-icon"><Aim /></el-icon>
              <div class="drone-name">{{ drone.name }}</div>
              <div class="drone-info" v-if="drone.manholeCount">
                <span>覆盖 {{ drone.manholeCount }} 个井盖</span>
                <span>预计 {{ drone.estimatedTime }} 分钟</span>
              </div>
              <div class="drone-info" v-else>
                <span style="color: #909399">未部署检测区域</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间地图区域 -->
      <div class="map-section">
        <div id="drone-map" class="map-container"></div>

        <!-- 无人机信息面板 -->
        <div class="drone-panel" v-if="selectedDrone">
          <h4>{{ selectedDrone.name }}</h4>
          <div class="panel-item">
            <span>状态：</span>
            <el-tag :type="getDroneStatusType(selectedDrone.status)" size="small">
              {{ selectedDrone.statusText }}
            </el-tag>
          </div>
          <div class="panel-item">
            <span>电量：</span>{{ selectedDrone.battery }}%
          </div>

          <!-- 已部署时显示当前部署信息 -->
          <template v-if="selectedDrone.pathPoints?.length">
            <div class="panel-divider"></div>
            <div class="panel-section-title">当前部署</div>
            <div class="panel-item">
              <span>检测区域：</span>{{ selectedDrone.taskRadius }}米
            </div>
            <div class="panel-item">
              <span>覆盖井盖：</span>{{ selectedDrone.manholeCount }} 个
            </div>
            <div class="panel-item">
              <span>预计时间：</span>{{ selectedDrone.estimatedTime }} 分钟
            </div>
          </template>

          <!-- 部署设置 -->
          <div class="panel-section deploy-section">
            <div class="section-header">
              <el-icon><Position /></el-icon>
              <span>部署设置</span>
            </div>

            <div class="deploy-info">
              <div class="info-row">
                <span class="info-label">位置坐标</span>
                <span class="info-value coords">{{ (pendingPosition?.lat ?? selectedDrone.latitude)?.toFixed(6) }}, {{ (pendingPosition?.lng ?? selectedDrone.longitude)?.toFixed(6) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">巡检半径</span>
                <span class="info-value radius">{{ deployRadius }} 米</span>
              </div>
            </div>

            <div class="radius-slider">
              <el-slider v-model="deployRadius" :min="100" :max="2000" :step="100" show-stops @input="onRadiusChange" />
            </div>

            <div class="deploy-actions">
              <el-button type="primary" @click="previewDeployPath" :loading="previewLoading">
                <el-icon><View /></el-icon>预览路径
              </el-button>
              <el-button type="success" @click="confirmDeploy" :loading="deployLoading" :disabled="!previewResult">
                <el-icon><Check /></el-icon>确认部署
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 任务列表抽屉 -->
    <el-drawer
      v-model="showTaskList"
      title="无人机检测任务列表"
      size="800px"
    >
      <el-table :data="taskList" v-loading="taskLoading" style="width: 100%">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="droneName" label="无人机" min-width="120" />
        <el-table-column label="检测区域" width="120">
          <template #default="{ row }">{{ row.radius }}米</template>
        </el-table-column>
        <el-table-column label="覆盖井盖" width="90">
          <template #default="{ row }">{{ row.manholeCount }}个</template>
        </el-table-column>
        <el-table-column label="预计时间" width="90">
          <template #default="{ row }">{{ row.estimatedTime }}分钟</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="info"
              link
              @click="showTaskOnMap(row)"
            >查看路径</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { droneApi } from '@/api/drone'
import { manholeApi } from '@/api/manhole'
import type { Drone, DroneTask, PathPlanResult } from '@/types/drone'
import type { ManholeMapVO } from '@/types/manhole'
import AMapLoader from '@amap/amap-jsapi-loader'
import { Aim, RefreshLeft, VideoPlay, CircleCheck, Lightning, List, Position, View, Check } from '@element-plus/icons-vue'

// ==================== 状态定义 ====================
const loading = ref(false)
const droneList = ref<Drone[]>([])
const selectedDrone = ref<Drone | null>(null)
const deployLoading = ref(false)
const actionLoading = ref(false)

// 位置拖动相关
const dronePositionChanged = ref(false)
const pendingPosition = ref<{ lng: number, lat: number } | null>(null)
const originalPosition = ref<{ lng: number, lat: number } | null>(null)

// 部署相关
const deployRadius = ref(1000)
const previewLoading = ref(false)
const previewResult = ref<PathPlanResult | null>(null)

// 任务列表相关
const showTaskList = ref(false)
const taskList = ref<DroneTask[]>([])
const taskLoading = ref(false)

// 计算属性
const availableDrones = computed(() => droneList.value.filter(d => d.status === 0 && (d.battery || 0) >= 30))

// 地图相关
const AMap = ref<any>(null)
const map = ref<any>(null)
let circle: any = null
let pathLine: any = null
let droneMarker: any = null
let manholeMarkers: any[] = []
let pathMarkers: any[] = []

// ==================== 方法定义 ====================
const fetchDrones = async () => {
  loading.value = true
  try {
    const res = await droneApi.getList()
    droneList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取无人机列表失败')
  } finally {
    loading.value = false
  }
}

const selectDrone = async (drone: Drone) => {
  selectedDrone.value = drone
  dronePositionChanged.value = false
  pendingPosition.value = null
  originalPosition.value = null
  previewResult.value = null

  // 设置部署半径为当前无人机的半径（任务半径优先，其次是无人机默认半径）
  deployRadius.value = drone.taskRadius || drone.radius || 1000

  // 清除地图
  clearMap()

  // 如果没有经纬度，使用默认位置（南京市中心）
  const lng = drone.longitude ?? 118.796877
  const lat = drone.latitude ?? 32.060255

  // 显示无人机位置
  showDroneOnMap({ ...drone, longitude: lng, latitude: lat })

  // 显示检测区域和路径
  if (drone.pathPoints && drone.pathPoints.length > 0) {
    showPathOnMap(drone.pathPoints, lng, lat, drone.taskRadius || 1000)
  } else {
    // 只显示半径圆圈
    showRadiusCircle(lng, lat, drone.radius || 1000)
  }

  // 加载附近井盖
  loadNearbyManholes({ ...drone, longitude: lng, latitude: lat, centerLng: lng, centerLat: lat })

  map.value?.setCenter([lng, lat])
}

// 显示无人机标记（可拖动）
const showDroneOnMap = (drone: Drone) => {
  if (!map.value || !AMap.value || !drone.longitude || !drone.latitude) return

  const lng = pendingPosition.value?.lng ?? drone.longitude
  const lat = pendingPosition.value?.lat ?? drone.latitude

  droneMarker = new AMap.value.Marker({
    position: [lng, lat],
    title: drone.name,
    label: {
      content: drone.name,
      offset: new AMap.value.Pixel(0, -35),
      direction: 'center'
    },
    icon: new AMap.value.Icon({
      size: new AMap.value.Size(40, 40),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
      imageSize: new AMap.value.Size(40, 40)
    }),
    draggable: true,
    zIndex: 150
  })

  if (!originalPosition.value) {
    originalPosition.value = { lng: drone.longitude, lat: drone.latitude }
  }

  // 拖动事件
  droneMarker.on('dragend', (e: any) => {
    const newLng = parseFloat(e.lnglat.getLng().toFixed(6))
    const newLat = parseFloat(e.lnglat.getLat().toFixed(6))

    pendingPosition.value = { lng: newLng, lat: newLat }
    dronePositionChanged.value = true

    // 位置改变，清除之前的路径预览和部署状态
    previewResult.value = null

    // 清除路径线和巡检点标记（无论是否来自预览或已部署）
    if (pathLine) {
      map.value?.remove(pathLine)
      pathLine = null
    }
    pathMarkers.forEach(m => map.value?.remove(m))
    pathMarkers = []

    // 更新圆圈位置
    if (circle) {
      circle.setCenter([newLng, newLat])
    }

    // 重新加载附近井盖（使用新位置）
    const tempDrone = {
      ...drone,
      longitude: newLng,
      latitude: newLat,
      centerLng: newLng,
      centerLat: newLat
    }
    loadNearbyManholes(tempDrone)

    ElMessage.info('位置已改变，请重新预览路径')
  })

  droneMarker.setMap(map.value)
}

// 显示半径圆圈
const showRadiusCircle = (lng: number, lat: number, radius: number) => {
  if (!map.value || !AMap.value) return

  if (circle) map.value.remove(circle)

  const color = selectedDrone.value?.status === 1 ? '#409EFF' : '#67C23A'

  circle = new AMap.value.Circle({
    center: [lng, lat],
    radius: radius,
    fillColor: color,
    fillOpacity: 0.1,
    strokeColor: color,
    strokeWeight: 2,
    strokeStyle: 'dashed',
    zIndex: 100
  })
  circle.setMap(map.value)
}

// 显示路径
const showPathOnMap = (pathPoints: PathPoint[], centerLng: number, centerLat: number, radius: number) => {
  if (!map.value || !AMap.value) return

  // 显示圆圈
  showRadiusCircle(centerLng, centerLat, radius)

  if (!pathPoints || pathPoints.length < 2) return

  // 绘制路径线
  const path = pathPoints.map(p => [p.lng, p.lat])
  pathLine = new AMap.value.Polyline({
    path: path,
    strokeColor: '#409EFF',
    strokeWeight: 3,
    strokeStyle: 'solid',
    showDir: true
  })
  pathLine.setMap(map.value)

  // 绘制巡检点（跳过起点）
  pathPoints.slice(1).forEach((point, index) => {
    const marker = new AMap.value.Marker({
      position: [point.lng, point.lat],
      title: point.manholeNo || `井盖${index + 1}`,
      label: {
        content: `${index + 1}`,
        offset: new AMap.value.Pixel(0, -25)
      },
      icon: new AMap.value.Icon({
        size: new AMap.value.Size(22, 22),
        image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
        imageSize: new AMap.value.Size(22, 22)
      })
    })
    marker.setMap(map.value)
    pathMarkers.push(marker)
  })

  // 调整视野
  map.value.setFitView([circle, pathLine, ...pathMarkers])
}

// 加载附近井盖
const loadNearbyManholes = async (drone: Drone) => {
  console.log('加载附近井盖，无人机数据:', drone)

  // 优先使用任务中心位置，如果没有则使用当前位置
  const lng = drone.centerLng ?? drone.longitude
  const lat = drone.centerLat ?? drone.latitude

  if (lng == null || lat == null) {
    console.warn('无人机位置信息不完整，无法加载附近井盖')
    return
  }

  const radius = (drone.taskRadius || drone.radius || 1000) * 2.5
  console.log(`查询参数: lng=${lng}, lat=${lat}, radius=${radius}`)

  try {
    const res = await manholeApi.getNearbyManholes(lng, lat, radius)
    console.log('附近井盖查询结果:', res)
    if (res.code === 200 && res.data) {
      showManholesOnMap(res.data, drone)
    }
  } catch (error) {
    console.error('加载附近井盖失败:', error)
  }
}

// 显示井盖标记
const showManholesOnMap = (manholes: ManholeMapVO[], drone: Drone) => {
  if (!map.value || !AMap.value) return

  // 清除之前的井盖标记
  manholeMarkers.forEach(m => map.value?.remove(m))
  manholeMarkers = []

  const centerLng = pendingPosition.value?.lng ?? drone.longitude
  const centerLat = pendingPosition.value?.lat ?? drone.latitude
  // 优先使用当前的 deployRadius（滑块值），然后是传入的 radius
  const radius = deployRadius.value || drone.radius || 1000

  manholes.forEach(manhole => {
    // 判断是否在半径范围内
    const distance = calculateDistance(centerLng, centerLat, manhole.longitude, manhole.latitude)
    const isInRadius = distance <= radius

    const colors: Record<number, string> = {
      0: '#f56c6c',
      1: '#67c23a',
      2: '#e6a23c'
    }
    const color = isInRadius ? '#87CEEB' : (colors[manhole.status || 1])

    const marker = new AMap.value.Marker({
      position: [manhole.longitude, manhole.latitude],
      title: manhole.manholeId || '井盖',
      icon: new AMap.value.Icon({
        type: 'image',
        image: `data:image/svg+xml;base64,${btoa(`
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="32" viewBox="0 0 24 32">
            <path fill="${color}" d="M12 0C5.4 0 0 5.4 0 12c0 9 12 20 12 20s12-11 12-20c0-6.6-5.4-12-12-12z"/>
            <circle fill="#fff" cx="12" cy="12" r="6"/>
          </svg>
        `)}`,
        size: new AMap.value.Size(24, 32),
        imageSize: new AMap.value.Size(24, 32)
      }),
      zIndex: isInRadius ? 35 : 30
    })

    marker.setMap(map.value)
    manholeMarkers.push(marker)
  })
}

// 计算距离
const calculateDistance = (lng1: number, lat1: number, lng2: number, lat2: number): number => {
  const R = 6371000
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

// 清除地图
const clearMap = () => {
  if (circle) {
    map.value?.remove(circle)
    circle = null
  }
  if (pathLine) {
    map.value?.remove(pathLine)
    pathLine = null
  }
  if (droneMarker) {
    map.value?.remove(droneMarker)
    droneMarker = null
  }
  manholeMarkers.forEach(m => map.value?.remove(m))
  manholeMarkers = []
  pathMarkers.forEach(m => map.value?.remove(m))
  pathMarkers = []
}

// 重置位置
const resetDronePosition = () => {
  if (!selectedDrone.value || !originalPosition.value) return

  pendingPosition.value = null
  dronePositionChanged.value = false

  selectDrone(selectedDrone.value)
  ElMessage.success('位置已重置')
}

// 部署无人机
const handleDeploy = async () => {
  if (!selectedDrone.value) return

  const drone = selectedDrone.value
  const deployLng = pendingPosition.value?.lng ?? drone.longitude
  const deployLat = pendingPosition.value?.lat ?? drone.latitude

  if (!deployLng || !deployLat) {
    ElMessage.warning('请先设置无人机位置')
    return
  }

  try {
    await ElMessageBox.confirm(
      `为 "${drone.name}" 设置检测区域并规划路径？`,
      '确认部署',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }

  deployLoading.value = true
  try {
    const res = await droneApi.deploy({
      droneId: drone.id,
      centerLng: deployLng,
      centerLat: deployLat,
      radius: drone.radius || 1000
    })

    if (res.code === 200 && res.data) {
      ElMessage.success(`部署成功！覆盖 ${res.data.manholeCount} 个井盖`)

      dronePositionChanged.value = false
      pendingPosition.value = null
      originalPosition.value = null

      // 刷新并重新选中
      await fetchDrones()
      const updatedDrone = droneList.value.find(d => d.id === drone.id)
      if (updatedDrone) {
        selectDrone(updatedDrone)
      }
    } else {
      ElMessage.error(res.message || '部署失败')
    }
  } catch (error) {
    console.error('部署失败:', error)
    ElMessage.error('部署失败')
  } finally {
    deployLoading.value = false
  }
}

// 开始巡检
const startInspection = async () => {
  if (!selectedDrone.value) return

  actionLoading.value = true
  try {
    const res = await droneApi.startInspection(selectedDrone.value.id)
    if (res.code === 200) {
      ElMessage.success('开始巡检')
      fetchDrones()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

// 结束巡检
const stopInspection = async () => {
  if (!selectedDrone.value) return

  actionLoading.value = true
  try {
    const res = await droneApi.stopInspection(selectedDrone.value.id)
    if (res.code === 200) {
      ElMessage.success('巡检结束')
      fetchDrones()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

// 半径滑块变化时更新地图圆圈和井盖高亮
const onRadiusChange = (val: number) => {
  if (!map.value || !selectedDrone.value) return

  const lng = pendingPosition.value?.lng ?? selectedDrone.value.longitude
  const lat = pendingPosition.value?.lat ?? selectedDrone.value.latitude

  if (!lng || !lat) return

  // 更新圆圈半径
  if (circle) {
    circle.setRadius(val)
  } else {
    // 如果圆圈不存在，创建一个新的
    showRadiusCircle(lng, lat, val)
  }

  // 半径变化，清除之前的路径显示（需要重新预览）
  previewResult.value = null
  // 清除路径线和巡检点标记
  if (pathLine) {
    map.value?.remove(pathLine)
    pathLine = null
  }
  pathMarkers.forEach(m => map.value?.remove(m))
  pathMarkers = []

  // 重新加载附近井盖，更新高亮状态（新的半径范围内的井盖会高亮）
  // 注意：清除 taskRadius 确保使用新的 radius 值
  const tempDrone = {
    ...selectedDrone.value,
    longitude: lng,
    latitude: lat,
    centerLng: lng,
    centerLat: lat,
    taskRadius: undefined,  // 清除任务半径，避免覆盖新半径
    radius: val  // 使用新的半径
  }
  loadNearbyManholes(tempDrone)
}

// 工具函数
const getDroneStatusType = (status?: number) => {
  const types: Record<number, string> = {
    0: 'success',
    1: 'primary',
    2: 'warning',
    3: 'danger'
  }
  return types[status || 0] || 'info'
}

const getTaskStatusType = (status?: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return types[status || 0] || 'info'
}

// 预览部署路径
const previewDeployPath = async () => {
  console.log('预览路径被点击', selectedDrone.value, deployRadius.value, pendingPosition.value)
  if (!selectedDrone.value) {
    ElMessage.warning('请先选择无人机')
    return
  }

  // 优先使用拖动后的位置，如果没有则使用无人机当前位置
  const lng = pendingPosition.value?.lng ?? selectedDrone.value.longitude
  const lat = pendingPosition.value?.lat ?? selectedDrone.value.latitude

  if (!lng || !lat) {
    ElMessage.warning('无人机位置信息不完整')
    return
  }

  previewLoading.value = true
  try {
    const requestData = {
      droneId: selectedDrone.value.id,
      centerLat: lat,
      centerLng: lng,
      radius: deployRadius.value
    }
    console.log('预览路径请求参数:', requestData)
    const res = await droneApi.previewPathPlan(requestData)
    console.log('预览路径响应:', res)
    if (res.code === 200 && res.data) {
      previewResult.value = res.data
      drawPreviewOnMap(res.data)
      ElMessage.success(`路径规划完成，覆盖${res.data.manholeCount}个井盖`)
    } else {
      ElMessage.warning(res.message || '该区域未找到井盖')
    }
  } catch (error) {
    console.error('预览路径失败:', error)
    ElMessage.error('路径规划失败')
  } finally {
    previewLoading.value = false
  }
}

// 在地图上绘制预览
const drawPreviewOnMap = (result: PathPlanResult) => {
  if (!map.value) return

  clearMap()

  // 绘制圆形区域
  circle = new AMap.value.Circle({
    center: [result.centerLng, result.centerLat],
    radius: result.radius,
    fillColor: '#67C23A',
    fillOpacity: 0.15,
    strokeColor: '#67C23A',
    strokeWeight: 2
  })
  circle.setMap(map.value)

  // 绘制起点
  const startMarker = new AMap.value.Marker({
    position: [result.centerLng, result.centerLat],
    title: '起点',
    label: { content: '起', offset: new AMap.value.Pixel(0, -30) }
  })
  startMarker.setMap(map.value)
  pathMarkers.push(startMarker)

  // 绘制路径
  if (result.pathPoints && result.pathPoints.length > 1) {
    const path = result.pathPoints.map(p => [p.lng, p.lat])
    pathLine = new AMap.value.Polyline({
      path: path,
      strokeColor: '#409EFF',
      strokeWeight: 3,
      strokeStyle: 'solid',
      showDir: true
    })
    pathLine.setMap(map.value)

    // 绘制巡检点
    result.pathPoints.slice(1).forEach((point, index) => {
      const marker = new AMap.value.Marker({
        position: [point.lng, point.lat],
        title: point.manholeNo || `井盖${index + 1}`,
        label: {
          content: `${index + 1}`,
          offset: new AMap.value.Pixel(0, -25)
        }
      })
      marker.setMap(map.value)
      pathMarkers.push(marker)
    })

    map.value.setFitView([circle, pathLine, ...pathMarkers])
  }
}

// 确认部署
const confirmDeploy = async () => {
  if (!previewResult.value || !selectedDrone.value) {
    ElMessage.warning('请先预览路径')
    return
  }

  // 优先使用拖动后的位置，如果没有则使用无人机当前位置
  const lng = pendingPosition.value?.lng ?? selectedDrone.value.longitude
  const lat = pendingPosition.value?.lat ?? selectedDrone.value.latitude

  if (!lng || !lat) {
    ElMessage.warning('无人机位置信息不完整')
    return
  }

  deployLoading.value = true
  try {
    const res = await droneApi.deploy({
      droneId: selectedDrone.value.id,
      centerLat: lat,
      centerLng: lng,
      radius: deployRadius.value,
      pathPoints: previewResult.value.pathPoints  // 传入预览时缓存的路径
    })
    if (res.code === 200) {
      ElMessage.success('部署成功')
      previewResult.value = null
      // 清除待处理位置，因为已经部署到新位置
      pendingPosition.value = null
      dronePositionChanged.value = false
      await fetchDrones()
      clearMap()
      if (res.data) {
        selectDrone(res.data)
      }
    } else {
      ElMessage.error(res.message || '部署失败')
    }
  } catch (error) {
    ElMessage.error('部署失败')
  } finally {
    deployLoading.value = false
  }
}

// 获取任务列表
const fetchTasks = async () => {
  taskLoading.value = true
  try {
    const res = await droneApi.getTaskList()
    taskList.value = res.data || []
  } catch (error) {
    console.error('获取任务列表失败', error)
  } finally {
    taskLoading.value = false
  }
}

// 在地图上显示无人机任务
const showTaskOnMap = (task: DroneTask) => {
  showTaskList.value = false
  // 查找对应的无人机
  const drone = droneList.value.find(d => d.id === task.droneId)
  if (drone) {
    selectDrone(drone)
  } else {
    ElMessage.warning('未找到对应的无人机')
  }
}

// 任务操作
const startTask = async (task: DroneTask) => {
  try {
    await droneApi.startTask(task.id)
    ElMessage.success('任务已开始')
    fetchTasks()
    fetchDrones()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const completeTask = async (task: DroneTask) => {
  try {
    await droneApi.completeTask(task.id)
    ElMessage.success('任务已完成')
    fetchTasks()
    fetchDrones()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const cancelTask = async (task: DroneTask) => {
  try {
    await ElMessageBox.confirm('确定取消该任务吗？', '提示', { type: 'warning' })
    await droneApi.cancelTask(task.id)
    ElMessage.success('任务已取消')
    fetchTasks()
    fetchDrones()
  } catch (error) {
    // 用户取消
  }
}

// 初始化地图
const initMap = async () => {
  try {
    ;(window as any)._AMapSecurityConfig = {
      securityJsCode: 'a8cc4b1f9059d7e80c3b2c28ee3e9e31'
    }

    AMap.value = await AMapLoader.load({
      key: '0713d505f8ee48a9c7fe9d43f7e2fef5',
      version: '2.0',
      plugins: ['AMap.Circle', 'AMap.Marker', 'AMap.Polyline', 'AMap.ToolBar']
    })

    map.value = new AMap.value.Map('drone-map', {
      zoom: 14,
      center: [118.796877, 32.060255],
      viewMode: '2D',
      mapStyle: 'amap://styles/whitesmoke'
    })

    map.value.addControl(new AMap.value.ToolBar())

    console.log('地图初始化完成')
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图初始化失败')
  }
}

// 生命周期
onMounted(async () => {
  await initMap()
  await fetchDrones()
  await fetchTasks()
})

onUnmounted(() => {
  if (map.value) {
    map.value.destroy()
  }
})
</script>

<style scoped lang="scss">
.drone-page {
  padding: 16px 20px;
  height: calc(100vh - 84px);
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;

    .title-icon {
      font-size: 28px;
      color: #409EFF;
    }

    .title-content {
      h1 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }

      p {
        margin: 2px 0 0;
        color: #909399;
        font-size: 12px;
      }
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  gap: 20px;
  min-height: 0;
}

.drone-list-section {
  width: 280px;
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .section-title {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 6px;
    flex-shrink: 0;
  }
}

.drone-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.drone-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;

  &:hover {
    background: #ecf5ff;
  }

  &.active {
    border-color: #409EFF;
    background: #ecf5ff;
  }

  &.inspecting {
    border-left: 3px solid #409EFF;
  }

  .drone-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .battery {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      color: #67C23A;

      &.low {
        color: #F56C6C;
      }
    }
  }

  .drone-body {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;

    .drone-icon {
      font-size: 32px;
      color: #409EFF;
      margin-bottom: 4px;
    }

    .drone-name {
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    .drone-info {
      font-size: 11px;
      color: #606266;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
  }
}

.map-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;

  .map-container {
    width: 100%;
    height: 100%;
  }

  .map-actions {
    position: absolute;
    top: 12px;
    left: 12px;
    display: flex;
    gap: 8px;
    background: rgba(255, 255, 255, 0.95);
    padding: 8px;
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .drone-panel {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 280px;
    background: rgba(255, 255, 255, 0.98);
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    border: 1px solid #ebeef5;

    h4 {
      margin: 0 0 16px;
      font-size: 18px;
      color: #303133;
      border-bottom: 2px solid #409EFF;
      padding-bottom: 10px;
      font-weight: 600;
    }

    .panel-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      font-size: 14px;
      padding: 4px 0;

      span:first-child {
        color: #606266;
      }
    }

    .panel-tip {
      margin-top: 16px;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 6px;
      font-size: 13px;
      color: #909399;
      text-align: center;
    }

    // 部署区域样式
    .deploy-section {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 2px dashed #e4e7ed;

      .section-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 600;
        color: #409EFF;
        margin-bottom: 16px;

        .el-icon {
          font-size: 18px;
        }
      }

      .deploy-info {
        background: #f5f7fa;
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 16px;

        .info-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
          font-size: 13px;

          &:last-child {
            margin-bottom: 0;
          }

          .info-label {
            color: #606266;
          }

          .info-value {
            font-weight: 500;

            &.coords {
              font-family: monospace;
              font-size: 12px;
              color: #409EFF;
              background: #ecf5ff;
              padding: 4px 8px;
              border-radius: 4px;
            }

            &.radius {
              color: #67C23A;
              font-size: 16px;
              font-weight: 600;
            }
          }
        }
      }

      .radius-slider {
        margin: 16px 0;
        padding: 0 8px;
      }

      .deploy-actions {
        display: flex;
        gap: 12px;
        margin-top: 16px;

        .el-button {
          flex: 1;
          height: 40px;
          font-size: 14px;

          .el-icon {
            margin-right: 4px;
          }
        }
      }
    }
  }
}


@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .drone-list-section {
    width: 100%;
    max-height: 250px;
  }
}
</style>
