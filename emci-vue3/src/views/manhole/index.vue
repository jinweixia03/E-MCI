<template>
  <div class="manhole-page page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Grid /></el-icon>
        <div class="title-content">
          <h1>井盖管理</h1>
          <p>管理井盖信息、位置及检测计划</p>
        </div>
      </div>
      <div class="header-actions">
        <!-- 新增按钮已移除 -->
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-grid cols-4">
      <div v-for="stat in overviewStats" :key="stat.label" class="stat-card">
        <div class="stat-icon" :style="{ background: stat.color }">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card" v-loading="loading">
      <div class="table-header">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索井盖编号、位置..."
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
        </div>
        <div class="table-actions">
          <el-button text @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
          <el-button text @click="handleRefresh">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </div>

      <div class="table-body">
        <el-table :data="manholeList" stripe style="width: 100%">
          <el-table-column type="index" width="60" align="center" />
          <el-table-column prop="manholeId" label="井盖编号" min-width="140">
            <template #default="{ row }">
              <div class="cell-highlight">{{ row.manholeId }}</div>
            </template>
          </el-table-column>
          <el-table-column label="位置坐标" min-width="200">
            <template #default="{ row }">
              <div class="location-cell">
                <el-icon><Location /></el-icon>
                <span>{{ formatCoordinate(row.latitude) }}, {{ formatCoordinate(row.longitude) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="nextDetTime" label="下次检测" min-width="140">
            <template #default="{ row }">
              <el-tag v-if="isOverdue(row.nextDetTime)" type="danger" effect="dark" size="small">
                已逾期
              </el-tag>
              <el-tag v-else-if="isUpcoming(row.nextDetTime)" type="warning" size="small">
                {{ getDaysLeft(row.nextDetTime) }}天
              </el-tag>
              <span v-else class="text-muted">{{ formatDate(row.nextDetTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="140">
            <template #default="{ row }">
              <span class="text-muted">{{ formatDate(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" class="detail-btn" @click="showDetail(row)">
                <el-icon><View /></el-icon>详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框已移除 -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Grid, Download, Refresh, Location, View, Warning, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { manholeApi } from '@/api/manhole'
import type { Manhole } from '@/types/manhole'

const router = useRouter()

const loading = ref(false)
const manholeList = ref<Manhole[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

// 概览统计
const overviewStats = computed(() => [
  { icon: 'Grid', value: total.value, label: '井盖总数', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { icon: 'Location', value: manholeList.value.length, label: '当前显示', color: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' },
  { icon: 'Warning', value: getOverdueCount(), label: '检测逾期', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { icon: 'Timer', value: getUpcomingCount(), label: '即将到期', color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
])

const getOverdueCount = () => {
  return manholeList.value.filter(m => isOverdue(m.nextDetTime)).length
}

const getUpcomingCount = () => {
  return manholeList.value.filter(m => isUpcoming(m.nextDetTime)).length
}

const formatCoordinate = (val?: number) => {
  return val?.toFixed(4) || '--'
}

const formatDate = (date?: string | Date) => {
  if (!date) return '--'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const isOverdue = (date?: string | Date) => {
  if (!date) return false
  return new Date(date) < new Date()
}

const isUpcoming = (date?: string | Date) => {
  if (!date) return false
  const days = getDaysLeft(date)
  return days > 0 && days <= 7
}

const getDaysLeft = (date?: string | Date) => {
  if (!date) return 0
  const diff = new Date(date).getTime() - new Date().getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await manholeApi.pageQuery({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    })
    manholeList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  pageNum.value = val
  fetchData()
}

// 刷新数据
const handleRefresh = () => {
  ElMessage.info('刷新中...')
  fetchData()
}

// 导出数据
const handleExport = () => {
  // 构建CSV内容
  const headers = ['井盖编号', '纬度', '经度', '地址', '下次检测时间', '创建时间']
  const rows = manholeList.value.map(item => [
    item.manholeId,
    item.latitude,
    item.longitude,
    item.address || '',
    item.nextDetTime || '',
    item.createTime || ''
  ])

  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
  ].join('\n')

  // 添加BOM以支持中文
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)

  link.href = url
  link.download = `井盖数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('导出成功')
}

const showDetail = (row: Manhole) => {
  router.push(`/information/detail/${row.manholeId}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
// 使用全局布局系统

// 单元格样式
.cell-highlight {
  font-weight: 600;
  color: #667eea;
}

.location-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);

  .el-icon {
    color: var(--primary-color);
  }
}

.text-muted {
  color: var(--text-muted);
}

// 详情按钮样式
.detail-btn {
  color: #fff !important;
}
</style>
