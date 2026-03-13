<template>
  <div class="dashboard-page">
    <!-- 主体内容 -->
    <div class="dashboard-body">
      <!-- 顶部统计行 - 15% -->
      <div class="section stats-section">
        <div class="stat-card total">
          <div class="stat-icon">📊</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.manholeStats?.totalCount || 0 }}</div>
            <div class="stat-label">井盖总数</div>
          </div>
        </div>
        <div class="stat-card normal">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.manholeStats?.normalCount || 0 }}</div>
            <div class="stat-label">正常</div>
          </div>
        </div>
        <div class="stat-card damaged">
          <div class="stat-icon">⚠️</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.manholeStats?.damagedCount || 0 }}</div>
            <div class="stat-label">损坏</div>
          </div>
        </div>
        <div class="stat-card repairing">
          <div class="stat-icon">🔧</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.manholeStats?.repairingCount || 0 }}</div>
            <div class="stat-label">维修中</div>
          </div>
        </div>
        <div class="stat-card detection">
          <div class="stat-icon">🔍</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.detectionStats?.todayCount || 0 }}</div>
            <div class="stat-label">今日检测</div>
          </div>
        </div>
        <div class="stat-card defect">
          <div class="stat-icon">🐛</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.detectionStats?.todayDefectCount || 0 }}</div>
            <div class="stat-label">今日缺陷</div>
          </div>
        </div>
        <div class="stat-card drone">
          <div class="stat-icon">🚁</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.droneStats?.onlineCount || 0 }}/{{ stats?.droneStats?.totalCount || 0 }}</div>
            <div class="stat-label">无人机在线</div>
          </div>
        </div>
        <div class="stat-card repair">
          <div class="stat-icon">📋</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.repairStats?.pendingCount || 0 }}</div>
            <div class="stat-label">待处理任务</div>
          </div>
        </div>
      </div>

      <!-- 图表区域 - 45% -->
      <div class="section charts-section">
        <div class="chart-box">
          <div class="box-title">
            <span class="title-icon"></span>
            <span>井盖状态分布</span>
          </div>
          <div ref="statusChart" class="chart-container"></div>
        </div>
        <div class="chart-box center">
          <div class="box-title">
            <span class="title-icon"></span>
            <span>近7天检测趋势</span>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </div>
        <div class="chart-box">
          <div class="box-title">
            <span class="title-icon"></span>
            <span>缺陷类型分布</span>
          </div>
          <div ref="defectChart" class="chart-container"></div>
        </div>
      </div>

      <!-- 底部区域 - 40% -->
      <div class="section bottom-section">
        <div class="info-box">
          <div class="box-title">
            <span class="title-icon"></span>
            <span>井盖类型分布</span>
          </div>
          <div ref="typeChart" class="chart-container"></div>
        </div>
        <div class="info-box kpi-box">
          <div class="kpi-item">
            <div class="kpi-label">设备完好率</div>
            <div class="kpi-value">{{ stats?.manholeStats?.normalRate || 0 }}%</div>
            <div class="kpi-bar">
              <div class="bar-fill" :style="{ width: (stats?.manholeStats?.normalRate || 0) + '%' }"></div>
            </div>
          </div>
          <div class="kpi-item">
            <div class="kpi-label">维修完成率</div>
            <div class="kpi-value">{{ stats?.repairStats?.completionRate || 0 }}%</div>
            <div class="kpi-bar">
              <div class="bar-fill" :style="{ width: (stats?.repairStats?.completionRate || 0) + '%' }"></div>
            </div>
          </div>
          <div class="kpi-item">
            <div class="kpi-label">无人机在线率</div>
            <div class="kpi-value">{{ stats?.droneStats?.onlineRate || 0 }}%</div>
            <div class="kpi-bar">
              <div class="bar-fill" :style="{ width: (stats?.droneStats?.onlineRate || 0) + '%' }"></div>
            </div>
          </div>
          <div class="kpi-item">
            <div class="kpi-label">累计检测</div>
            <div class="kpi-value">{{ stats?.detectionStats?.totalCount || 0 }}</div>
            <div class="kpi-sub">本周: {{ stats?.detectionStats?.weekCount || 0 }}</div>
          </div>
        </div>
        <div class="info-box list-box">
          <div class="box-title">
            <span class="title-icon"></span>
            <span>实时检测记录</span>
          </div>
          <div class="list-container">
            <div class="list-header">
              <span>井盖编号</span>
              <span>时间</span>
              <span>缺陷</span>
              <span>状态</span>
            </div>
            <div class="list-body">
              <div v-for="(item, index) in recentDetections" :key="index" class="list-row">
                <span class="manhole-id">{{ item.manholeId }}</span>
                <span class="time">{{ formatTime(item.detectionTime) }}</span>
                <span class="defect" :class="item.defectCount > 0 ? 'has-defect' : 'no-defect'">
                  {{ item.defectCount }}
                </span>
                <span class="status" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="info-box repair-box">
          <div class="box-title">
            <span class="title-icon"></span>
            <span>维修任务</span>
          </div>
          <div class="repair-list">
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
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api/dashboard'
import type { DashboardStats, RecentDetection } from '@/types/dashboard'

const stats = ref<DashboardStats | null>(null)

const statusChart = ref<HTMLDivElement>()
const typeChart = ref<HTMLDivElement>()
const trendChart = ref<HTMLDivElement>()
const defectChart = ref<HTMLDivElement>()

let statusChartInstance: echarts.ECharts | null = null
let typeChartInstance: echarts.ECharts | null = null
let trendChartInstance: echarts.ECharts | null = null
let defectChartInstance: echarts.ECharts | null = null

const recentDetections = computed<RecentDetection[]>(() => {
  if (stats.value?.recentDetections?.length) {
    return stats.value.recentDetections.slice(0, 5)
  }
  return [
    { manholeId: 'MH001', detectionTime: '2024-01-15 09:30:00', defectCount: 0, status: 0 },
    { manholeId: 'MH002', detectionTime: '2024-01-15 09:25:00', defectCount: 2, status: 1 },
    { manholeId: 'MH003', detectionTime: '2024-01-15 09:20:00', defectCount: 0, status: 0 },
    { manholeId: 'MH004', detectionTime: '2024-01-15 09:15:00', defectCount: 1, status: 1 },
    { manholeId: 'MH005', detectionTime: '2024-01-15 09:10:00', defectCount: 0, status: 0 }
  ] as RecentDetection[]
})

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.split(' ')[1]?.substring(0, 5) || time
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '正常', 1: '异常', 2: '维修' }
  return map[status] || '未知'
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = { 0: 'status-normal', 1: 'status-error', 2: 'status-repair' }
  return map[status] || ''
}

const loadData = async () => {
  try {
    const res = await dashboardApi.getStats()
    stats.value = res.data
    nextTick(() => initCharts())
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const initCharts = () => {
  if (!stats.value) return

  if (statusChart.value) {
    statusChartInstance?.dispose()
    statusChartInstance = echarts.init(statusChart.value)
    statusChartInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: {
        orient: 'vertical',
        right: '2%',
        top: 'center',
        textStyle: { color: '#fff', fontSize: 11 },
        itemWidth: 10,
        itemHeight: 10
      },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#0f1c3f', borderWidth: 2 },
        label: { show: false },
        data: stats.value.statusDistribution?.map(item => ({
          name: item.name,
          value: item.count,
          itemStyle: { color: item.status === 0 ? '#67c23a' : item.status === 1 ? '#f56c6c' : '#e6a23c' }
        })) || []
      }]
    })
  }

  if (typeChart.value) {
    typeChartInstance?.dispose()
    typeChartInstance = echarts.init(typeChart.value)
    typeChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '5%', bottom: '5%', top: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        data: stats.value.manholeTypeDistribution?.map(item => item.name) || [],
        axisLabel: { color: '#8c9bb3', fontSize: 10 },
        axisLine: { lineStyle: { color: '#333' } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#8c9bb3', fontSize: 10 },
        splitLine: { lineStyle: { color: '#1a2a4a' } }
      },
      series: [{
        type: 'bar',
        data: stats.value.manholeTypeDistribution?.map(item => ({
          value: item.count,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#1e3a8a' }
            ])
          }
        })) || [],
        barWidth: '50%',
        itemStyle: { borderRadius: [3, 3, 0, 0] }
      }]
    })
  }

  if (trendChart.value) {
    trendChartInstance?.dispose()
    trendChartInstance = echarts.init(trendChart.value)
    trendChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: {
        data: ['检测数', '缺陷数'],
        textStyle: { color: '#8c9bb3', fontSize: 11 },
        top: 5,
        itemWidth: 12,
        itemHeight: 8
      },
      grid: { left: '3%', right: '4%', bottom: '5%', top: '20%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: stats.value.detectionTrend?.map(item => item.date?.slice(5)) || [],
        axisLabel: { color: '#8c9bb3', fontSize: 10 },
        axisLine: { lineStyle: { color: '#333' } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#8c9bb3', fontSize: 10 },
        splitLine: { lineStyle: { color: '#1a2a4a' } }
      },
      series: [
        {
          name: '检测数',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 4,
          data: stats.value.detectionTrend?.map(item => item.detectionCount) || [],
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64,158,255,0.3)' },
              { offset: 1, color: 'rgba(64,158,255,0)' }
            ])
          },
          lineStyle: { color: '#409eff', width: 2 },
          itemStyle: { color: '#409eff' }
        },
        {
          name: '缺陷数',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 4,
          data: stats.value.detectionTrend?.map(item => item.defectCount) || [],
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(245,108,108,0.3)' },
              { offset: 1, color: 'rgba(245,108,108,0)' }
            ])
          },
          lineStyle: { color: '#f56c6c', width: 2 },
          itemStyle: { color: '#f56c6c' }
        }
      ]
    })
  }

  if (defectChart.value) {
    defectChartInstance?.dispose()
    defectChartInstance = echarts.init(defectChart.value)
    const defectData = stats.value.defectTypeDistribution?.slice().reverse() || []
    defectChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '15%', bottom: '5%', top: '5%', containLabel: true },
      xAxis: {
        type: 'value',
        axisLabel: { color: '#8c9bb3', fontSize: 9 },
        splitLine: { lineStyle: { color: '#1a2a4a' } }
      },
      yAxis: {
        type: 'category',
        data: defectData.map(item => item.name),
        axisLabel: { color: '#8c9bb3', fontSize: 10 },
        axisLine: { lineStyle: { color: '#333' } }
      },
      series: [{
        type: 'bar',
        data: defectData.map(item => ({
          value: item.count,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#e6a23c' },
              { offset: 1, color: '#d48c1f' }
            ])
          }
        })),
        barWidth: '55%',
        itemStyle: { borderRadius: [0, 3, 3, 0] },
        label: { show: true, position: 'right', color: '#fff', fontSize: 10 }
      }]
    })
  }
}

const handleResize = () => {
  statusChartInstance?.resize()
  typeChartInstance?.resize()
  trendChartInstance?.resize()
  defectChartInstance?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  statusChartInstance?.dispose()
  typeChartInstance?.dispose()
  trendChartInstance?.dispose()
  defectChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.dashboard-page {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #0a0f1e 0%, #1a1f3a 50%, #0f1629 100%);
  color: #fff;
  border-radius: 8px;
  overflow: hidden;
}

// 主体 - 占满整个容器
.dashboard-body {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px;
  gap: 12px;
}

// 通用区块样式
.section {
  width: 100%;
  display: flex;
  gap: 12px;
}

.box-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  flex-shrink: 0;

  .title-icon {
    width: 4px;
    height: 14px;
    background: linear-gradient(180deg, #409eff, #1e3a8a);
    border-radius: 2px;
  }
}

// 统计区块 - 固定高度
.stats-section {
  height: 80px;
  flex-shrink: 0;

  .stat-card {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: rgba(255,255,255,0.05);
    border: 1px solid rgba(64,158,255,0.15);
    border-radius: 6px;
    padding: 8px;

    .stat-icon {
      font-size: 20px;
    }

    .stat-value {
      font-size: 20px;
      font-weight: bold;
    }

    .stat-label {
      font-size: 11px;
      color: #8c9bb3;
    }

    &.total { border-left: 3px solid #409eff; .stat-value { color: #409eff; } }
    &.normal { border-left: 3px solid #67c23a; .stat-value { color: #67c23a; } }
    &.damaged { border-left: 3px solid #f56c6c; .stat-value { color: #f56c6c; } }
    &.repairing { border-left: 3px solid #e6a23c; .stat-value { color: #e6a23c; } }
    &.detection { border-left: 3px solid #409eff; .stat-value { color: #409eff; } }
    &.defect { border-left: 3px solid #f56c6c; .stat-value { color: #f56c6c; } }
    &.drone { border-left: 3px solid #67c23a; .stat-value { color: #67c23a; } }
    &.repair { border-left: 3px solid #e6a23c; .stat-value { color: #e6a23c; } }
  }
}

// 图表区块 - 自适应
.charts-section {
  flex: 1.2;
  min-height: 0;

  .chart-box {
    flex: 1;
    background: rgba(255,255,255,0.03);
    border: 1px solid rgba(64,158,255,0.15);
    border-radius: 6px;
    padding: 12px;
    display: flex;
    flex-direction: column;
    min-height: 0;

    &.center {
      flex: 1.5;
    }

    .chart-container {
      flex: 1;
      min-height: 0;
    }
  }
}

// 底部区块 - 自适应
.bottom-section {
  flex: 1;
  min-height: 0;

  .info-box {
    flex: 1;
    background: rgba(255,255,255,0.03);
    border: 1px solid rgba(64,158,255,0.15);
    border-radius: 6px;
    padding: 12px;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;

    .chart-container {
      flex: 1;
      min-height: 0;
    }
  }

  // KPI盒子
  .kpi-box {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    padding: 12px;

    .kpi-item {
      background: rgba(255,255,255,0.05);
      border-radius: 6px;
      padding: 12px;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .kpi-label {
        font-size: 11px;
        color: #8c9bb3;
      }

      .kpi-value {
        font-size: 18px;
        font-weight: bold;
        color: #409eff;
        margin: 4px 0;
      }

      .kpi-bar {
        height: 4px;
        background: rgba(255,255,255,0.1);
        border-radius: 2px;
        overflow: hidden;

        .bar-fill {
          height: 100%;
          background: linear-gradient(90deg, #409eff, #67c23a);
          border-radius: 2px;
        }
      }

      .kpi-sub {
        font-size: 10px;
        color: #8c9bb3;
      }
    }
  }

  // 列表盒子
  .list-box {
    flex: 1.2;

    .list-container {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .list-header {
        display: grid;
        grid-template-columns: 1.2fr 0.9fr 0.6fr 0.7fr;
        gap: 8px;
        padding: 8px;
        background: rgba(64,158,255,0.1);
        border-radius: 4px;
        font-size: 11px;
        color: #8c9bb3;
        text-align: center;
        flex-shrink: 0;
      }

      .list-body {
        flex: 1;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        justify-content: space-around;

        .list-row {
          display: grid;
          grid-template-columns: 1.2fr 0.9fr 0.6fr 0.7fr;
          gap: 8px;
          padding: 6px 8px;
          font-size: 12px;
          text-align: center;
          align-items: center;
          border-bottom: 1px solid rgba(255,255,255,0.05);

          &:last-child {
            border-bottom: none;
          }

          .manhole-id {
            color: #409eff;
            font-weight: 500;
          }

          .time {
            color: #8c9bb3;
          }

          .defect {
            font-weight: bold;
            &.has-defect { color: #f56c6c; }
            &.no-defect { color: #67c23a; }
          }

          .status {
            padding: 2px 8px;
            border-radius: 10px;
            font-size: 11px;

            &.status-normal {
              background: rgba(103,194,58,0.2);
              color: #67c23a;
            }

            &.status-error {
              background: rgba(245,108,108,0.2);
              color: #f56c6c;
            }

            &.status-repair {
              background: rgba(230,162,60,0.2);
              color: #e6a23c;
            }
          }
        }
      }
    }
  }

  // 维修盒子
  .repair-box {
    flex: 0.8;

    .repair-list {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;

      .repair-item {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 12px;
        background: rgba(255,255,255,0.05);
        border-radius: 6px;
        padding: 0 12px;

        .repair-icon {
          font-size: 20px;
        }

        .repair-value {
          font-size: 18px;
          font-weight: bold;
        }

        .repair-label {
          font-size: 11px;
          color: #8c9bb3;
        }

        &.pending { border-left: 3px solid #e6a23c; .repair-value { color: #e6a23c; } }
        &.progress { border-left: 3px solid #409eff; .repair-value { color: #409eff; } }
        &.completed { border-left: 3px solid #67c23a; .repair-value { color: #67c23a; } }
      }
    }
  }
}
</style>
