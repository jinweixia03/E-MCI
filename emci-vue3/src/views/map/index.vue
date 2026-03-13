<template>
  <div class="map-page">
    <!-- 左侧统计面板 -->
    <div class="map-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon size="24"><MapLocation /></el-icon>
          <span>井盖地图</span>
        </div>
        <el-button
          link
          class="toggle-btn"
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <el-icon><ArrowLeft v-if="!sidebarCollapsed" /><ArrowRight v-else /></el-icon>
        </el-button>
      </div>

      <div v-show="!sidebarCollapsed" class="sidebar-content">
        <!-- 统计卡片 -->
        <div class="stats-section">
          <h4 class="section-title">概览统计</h4>
          <div class="stat-cards">
            <div class="stat-card total">
              <div class="stat-icon">
                <el-icon><Grid /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats?.totalCount || 0 }}</div>
                <div class="stat-label">井盖总数</div>
              </div>
            </div>
            <div class="stat-card normal">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats?.normalCount || 0 }}</div>
                <div class="stat-label">正常</div>
              </div>
            </div>
            <div class="stat-card damaged">
              <div class="stat-icon">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats?.damagedCount || 0 }}</div>
                <div class="stat-label">损坏</div>
              </div>
            </div>
            <div class="stat-card repairing">
              <div class="stat-icon">
                <el-icon><Tools /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats?.repairingCount || 0 }}</div>
                <div class="stat-label">维修中</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 筛选器 -->
        <div class="filter-section">
          <h4 class="section-title">
            <el-icon><Filter /></el-icon>
            筛选条件
          </h4>

          <div class="filter-group">
            <label>城市</label>
            <el-select
              v-model="filters.city"
              placeholder="选择城市"
              clearable
              @change="handleCityChange"
            >
              <el-option
                v-for="city in cities"
                :key="city"
                :label="city"
                :value="city"
              />
            </el-select>
          </div>

          <div class="filter-group">
            <label>区县</label>
            <el-select
              v-model="filters.district"
              placeholder="选择区县"
              clearable
              :disabled="!filters.city"
              @change="handleFilterChange"
            >
              <el-option
                v-for="district in districts"
                :key="district"
                :label="district"
                :value="district"
              />
            </el-select>
          </div>

          <div class="filter-group">
            <label>井盖状态</label>
            <el-select
              v-model="filters.status"
              placeholder="选择状态"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="正常" :value="0" />
              <el-option label="损坏" :value="1" />
              <el-option label="维修中" :value="2" />
            </el-select>
          </div>

          <div class="filter-group">
            <label>井盖类型</label>
            <el-select
              v-model="filters.manholeType"
              placeholder="选择类型"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="雨水" :value="1" />
              <el-option label="污水" :value="2" />
              <el-option label="电力" :value="3" />
              <el-option label="通信" :value="4" />
              <el-option label="燃气" :value="5" />
            </el-select>
          </div>

          <div class="filter-group">
            <el-checkbox v-model="filters.onlyDefect" @change="handleFilterChange">
              只显示有缺陷的
            </el-checkbox>
          </div>

          <div class="filter-actions">
            <el-button type="primary" @click="applyFilters">
              <el-icon><Search /></el-icon>应用筛选
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><RefreshRight /></el-icon>重置
            </el-button>
          </div>
        </div>

        <!-- 类型分布 -->
        <div class="distribution-section" v-if="stats?.typeDistribution?.length">
          <h4 class="section-title">类型分布</h4>
          <div class="type-list">
            <div
              v-for="item in stats.typeDistribution"
              :key="item.type"
              class="type-item"
            >
              <div class="type-info">
                <span class="type-name">{{ item.name }}</span>
                <span class="type-count">{{ item.count }}</span>
              </div>
              <el-progress
                :percentage="item.percentage"
                :color="getTypeColor(item.type)"
                :stroke-width="8"
                :show-text="false"
              />
            </div>
          </div>
        </div>

        <!-- 状态分布 -->
        <div class="distribution-section" v-if="stats?.statusDistribution?.length">
          <h4 class="section-title">状态分布</h4>
          <div class="status-chart">
            <div
              v-for="item in stats.statusDistribution"
              :key="item.status"
              class="status-bar"
              :style="{
                width: item.percentage + '%',
                backgroundColor: getStatusColor(item.status)
              }"
              :title="`${item.name}: ${item.count} (${item.percentage}%)`"
            />
          </div>
          <div class="status-legend">
            <div
              v-for="item in stats.statusDistribution"
              :key="item.status"
              class="legend-item"
            >
              <span
                class="legend-dot"
                :style="{ backgroundColor: getStatusColor(item.status) }"
              />
              <span class="legend-name">{{ item.name }}</span>
              <span class="legend-value">{{ item.count }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 地图容器 -->
    <div class="map-container">
      <!-- 搜索框 -->
      <div class="map-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索井盖编号或地址..."
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 地图控件 -->
      <div class="map-controls">
        <el-button-group>
          <el-button @click="zoomIn" title="放大">
            <el-icon><Plus /></el-icon>
          </el-button>
          <el-button @click="zoomOut" title="缩小">
            <el-icon><Minus /></el-icon>
          </el-button>
        </el-button-group>
        <el-button type="primary" @click="resetMap" title="重置视图">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
        <el-button @click="locateMe" title="定位当前位置" type="primary">
          <el-icon><Location /></el-icon>定位
        </el-button>
        <el-button
          :type="enableCluster ? 'primary' : 'default'"
          @click="toggleCluster"
          title="切换聚合模式"
        >
          <el-icon><Connection /></el-icon>聚合
        </el-button>
        <el-button type="primary" @click="loadManholes" title="刷新当前视野数据">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>

      <!-- 状态筛选快捷按钮 -->
      <div class="status-filter-bar">
        <span class="filter-label">状态筛选:</span>
        <el-radio-group v-model="filters.status" @change="handleFilterChange" size="small">
          <el-radio-button :label="undefined">全部</el-radio-button>
          <el-radio-button :value="0">
            <span class="status-dot normal"></span>正常
          </el-radio-button>
          <el-radio-button :value="1">
            <span class="status-dot damaged"></span>损坏
          </el-radio-button>
          <el-radio-button :value="2">
            <span class="status-dot repairing"></span>维修中
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 图例 -->
      <div class="map-legend">
        <div class="legend-title">井盖状态</div>
        <div class="legend-item">
          <span class="legend-marker normal" />
          <span>正常</span>
        </div>
        <div class="legend-item">
          <span class="legend-marker damaged" />
          <span>损坏</span>
        </div>
        <div class="legend-item">
          <span class="legend-marker repairing" />
          <span>维修中</span>
        </div>
      </div>

      <!-- 高德地图容器 -->
      <div id="amap-container" ref="mapContainer" class="amap-container"></div>
    </div>

    <!-- 井盖详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="井盖详情"
      width="500px"
      destroy-on-close
    >
      <div v-if="selectedManhole" class="manhole-detail">
        <div class="detail-header">
          <div class="manhole-id">{{ selectedManhole.manholeId }}</div>
          <el-tag :type="getStatusTagType(selectedManhole.status)">
            {{ selectedManhole.statusText }}
          </el-tag>
        </div>

        <div class="detail-image">
          <el-image :src="getFullImageUrl(selectedManhole?.imgUrl)" fit="cover">
            <template #error>
              <div class="image-error">
                <el-icon size="48"><Picture /></el-icon>
                <span>暂无图片</span>
              </div>
            </template>
          </el-image>
        </div>

        <div class="detail-info">
          <div class="info-row">
            <span class="label">地址：</span>
            <span class="value">{{ selectedManhole.address || '暂无' }}</span>
          </div>
          <div class="info-row">
            <span class="label">类型：</span>
            <span class="value">{{ selectedManhole.manholeTypeText || '未知' }}</span>
          </div>
          <div class="info-row">
            <span class="label">坐标：</span>
            <span class="value">
              {{ selectedManhole.latitude?.toFixed(6) }}, {{ selectedManhole.longitude?.toFixed(6) }}
            </span>
          </div>
          <div class="info-row">
            <span class="label">检测次数：</span>
            <span class="value">{{ selectedManhole.detectionCount || 0 }}</span>
          </div>
          <div class="info-row">
            <span class="label">缺陷数量：</span>
            <span class="value" :class="{ danger: selectedManhole.defectCount > 0 }">
              {{ selectedManhole.defectCount || 0 }}
            </span>
          </div>
          <div class="info-row">
            <span class="label">安全评分：</span>
            <span class="value">
              <el-rate
                v-model="safetyScoreRate"
                disabled
                :colors="['#ff4d4f', '#ff7a45', '#ffa940', '#73d13d', '#52c41a']"
              />
              {{ selectedManhole.safetyScore?.toFixed(1) || '-' }}
            </span>
          </div>
          <div class="info-row">
            <span class="label">下次检测：</span>
            <span class="value">{{ selectedManhole.nextDetTime || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">最后检测：</span>
            <span class="value">{{ selectedManhole.lastDetTime || '-' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="viewDetail">
          查看详情
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  MapLocation, ArrowLeft, ArrowRight, Grid, CircleCheck, CircleClose,
  Tools, Filter, Search, RefreshRight, Plus, Minus, Refresh, Location, Connection, Picture
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AMapLoader from '@amap/amap-jsapi-loader'
import { manholeApi } from '@/api/manhole'
import type { ManholeMapVO, ManholeMapStats, ManholeClusterVO } from '@/types/manhole'

const router = useRouter()

// ============ Refs ============
const mapContainer = ref<HTMLDivElement>()
const map = ref<any>(null)
const AMap = ref<any>(null)
const cluster = ref<any>(null)
const infoWindow = ref<any>(null)
const markers = ref<any[]>([])
const manholes = ref<ManholeMapVO[]>([])
const stats = ref<ManholeMapStats | null>(null)
const cities = ref<string[]>([])
const districts = ref<string[]>([])
const sidebarCollapsed = ref(false)
const searchKeyword = ref('')
const enableCluster = ref(true)
const detailDialogVisible = ref(false)
const selectedManhole = ref<ManholeMapVO | null>(null)

// 默认井盖图片
const defaultManholeImage = 'https://via.placeholder.com/400x300?text=No+Image'

// 获取完整图片 URL
const getFullImageUrl = (url?: string) => {
  if (!url) return defaultManholeImage
  if (url.startsWith('http')) return url
  // 本地开发环境使用 localhost:8086
  const baseUrl = 'http://localhost:8086'
  // 处理 /uploads/... 路径，转换为 /api/files/...
  if (url.startsWith('/uploads/')) {
    return baseUrl + url.replace('/uploads/', '/api/files/')
  }
  // 已经是 /api/files/ 路径
  if (url.startsWith('/api/')) {
    return baseUrl + url
  }
  return baseUrl + url
}

const filters = ref({
  city: '',
  district: '',
  status: undefined as number | undefined,
  manholeType: undefined as number | undefined,
  onlyDefect: false
})

// ============ Computed ============
const safetyScoreRate = computed(() => {
  if (!selectedManhole.value?.safetyScore) return 0
  return Math.round(selectedManhole.value.safetyScore / 20)
})

// ============ Methods ============
const getStatusColor = (status: number) => {
  const colors: Record<number, string> = {
    0: '#67c23a', // 正常 - 绿色
    1: '#f56c6c', // 损坏 - 红色
    2: '#e6a23c'  // 维修中 - 橙色
  }
  return colors[status] || '#909399'
}

const getStatusTagType = (status: number) => {
  const types: Record<number, any> = {
    0: 'success',  // 正常 - 绿色
    1: 'danger',   // 损坏 - 红色
    2: 'warning'   // 维修中 - 橙色
  }
  return types[status] || 'info'
}

const getTypeColor = (type: number) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  return colors[type - 1] || '#909399'
}

const initMap = async () => {
  try {
    // 安全密钥配置
    ;(window as any)._AMapSecurityConfig = {
      securityJsCode: 'a8cc4b1f9059d7e80c3b2c28ee3e9e31'
    }

    AMap.value = await AMapLoader.load({
      key: '0713d505f8ee48a9c7fe9d43f7e2fef5',
      version: '2.0',
      plugins: [
        'AMap.ToolBar',
        'AMap.Scale',
        'AMap.Geolocation',
        'AMap.MarkerCluster',
        'AMap.InfoWindow',
        'AMap.Geocoder'
      ]
    })

    map.value = new AMap.value.Map('amap-container', {
      zoom: 11,
      center: [118.796877, 32.060255], // 南京
      viewMode: '2D',
      mapStyle: 'amap://styles/whitesmoke'
    })

    // 添加控件
    map.value.addControl(new AMap.value.ToolBar())
    map.value.addControl(new AMap.value.Scale())

    // 加载初始数据
    await loadManholes()
    await loadStats()
    await loadCities()
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图初始化失败')
  }
}

const loadManholes = async () => {
  try {
    const bounds = map.value.getBounds()
    const ne = bounds.getNorthEast()
    const sw = bounds.getSouthWest()

    const params = {
      minLongitude: sw.lng,
      maxLongitude: ne.lng,
      minLatitude: sw.lat,
      maxLatitude: ne.lat,
      ...filters.value,
      maxResults: enableCluster.value ? undefined : 500
    }

    const res = await manholeApi.queryForMap(params)
    manholes.value = res.data || []

    if (enableCluster.value) {
      await renderCluster()
    } else {
      await renderMarkers()
    }
  } catch (error) {
    console.error('加载井盖数据失败:', error)
  }
}

const loadStats = async () => {
  try {
    const res = await manholeApi.getMapStats(filters.value)
    stats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadCities = async () => {
  try {
    const res = await manholeApi.getAllCities()
    cities.value = res.data || []
  } catch (error) {
    console.error('加载城市列表失败:', error)
  }
}

const loadDistricts = async (city: string) => {
  try {
    const res = await manholeApi.getDistrictsByCity(city)
    districts.value = res.data || []
  } catch (error) {
    console.error('加载区县列表失败:', error)
  }
}

const renderMarkers = () => {
  // 清除现有标记和聚合
  clearMarkers()
  if (cluster.value) {
    cluster.value.setMap(null)
    cluster.value = null
  }

  if (!manholes.value.length) return

  markers.value = manholes.value.map(m => {
    const marker = new AMap.value.Marker({
      position: [m.longitude, m.latitude],
      title: m.manholeId,
      icon: getMarkerIcon(m.status),
      offset: new AMap.value.Pixel(-12, -24),
      extData: m
    })

    marker.on('click', () => {
      selectedManhole.value = m
      detailDialogVisible.value = true
      showInfoWindow(m, marker)
    })

    return marker
  })

  map.value.add(markers.value)
}

const renderCluster = async () => {
  // 清除现有标记和聚合
  clearMarkers()
  if (cluster.value) {
    cluster.value.setMap(null)
  }

  const bounds = map.value.getBounds()
  const ne = bounds.getNorthEast()
  const sw = bounds.getSouthWest()

  try {
    const res = await manholeApi.getClusterData({
      minLongitude: sw.lng,
      maxLongitude: ne.lng,
      minLatitude: sw.lat,
      maxLatitude: ne.lat,
      ...filters.value,
      gridSize: 60
    })

    const clusters = res.data || []

    // 创建聚合点数据
    const points = clusters.flatMap(c =>
      c.manholes.map(m => ({
        lnglat: [m.longitude, m.latitude],
        weight: 1,
        ...m
      }))
    )

    if (!points.length) return

    // 聚合样式
    const styles = [
      {
        url: '//a.amap.com/jsapi_demos/static/images/blue.png',
        size: new AMap.value.Size(32, 32),
        offset: new AMap.value.Pixel(-16, -16),
        textColor: '#fff'
      },
      {
        url: '//a.amap.com/jsapi_demos/static/images/green.png',
        size: new AMap.value.Size(36, 36),
        offset: new AMap.value.Pixel(-18, -18),
        textColor: '#fff'
      },
      {
        url: '//a.amap.com/jsapi_demos/static/images/orange.png',
        size: new AMap.value.Size(40, 40),
        offset: new AMap.value.Pixel(-20, -20),
        textColor: '#fff'
      },
      {
        url: '//a.amap.com/jsapi_demos/static/images/red.png',
        size: new AMap.value.Size(44, 44),
        offset: new AMap.value.Pixel(-22, -22),
        textColor: '#fff'
      }
    ]

    AMap.value.plugin(['AMap.MarkerCluster'], () => {
      cluster.value = new AMap.value.MarkerCluster(
        map.value,
        points,
        {
          styles: styles,
          renderMarker: (context: any) => {
            const data = context.data[0] as ManholeMapVO
            context.marker.setIcon(getMarkerIcon(data.status))
            context.marker.setOffset(new AMap.value.Pixel(-12, -24))

            context.marker.on('click', () => {
              selectedManhole.value = data
              detailDialogVisible.value = true
              showInfoWindow(data, context.marker)
            })
          }
        }
      )
    })
  } catch (error) {
    console.error('加载聚合数据失败:', error)
  }
}

const getMarkerIcon = (status?: number) => {
  const colors: Record<number, string> = {
    0: '#f56c6c',
    1: '#67c23a',
    2: '#e6a23c'
  }
  const color = colors[status || 1]

  // 返回圆形标记
  return new AMap.value.Icon({
    type: 'image',
    image: `data:image/svg+xml;base64,${btoa(`
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="32" viewBox="0 0 24 32">
        <path fill="${color}" d="M12 0C5.4 0 0 5.4 0 12c0 9 12 20 12 20s12-11 12-20c0-6.6-5.4-12-12-12z"/>
        <circle fill="#fff" cx="12" cy="12" r="6"/>
      </svg>
    `)}`,
    size: new AMap.value.Size(24, 32),
    imageSize: new AMap.value.Size(24, 32)
  })
}

const showInfoWindow = (manhole: ManholeMapVO, marker: any) => {
  if (infoWindow.value) {
    infoWindow.value.close()
  }

  const content = `
    <div style="padding: 12px; min-width: 200px;">
      <div style="font-weight: bold; font-size: 14px; margin-bottom: 8px;">
        ${manhole.manholeId}
        <span style="
          display: inline-block;
          margin-left: 8px;
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
          color: #fff;
          background: ${getStatusColor(manhole.status)};
        ">${manhole.statusText || '-'}</span>
      </div>
      <div style="color: #666; font-size: 12px; margin-bottom: 4px;">
        地址：${manhole.address || '-'}
      </div>
      <div style="color: #666; font-size: 12px; margin-bottom: 4px;">
        类型：${manhole.manholeTypeText || '-'}
      </div>
      <div style="color: #666; font-size: 12px;">
        缺陷数：${manhole.defectCount || 0}
      </div>
    </div>
  `

  infoWindow.value = new AMap.value.InfoWindow({
    content,
    offset: new AMap.value.Pixel(0, -32)
  })

  infoWindow.value.open(map.value, marker.getPosition())
}

const clearMarkers = () => {
  if (markers.value.length) {
    map.value.remove(markers.value)
    markers.value = []
  }
}

const handleCityChange = async (city: string) => {
  filters.value.district = ''
  if (city) {
    await loadDistricts(city)
    // 定位到城市
    const geocoder = new AMap.value.Geocoder()
    geocoder.getLocation(city, (status: string, result: any) => {
      if (status === 'complete' && result.geocodes.length) {
        const lnglat = result.geocodes[0].location
        map.value.setCenter([lnglat.lng, lnglat.lat], false, 500)
        map.value.setZoom(12)
      }
    })
  }
  handleFilterChange()
}

const handleFilterChange = () => {
  loadManholes()
  loadStats()
}

const applyFilters = () => {
  loadManholes()
  loadStats()
  ElMessage.success('筛选已应用')
}

const resetFilters = () => {
  filters.value = {
    city: '',
    district: '',
    status: undefined,
    manholeType: undefined,
    onlyDefect: false
  }
  districts.value = []
  loadManholes()
  loadStats()
  ElMessage.success('筛选已重置')
}

const handleSearch = async () => {
  if (!searchKeyword.value) {
    loadManholes()
    return
  }

  try {
    const params = {
      keyword: searchKeyword.value,
      maxResults: 100
    }
    const res = await manholeApi.queryForMap(params)
    manholes.value = res.data || []

    if (manholes.value.length > 0) {
      // 定位到第一个结果
      const first = manholes.value[0]
      map.value.setCenter([first.longitude, first.latitude], false, 500)
      map.value.setZoom(15)

      if (!enableCluster.value) {
        renderMarkers()
      }

      ElMessage.success(`找到 ${manholes.value.length} 个井盖`)
    } else {
      ElMessage.warning('未找到匹配的井盖')
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  }
}

const zoomIn = () => {
  if (map.value) {
    const currentZoom = map.value.getZoom()
    map.value.setZoom(currentZoom + 1)
  }
}

const zoomOut = () => {
  if (map.value) {
    const currentZoom = map.value.getZoom()
    map.value.setZoom(currentZoom - 1)
  }
}

const resetMap = () => {
  if (map.value) {
    map.value.setCenter([118.796877, 32.060255])
    map.value.setZoom(11)
  }
  resetFilters()
}

const locateMe = async () => {
  if (!AMap.value || !map.value) {
    ElMessage.warning('地图尚未加载完成')
    return
  }

  try {
    ElMessage.info('正在获取位置...')

    // 使用浏览器原生定位API
    if (!navigator.geolocation) {
      ElMessage.error('浏览器不支持定位功能')
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { longitude, latitude } = position.coords
        console.log('定位成功:', longitude, latitude)

        // 设置地图中心
        map.value.setCenter([longitude, latitude])
        map.value.setZoom(15)

        // 清除之前的定位标记
        clearMarkers()

        // 添加定位标记
        const marker = new AMap.value.Marker({
          position: [longitude, latitude],
          title: '我的位置',
          icon: new AMap.value.Icon({
            image: 'https://webapi.amap.com/theme/v1.3/markers/b/loc.png',
            size: new AMap.value.Size(24, 24),
            imageSize: new AMap.value.Size(24, 24)
          })
        })
        marker.setMap(map.value)
        markers.value.push(marker)

        // 添加信息窗体
        const infoWindow = new AMap.value.InfoWindow({
          content: '<div style="padding:8px;font-size:14px;">📍 我的位置</div>',
          offset: new AMap.value.Pixel(0, -30)
        })
        infoWindow.open(map.value, [longitude, latitude])

        ElMessage.success('定位成功')
      },
      (error) => {
        console.error('定位失败:', error)
        let errorMsg = '定位失败'
        switch (error.code) {
          case error.PERMISSION_DENIED:
            errorMsg = '定位被拒绝，请检查浏览器权限设置'
            break
          case error.POSITION_UNAVAILABLE:
            errorMsg = '位置信息不可用'
            break
          case error.TIMEOUT:
            errorMsg = '定位超时'
            break
        }
        ElMessage.error(errorMsg)
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    )
  } catch (error) {
    console.error('定位出错:', error)
    ElMessage.error('定位功能出错')
  }
}

const toggleCluster = async () => {
  enableCluster.value = !enableCluster.value
  ElMessage.info(enableCluster.value ? '已开启聚合模式' : '已关闭聚合模式')

  // 清除聚合标记（如果存在）
  if (cluster.value) {
    cluster.value.setMap(null)
    cluster.value = null
  }

  // 清除普通标记
  clearMarkers()

  await loadManholes()
}

const viewDetail = () => {
  if (selectedManhole.value) {
    router.push(`/information/detail/${selectedManhole.value.id}`)
  }
}

// ============ Lifecycle ============
onMounted(() => {
  nextTick(() => {
    initMap()
  })
})

onUnmounted(() => {
  if (map.value) {
    map.value.destroy()
  }
})
</script>

<style scoped lang="scss">
.map-page {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

// 侧边栏
.map-sidebar {
  width: 300px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  z-index: 10;
  max-height: 100%;

  &.collapsed {
    width: 100px;

    .sidebar-header {
      padding: 16px 12px;
      justify-content: center;

      .logo span {
        display: none;
      }
    }

    .sidebar-content {
      padding: 16px 12px;
    }
  }

  .sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;

    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .toggle-btn {
      padding: 4px;
    }
  }

  .sidebar-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c0c4cc;
      border-radius: 2px;
    }
  }
}

// 统计卡片
.stats-section {
  margin-bottom: 24px;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .stat-cards {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }


  .stat-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    background: #f5f7fa;

    &.total {
      .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
    }

    &.normal {
      .stat-icon {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
      }
    }

    &.damaged {
      .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
    }

    &.repairing {
      .stat-icon {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }
    }

    .stat-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }

    .stat-info {
      .stat-value {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }

      .stat-label {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

// 筛选器
.filter-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;

  .section-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }

  .filter-group {
    margin-bottom: 12px;

    label {
      display: block;
      font-size: 12px;
      color: #606266;
      margin-bottom: 4px;
    }

    :deep(.el-select) {
      width: 100%;
    }
  }

  .filter-actions {
    display: flex;
    gap: 8px;
    margin-top: 16px;

    .el-button {
      flex: 1;
    }
  }
}

// 分布统计
.distribution-section {
  margin-bottom: 24px;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .type-list {
    .type-item {
      margin-bottom: 12px;

      .type-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 4px;

        .type-name {
          font-size: 13px;
          color: #606266;
        }

        .type-count {
          font-size: 13px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }

  .status-chart {
    display: flex;
    height: 24px;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 12px;

    .status-bar {
      height: 100%;
      transition: width 0.3s ease;
    }
  }

  .status-legend {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;

      .legend-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
      }

      .legend-name {
        color: #606266;
      }

      .legend-value {
        color: #303133;
        font-weight: 600;
      }
    }
  }
}

// 地图容器
.map-container {
  flex: 1;
  position: relative;

  .amap-container {
    width: 100%;
    height: 100%;
  }
}

// 地图搜索
.map-search {
  position: absolute;
  top: 16px;
  left: 16px;
  right: 16px;
  max-width: 400px;
  z-index: 100;

  :deep(.el-input__wrapper) {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}

// 地图控件
.map-controls {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 100;

  .el-button-group {
    display: flex;
    flex-direction: column;

    .el-button {
      margin: 0;
      border-radius: 0;

      &:first-child {
        border-radius: 4px 4px 0 0;
      }

      &:last-child {
        border-radius: 0 0 4px 4px;
      }
    }
  }

  .el-button {
    background: #fff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    &:hover {
      background: #f5f7fa;
    }
  }
}

// 状态筛选栏
.status-filter-bar {
  position: absolute;
  top: 16px;
  left: 316px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 8px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;

  .filter-label {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    white-space: nowrap;
  }

  .status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 4px;

    &.normal {
      background: #67c23a;
    }

    &.damaged {
      background: #f56c6c;
    }

    &.repairing {
      background: #e6a23c;
    }
  }
}

// 侧边栏收起时调整筛选栏位置
.map-sidebar.collapsed + .map-container .status-filter-bar {
  left: 116px;
}

// 地图图例
.map-legend {
  position: absolute;
  bottom: 16px;
  right: 16px;
  background: #fff;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;

  .legend-title {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
    font-size: 12px;
    color: #606266;

    &:last-child {
      margin-bottom: 0;
    }

    .legend-marker {
      width: 12px;
      height: 12px;
      border-radius: 50%;

      &.normal {
        background: #67c23a;
      }

      &.damaged {
        background: #f56c6c;
      }

      &.repairing {
        background: #e6a23c;
      }
    }
  }
}

// 详情弹窗
.manhole-detail {
  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;

    .manhole-id {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  .detail-image {
    width: 100%;
    height: 200px;
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 16px;
    background: #f5f7fa;

    .el-image {
      width: 100%;
      height: 100%;
    }

    .image-error {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;
      gap: 8px;

      span {
        font-size: 14px;
      }
    }
  }

  .detail-info {
    .info-row {
      display: flex;
      margin-bottom: 12px;

      .label {
        width: 90px;
        color: #909399;
        font-size: 13px;
      }

      .value {
        flex: 1;
        color: #303133;
        font-size: 13px;

        &.danger {
          color: #f56c6c;
          font-weight: 600;
        }
      }
    }
  }
}

:deep(.amap-logo) {
  display: none !important;
}

:deep(.amap-copyright) {
  display: none !important;
}
</style>
