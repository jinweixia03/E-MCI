<template>
  <div class="dashboard-page">
    <!-- 头部 -->
    <div class="dashboard-header">
      <div class="header-decoration left"></div>
      <h1 class="header-title">智慧井盖运维管理数据大屏</h1>
      <div class="header-decoration right"></div>
      <div class="header-time">
        <span>{{ currentDate }}</span>
        <span>{{ currentTime }}</span>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="dashboard-body">
      <!-- 左侧 -->
      <div class="dashboard-left">
        <!-- 井盖统计卡片 -->
        <div class="chart-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>井盖概况</span>
          </div>
          <div class="manhole-stats">
            <div class="stat-item total">
              <div class="stat-value">{{ stats?.manholeStats?.totalCount || 0 }}</div>
              <div class="stat-label">井盖总数</div>
            </div>
            <div class="stat-item normal">
              <div class="stat-value">{{ stats?.manholeStats?.normalCount || 0 }}</div>
              <div class="stat-label">正常</div>
            </div>
            <div class="stat-item damaged">
              <div class="stat-value">{{ stats?.manholeStats?.damagedCount || 0 }}</div>
              <div class="stat-label">损坏</div>
            </div>
            <div class="stat-item repairing">
              <div class="stat-value">{{ stats?.manholeStats?.repairingCount || 0 }}</div>
              <div class="stat-label">维修中</div>
            </div>
          </div>
        </div>

        <!-- 状态分布饼图 -->
        <div class="chart-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>井盖状态分布</span>
          </div>
          <div ref="statusChart" class="chart-content"></div>
        </div>

        <!-- 井盖类型分布 -->
        <div class="chart-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>井盖类型分布</span>
          </div>
          <div ref="typeChart" class="chart-content"></div>
        </div>
      </div>

      <!-- 中间 -->
      <div class="dashboard-center">
        <!-- 地图 -->
        <div class="chart-box map-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>井盖分布地图</span>
          </div>
          <div id="dashboard-map" class="map-content"></div>
        </div>

        <!-- 检测趋势 -->
        <div class="chart-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>近7天检测趋势</span>
          </div>
          <div ref="trendChart" class="chart-content"></div>
        </div>
      </div>

      <!-- 右侧 -->
      <div class="dashboard-right">
        <!-- 检测统计 -->
        <div class="chart-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>检测统计</span>
          </div>
          <div class="detection-stats">
            <div class="detection-item">
              <div class="detection-label">今日检测</div>
              <div class="detection-value">{{ stats?.detectionStats?.todayCount || 0 }}</div>
              <div class="detection-sub">缺陷: {{ stats?.detectionStats?.todayDefectCount || 0 }}</div>
            </div>
            <div class="detection-item">
              <div class="detection-label">本周检测</div>
              <div class="detection-value">{{ stats?.detectionStats?.weekCount || 0 }}</div>
              <div class="detection-sub">缺陷: {{ stats?.detectionStats?.weekDefectCount || 0 }}</div>
            </div>
            <div class="detection-item">
              <div class="detection-label">本月检测</div>
              <div class="detection-value">{{ stats?.detectionStats?.monthCount || 0 }}</div>
              <div class="detection-sub">缺陷: {{ stats?.detectionStats?.monthDefectCount || 0 }}</div>
            </div>
          </div>
        </div>

        <!-- 缺陷类型分布 -->
        <div class="chart-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>缺陷类型分布</span>
          </div>
          <div ref="defectChart" class="chart-content"></div>
        </div>

        <!-- 维修任务统计 -->
        <div class="chart-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>维修任务</span>
          </div>
          <div class="repair-stats">
            <div class="repair-item pending">
              <div class="repair-icon">⏳</div>
              <div class="repair-info">
                <div class="repair-value">{{ stats?.repairStats?.pendingCount || 0 }}</div>
                <div class="repair-label">待处理</div>
              </div>
            </div>
            <div class="repair-item progress">
              <div class="repair-icon">🔧</div>
              <div class="repair-info">
                <div class="repair-value">{{ stats?.repairStats?.inProgressCount || 0 }}</div>
                <div class="repair-label">进行中</div>
              </div>
            </div>
            <div class="repair-item completed">
              <div class="repair-icon">✅</div>
              <div class="repair-info">
                <div class="repair-value">{{ stats?.repairStats?.completedCount || 0 }}</div>
                <div class="repair-label">已完成</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 无人机状态 -->
        <div class="chart-box">
          <div class="chart-title">
            <span class="title-icon"></span>
            <span>无人机状态</span>
          </div>
          <div class="drone-stats">
            <div class="drone-item">
              <div class="drone-value">{{ stats?.droneStats?.totalCount || 0 }}</div>
              <div class="drone-label">总数</div>
            </div>
            <div class="drone-item online">
              <div class="drone-value">{{ stats?.droneStats?.onlineCount || 0 }}</div>
              <div class="drone-label">在线</div>
            </div>
            <div class="drone-item offline">
              <div class="drone-value">{{ stats?.droneStats?.offlineCount || 0 }}</div>
              <div class="drone-label">离线</div>
            </div>
            <div class="drone-item fault">
              <div class="drone-value">{{ stats?.droneStats?.faultCount || 0 }}</div>
              <div class="drone-label">故障</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import AMapLoader from '@amap/amap-jsapi-loader'
import { dashboardApi } from '@/api/dashboard'
import type { DashboardStats } from '@/types/dashboard'

const stats = ref<DashboardStats | null>(null)
const currentTime = ref('')
const currentDate = ref('')

// 图表实例
const statusChart = ref<HTMLDivElement>()
const typeChart = ref<HTMLDivElement>()
const trendChart = ref<HTMLDivElement>()
const defectChart = ref<HTMLDivElement>()

let statusChartInstance: echarts.ECharts | null = null
let typeChartInstance: echarts.ECharts | null = null
let trendChartInstance: echarts.ECharts | null = null
let defectChartInstance: echarts.ECharts | null = null
let mapInstance: any = null

let timer: ReturnType<typeof setInterval> | null = null

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN')
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'long' })
}

// 加载数据
const loadData = async () => {
  try {
    const res = await dashboardApi.getStats()
    stats.value = res.data
    initCharts()
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  if (!stats.value) return

  // 状态分布饼图
  if (statusChart.value) {
    statusChartInstance = echarts.init(statusChart.value)
    statusChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%', textStyle: { color: '#fff' } },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#0f1c3f', borderWidth: 2 },
        label: { show: false },
        data: stats.value.statusDistribution?.map(item => ({
          name: item.name,
          value: item.count,
          itemStyle: { color: item.status === 0 ? '#67c23a' : item.status === 1 ? '#f56c6c' : '#e6a23c' }
        })) || []
      }]
    })
  }

  // 类型分布柱状图
  if (typeChart.value) {
    typeChartInstance = echarts.init(typeChart.value)
    typeChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: stats.value.manholeTypeDistribution?.map(item => item.name) || [],
        axisLabel: { color: '#fff' },
        axisLine: { lineStyle: { color: '#333' } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#fff' },
        splitLine: { lineStyle: { color: '#333' } }
      },
      series: [{
        type: 'bar',
        data: stats.value.manholeTypeDistribution?.map(item => ({
          value: item.count,
          itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#409eff' }, { offset: 1, color: '#1e3a8a' }]) }
        })) || [],
        barWidth: '60%'
      }]
    })
  }

  // 检测趋势折线图
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    trendChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['检测数', '缺陷数'], textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: stats.value.detectionTrend?.map(item => item.date) || [],
        axisLabel: { color: '#fff' },
        axisLine: { lineStyle: { color: '#333' } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#fff' },
        splitLine: { lineStyle: { color: '#333' } }
      },
      series: [
        {
          name: '检测数',
          type: 'line',
          smooth: true,
          data: stats.value.detectionTrend?.map(item => item.detectionCount) || [],
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.3)' }, { offset: 1, color: 'rgba(64,158,255,0)' }])
          },
          lineStyle: { color: '#409eff' }
        },
        {
          name: '缺陷数',
          type: 'line',
          smooth: true,
          data: stats.value.detectionTrend?.map(item => item.defectCount) || [],
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245,108,108,0.3)' }, { offset: 1, color: 'rgba(245,108,108,0)' }])
          },
          lineStyle: { color: '#f56c6c' }
        }
      ]
    })
  }

  // 缺陷类型横向柱状图
  if (defectChart.value) {
    defectChartInstance = echarts.init(defectChart.value)
    defectChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'value',
        axisLabel: { color: '#fff' },
        splitLine: { lineStyle: { color: '#333' } }
      },
      yAxis: {
        type: 'category',
        data: stats.value.defectTypeDistribution?.map(item => item.name).reverse() || [],
        axisLabel: { color: '#fff' },
        axisLine: { lineStyle: { color: '#333' } }
      },
      series: [{
        type: 'bar',
        data: stats.value.defectTypeDistribution?.map(item => ({
          value: item.count,
          itemStyle: { color: '#e6a23c' }
        })).reverse() || [],
        barWidth: '50%',
        label: { show: true, position: 'right', color: '#fff' }
      }]
    })
  }
}

// 初始化地图
const initMap = async () => {
  try {
    ;(window as any)._AMapSecurityConfig = {
      securityJsCode: 'a8cc4b1f9059d7e80c3b2c28ee3e9e31'
    }

    const AMap = await AMapLoader.load({
      key: '0713d505f8ee48a9c7fe9d43f7e2fef5',
      version: '2.0'
    })

    mapInstance = new AMap.Map('dashboard-map', {
      zoom: 11,
      center: [118.796877, 32.060255],
      viewMode: '2D',
      mapStyle: 'amap://styles/dark'
    })
  } catch (error) {
    console.error('地图加载失败:', error)
  }
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  statusChartInstance?.resize()
  typeChartInstance?.resize()
  trendChartInstance?.resize()
  defectChartInstance?.resize()
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadData()
  initMap()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  statusChartInstance?.dispose()
  typeChartInstance?.dispose()
  trendChartInstance?.dispose()
  defectChartInstance?.dispose()
  mapInstance?.destroy()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.dashboard-page {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #0a0f1e 0%, #1a1f3a 50%, #0f1629 100%);
  color: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

// 头部
.dashboard-header {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(90deg, transparent 0%, rgba(64,158,255,0.1) 20%, rgba(64,158,255,0.2) 50%, rgba(64,158,255,0.1) 80%, transparent 100%);
  border-bottom: 1px solid rgba(64,158,255,0.3);

  .header-decoration {
    width: 200px;
    height: 2px;
    background: linear-gradient(90deg, transparent, #409eff, transparent);
    position: relative;

    &::before {
      content: '';
      position: absolute;
      width: 10px;
      height: 10px;
      background: #409eff;
      border-radius: 50%;
      top: -4px;
    }

    &.left::before { right: 0; }
    &.right::before { left: 0; }
  }

  .header-title {
    font-size: 28px;
    font-weight: bold;
    margin: 0 30px;
    background: linear-gradient(90deg, #fff, #409eff, #fff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    text-shadow: 0 0 20px rgba(64,158,255,0.5);
  }

  .header-time {
    position: absolute;
    right: 30px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    font-size: 14px;
    color: #8c9bb3;
  }
}

// 主体
.dashboard-body {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1.5fr 1fr;
  gap: 15px;
  padding: 15px;
  overflow: hidden;
}

// 通用图表盒子
.chart-box {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(64,158,255,0.2);
  border-radius: 8px;
  padding: 15px;
  display: flex;
  flex-direction: column;

  .chart-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 500;
    color: #fff;
    margin-bottom: 15px;

    .title-icon {
      width: 4px;
      height: 16px;
      background: linear-gradient(180deg, #409eff, #1e3a8a);
      border-radius: 2px;
    }
  }

  .chart-content {
    flex: 1;
    min-height: 0;
  }
}

// 左侧
.dashboard-left {
  display: flex;
  flex-direction: column;
  gap: 15px;

  .manhole-stats {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;

    .stat-item {
      background: rgba(255,255,255,0.05);
      border-radius: 8px;
      padding: 15px;
      text-align: center;
      border-left: 4px solid;

      &.total { border-color: #409eff; }
      &.normal { border-color: #67c23a; }
      &.damaged { border-color: #f56c6c; }
      &.repairing { border-color: #e6a23c; }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #fff;
      }

      .stat-label {
        font-size: 12px;
        color: #8c9bb3;
        margin-top: 5px;
      }
    }
  }
}

// 中间
.dashboard-center {
  display: flex;
  flex-direction: column;
  gap: 15px;

  .map-box {
    flex: 1.5;

    .map-content {
      flex: 1;
      border-radius: 4px;
      overflow: hidden;
    }
  }
}

// 右侧
.dashboard-right {
  display: flex;
  flex-direction: column;
  gap: 15px;

  .detection-stats {
    display: flex;
    justify-content: space-between;
    gap: 10px;

    .detection-item {
      flex: 1;
      background: rgba(255,255,255,0.05);
      border-radius: 8px;
      padding: 10px;
      text-align: center;

      .detection-label {
        font-size: 12px;
        color: #8c9bb3;
      }

      .detection-value {
        font-size: 24px;
        font-weight: bold;
        color: #409eff;
        margin: 5px 0;
      }

      .detection-sub {
        font-size: 11px;
        color: #f56c6c;
      }
    }
  }

  .repair-stats {
    display: flex;
    justify-content: space-between;
    gap: 10px;

    .repair-item {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 10px;
      background: rgba(255,255,255,0.05);
      border-radius: 8px;
      padding: 10px;

      .repair-icon {
        font-size: 24px;
      }

      .repair-info {
        .repair-value {
          font-size: 20px;
          font-weight: bold;
          color: #fff;
        }

        .repair-label {
          font-size: 12px;
          color: #8c9bb3;
        }
      }

      &.pending { border-left: 3px solid #e6a23c; }
      &.progress { border-left: 3px solid #409eff; }
      &.completed { border-left: 3px solid #67c23a; }
    }
  }

  .drone-stats {
    display: flex;
    justify-content: space-between;
    gap: 10px;

    .drone-item {
      flex: 1;
      text-align: center;
      padding: 10px;
      background: rgba(255,255,255,0.05);
      border-radius: 8px;

      .drone-value {
        font-size: 22px;
        font-weight: bold;
        color: #fff;
      }

      .drone-label {
        font-size: 12px;
        color: #8c9bb3;
        margin-top: 5px;
      }

      &.online .drone-value { color: #67c23a; }
      &.offline .drone-value { color: #909399; }
      &.fault .drone-value { color: #f56c6c; }
    }
  }
}

:deep(.amap-logo) { display: none !important; }
:deep(.amap-copyright) { display: none !important; }
</style>
