<template>
  <div class="detail-page page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <div class="header-title">
          <h1>{{ manholeInfo?.manholeId || manholeId }}</h1>
          <p>井盖详细信息与检测记录</p>
        </div>
      </div>
      <div class="header-actions">
        <el-tag :type="statusType" size="large" effect="dark">
          {{ manholeInfo?.statusText || '正常' }}
        </el-tag>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="detail-content">
      <!-- 左侧：井盖图片 -->
      <div class="image-section">
        <el-card>
          <template #header>
            <span>井盖图片</span>
          </template>
          <div class="image-wrapper">
            <el-image
              :src="getFullImageUrl(manholeInfo?.imgUrl)"
              fit="cover"
              class="manhole-image"
              :preview-src-list="[getFullImageUrl(manholeInfo?.imgUrl)]"
            >
              <template #error>
                <div class="image-error">
                  <el-icon size="48"><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>
              </template>
            </el-image>
          </div>
        </el-card>

        <!-- 快速统计 -->
        <el-card style="margin-top: 16px">
          <div class="quick-stats">
            <div class="stat-item">
              <div class="stat-value">{{ detectionList.length }}</div>
              <div class="stat-label">检测次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ defectCount }}</div>
              <div class="stat-label">缺陷记录</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ manholeInfo?.safetyScore || '--' }}</div>
              <div class="stat-label">安全评分</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：详细信息和检测记录 -->
      <div class="info-section">
        <!-- 基本信息 -->
        <el-card>
          <template #header>
            <span>基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="井盖编号">{{ manholeInfo?.manholeId || manholeId }}</el-descriptions-item>
            <el-descriptions-item label="井盖类型">{{ manholeInfo?.manholeTypeText || '--' }}</el-descriptions-item>
            <el-descriptions-item label="所在省份">{{ manholeInfo?.province || '--' }}</el-descriptions-item>
            <el-descriptions-item label="所在城市">{{ manholeInfo?.city || '--' }}</el-descriptions-item>
            <el-descriptions-item label="所在区县">{{ manholeInfo?.district || '--' }}</el-descriptions-item>
            <el-descriptions-item label="详细地址">{{ manholeInfo?.address || '--' }}</el-descriptions-item>
            <el-descriptions-item label="纬度">{{ formatCoordinate(manholeInfo?.latitude) }}</el-descriptions-item>
            <el-descriptions-item label="经度">{{ formatCoordinate(manholeInfo?.longitude) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(manholeInfo?.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDate(manholeInfo?.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="下次检测时间" :span="2">
              <el-tag v-if="isOverdue(manholeInfo?.nextDetTime)" type="danger" effect="dark">已逾期</el-tag>
              <el-tag v-else-if="isUpcoming(manholeInfo?.nextDetTime)" type="warning">{{ getDaysLeft(manholeInfo?.nextDetTime) }}天后</el-tag>
              <span v-else>{{ formatDate(manholeInfo?.nextDetTime) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 检测记录 -->
        <el-card style="margin-top: 16px">
          <template #header>
            <div class="section-header">
              <span>检测记录</span>
              <el-radio-group v-model="detectionFilter" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="defect">有缺陷</el-radio-button>
                <el-radio-button label="normal">正常</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div v-if="loading" class="loading-wrapper">
            <el-skeleton :rows="5" animated />
          </div>

          <div v-else-if="filteredDetectionList.length === 0" class="empty-wrapper">
            <el-empty description="暂无检测记录" />
          </div>

          <div v-else class="detection-list">
            <div
              v-for="item in filteredDetectionList"
              :key="item.id"
              class="detection-item"
              :class="{ 'has-defect': item.hasDefect }"
              @click="showDetectionDetail(item)"
            >
              <div class="detection-main">
                <div class="detection-info">
                  <div class="detection-no">{{ item.detectionNo }}</div>
                  <div class="detection-time">{{ formatDateTime(item.detectionTime) }}</div>
                </div>
                <div class="detection-result">
                  <el-tag :type="item.hasDefect ? 'danger' : 'success'" size="small">
                    {{ item.hasDefect ? `${item.defectCount}个缺陷` : '无缺陷' }}
                  </el-tag>
                  <div v-if="item.defectTypes" class="defect-types">{{ item.defectTypes }}</div>
                </div>
              </div>
              <div class="detection-images" v-if="item.originalImgUrl || item.resultImgUrl">
                <el-image
                  :src="getFullImageUrl(item.resultImgUrl || item.originalImgUrl)"
                  fit="cover"
                  class="result-thumb"
                  :preview-src-list="[getFullImageUrl(item.resultImgUrl || item.originalImgUrl)]"
                />
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="filteredDetectionList.length > 0">
            <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20]"
              :total="filteredDetectionList.length"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </div>
    </div>

    <!-- 检测详情弹窗 -->
    <el-dialog v-model="detailVisible" title="检测详情" width="800px">
      <div v-if="currentDetection" class="detection-detail">
        <div class="detail-images">
          <div class="image-box">
            <div class="image-label">原始图片</div>
            <el-image :src="getFullImageUrl(currentDetection.originalImgUrl)" fit="contain" />
          </div>
          <div class="image-box">
            <div class="image-label">检测结果</div>
            <el-image :src="getFullImageUrl(currentDetection.resultImgUrl)" fit="contain" />
          </div>
        </div>
        <el-descriptions :column="2" border style="margin-top: 16px">
          <el-descriptions-item label="检测编号">{{ currentDetection.detectionNo }}</el-descriptions-item>
          <el-descriptions-item label="检测时间">{{ formatDateTime(currentDetection.detectionTime) }}</el-descriptions-item>
          <el-descriptions-item label="检测结果">{{ currentDetection.hasDefect ? '有缺陷' : '正常' }}</el-descriptions-item>
          <el-descriptions-item label="缺陷数量">{{ currentDetection.defectCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="缺陷类型" :span="2">{{ currentDetection.defectTypes || '--' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { manholeApi } from '@/api/manhole'
import { detectionApi } from '@/api/detection'
import type { Manhole } from '@/types/manhole'
import type { Detection } from '@/types/detection'

const route = useRoute()
const manholeId = ref(route.params.id as string)

// 默认占位图片
const defaultImage = 'https://via.placeholder.com/400x400?text=No+Image'

// 获取完整图片 URL
const getFullImageUrl = (url?: string) => {
  if (!url) return defaultImage
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

// 井盖信息
const manholeInfo = ref<Manhole | null>(null)
const loading = ref(false)

// 检测记录
const detectionList = ref<Detection[]>([])
const detectionFilter = ref('all')
const pageNum = ref(1)
const pageSize = ref(10)

// 详情弹窗
const detailVisible = ref(false)
const currentDetection = ref<Detection | null>(null)

// 默认图片
const defaultImage = 'https://via.placeholder.com/400x400?text=No+Image'

// 状态类型
const statusType = computed(() => {
  const status = manholeInfo.value?.status
  switch (status) {
    case 0: return 'success'
    case 1: return 'danger'
    case 2: return 'warning'
    default: return 'info'
  }
})

// 缺陷数量统计
const defectCount = computed(() => {
  return detectionList.value.filter(d => d.hasDefect).length
})

// 过滤后的检测记录
const filteredDetectionList = computed(() => {
  let list = detectionList.value
  if (detectionFilter.value === 'defect') {
    list = list.filter(d => d.hasDefect)
  } else if (detectionFilter.value === 'normal') {
    list = list.filter(d => !d.hasDefect)
  }
  return list
})

// 格式化坐标
const formatCoordinate = (val?: number) => {
  return val?.toFixed(6) || '--'
}

// 格式化日期
const formatDate = (date?: string | Date) => {
  if (!date) return '--'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 格式化日期时间
const formatDateTime = (date?: string | Date) => {
  if (!date) return '--'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 判断是否逾期
const isOverdue = (date?: string | Date) => {
  if (!date) return false
  return new Date(date) < new Date()
}

// 判断是否即将到期（7天内）
const isUpcoming = (date?: string | Date) => {
  if (!date) return false
  const days = getDaysLeft(date)
  return days > 0 && days <= 7
}

// 获取剩余天数
const getDaysLeft = (date?: string | Date) => {
  if (!date) return 0
  const diff = new Date(date).getTime() - new Date().getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

// 显示检测详情
const showDetectionDetail = (item: Detection) => {
  currentDetection.value = item
  detailVisible.value = true
}

// 分页变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  pageNum.value = 1
}

const handleCurrentChange = (val: number) => {
  pageNum.value = val
}

// 获取井盖信息
const fetchManholeInfo = async () => {
  try {
    const res = await manholeApi.getByManholeId(manholeId.value)
    manholeInfo.value = res.data
  } catch (error) {
    ElMessage.error('获取井盖信息失败')
  }
}

// 获取检测记录
const fetchDetectionList = async () => {
  loading.value = true
  try {
    const res = await detectionApi.getByManholeId(manholeId.value)
    detectionList.value = res.data || []
    // 调试输出
    console.log('检测记录:', detectionList.value)
    detectionList.value.forEach((item, index) => {
      console.log(`记录${index}:`, item.detectionNo, 'resultImgUrl:', item.resultImgUrl, 'originalImgUrl:', item.originalImgUrl)
    })
  } catch (error) {
    ElMessage.error('获取检测记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchManholeInfo()
  fetchDetectionList()
})
</script>

<style scoped lang="scss">
.detail-page {
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-title {
      h1 {
        font-size: 20px;
        font-weight: 600;
        margin: 0 0 4px;
      }
      p {
        font-size: 13px;
        color: var(--text-muted);
        margin: 0;
      }
    }
  }
}

.detail-content {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
}

.image-section {
  .image-wrapper {
    height: 320px;
    background: var(--bg-primary);
    border-radius: 8px;
    overflow: hidden;

    .manhole-image {
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
      background: #f5f7fa;

      span {
        font-size: 14px;
      }
    }
  }
}

.quick-stats {
  display: flex;
  justify-content: space-around;
  padding: 8px 0;

  .stat-item {
    text-align: center;

    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: var(--primary-color);
    }

    .stat-label {
      font-size: 12px;
      color: var(--text-muted);
      margin-top: 4px;
    }
  }
}

.info-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.detection-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .detection-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: var(--bg-primary);
    border-radius: 8px;
    border: 1px solid var(--border-light);
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: var(--primary-color);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    &.has-defect {
      border-left: 4px solid #f56c6c;
    }

    .detection-main {
      flex: 1;

      .detection-info {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;

        .detection-no {
          font-weight: 600;
          color: var(--text-primary);
        }

        .detection-time {
          font-size: 13px;
          color: var(--text-muted);
        }
      }

      .detection-result {
        display: flex;
        align-items: center;
        gap: 12px;

        .defect-types {
          font-size: 13px;
          color: var(--text-secondary);
        }
      }
    }

    .detection-images {
      .result-thumb {
        width: 80px;
        height: 60px;
        border-radius: 4px;
        object-fit: cover;
      }
    }
  }
}

.loading-wrapper, .empty-wrapper {
  padding: 40px 0;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detection-detail {
  .detail-images {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;

    .image-box {
      border: 1px solid var(--border-light);
      border-radius: 8px;
      overflow: hidden;

      .image-label {
        padding: 8px 12px;
        background: var(--bg-primary);
        font-size: 13px;
        color: var(--text-secondary);
        border-bottom: 1px solid var(--border-light);
      }

      .el-image {
        width: 100%;
        height: 200px;
      }
    }
  }
}

@media (max-width: 1200px) {
  .detail-content {
    grid-template-columns: 1fr;
  }

  .image-section {
    .image-wrapper {
      height: 240px;
    }
  }
}
</style>
