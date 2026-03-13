<template>
  <div class="detection-page page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><VideoCamera /></el-icon>
        <div class="title-content">
          <h1>AI 智能检测</h1>
          <p>上传图片自动识别井盖缺陷</p>
        </div>
      </div>
    </div>

    <!-- 步骤条 -->
    <div class="steps-wrapper">
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="上传图片" description="选择要检测的图片" />
        <el-step title="确认信息" description="核对井盖信息" />
        <el-step title="开始检测" description="设置参数并检测" />
        <el-step title="查看结果" description="获取检测结果" />
      </el-steps>
    </div>

    <!-- 步骤内容 -->
    <div class="step-content">
      <!-- 步骤1：上传图片 -->
      <transition name="fade" mode="out-in">
        <div v-if="currentStep === 0" key="step0" class="step-panel">
          <el-card class="upload-card">
            <div class="upload-wrapper">
              <el-upload
                ref="uploadRef"
                class="upload-area"
                drag
                :auto-upload="false"
                :on-change="handleFileChange"
                accept=".jpg,.jpeg,.png"
              >
                <el-icon class="el-icon--upload" :size="80"><upload-filled /></el-icon>
                <div class="upload-title">点击或拖拽上传图片</div>
                <div class="upload-tip">
                  请将图片命名为井盖ID，如：<code>MH001.jpg</code>
                </div>
              </el-upload>
            </div>
          </el-card>
        </div>

        <!-- 步骤2：确认井盖信息 -->
        <div v-else-if="currentStep === 1" key="step1" class="step-panel">
          <el-row :gutter="24">
            <el-col :span="14">
              <el-card>
                <template #header>
                  <span>上传的图片</span>
                </template>
                <div class="preview-wrapper">
                  <el-image
                    v-if="previewUrl"
                    :src="previewUrl"
                    fit="contain"
                    class="preview-image"
                  />
                </div>
              </el-card>
            </el-col>
            <el-col :span="10">
              <el-card :class="{ 'error-card': !manholeInfo }">
                <template #header>
                  <div class="card-header">
                    <span>井盖信息</span>
                    <el-tag v-if="manholeInfo" type="success" effect="dark">已匹配</el-tag>
                    <el-tag v-else type="danger" effect="dark">未找到</el-tag>
                  </div>
                </template>

                <div v-if="loadingManhole" class="loading-wrapper">
                  <el-skeleton :rows="4" animated />
                </div>

                <div v-else-if="manholeInfo" class="manhole-info-wrapper">
                  <div class="info-item">
                    <label>井盖编号</label>
                    <div class="info-value">{{ manholeInfo.manholeId }}</div>
                  </div>
                  <div class="info-item">
                    <label>详细地址</label>
                    <div class="info-value">{{ manholeInfo.address || '暂无地址' }}</div>
                  </div>
                  <div class="info-item">
                    <label>经纬度</label>
                    <div class="info-value coords">
                      {{ manholeInfo.longitude?.toFixed(6) }}, {{ manholeInfo.latitude?.toFixed(6) }}
                    </div>
                  </div>
                  <div class="info-item">
                    <label>所在区域</label>
                    <div class="info-value">{{ formatLocation(manholeInfo) }}</div>
                  </div>
                </div>

                <div v-else class="error-wrapper">
                  <el-empty description="未找到井盖信息">
                    <template #description>
                      <div class="error-text">
                        <p>文件名 "{{ extractedManholeId }}" 未找到对应井盖</p>
                        <p class="sub-text">请先录入井盖信息后再进行检测</p>
                      </div>
                    </template>
                    <el-button type="primary" @click="goBack">
                      <el-icon><ArrowLeft /></el-icon>
                      重新上传
                    </el-button>
                  </el-empty>
                </div>

                <div v-if="manholeInfo" class="step-actions">
                  <el-button @click="goBack">
                    <el-icon><ArrowLeft /></el-icon>
                    返回
                  </el-button>
                  <el-button type="primary" @click="nextStep">
                    确认并下一步
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 步骤3：设置参数并开始检测 -->
        <div v-else-if="currentStep === 2" key="step2" class="step-panel">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>待检测图片</span>
                </template>
                <div class="preview-wrapper small">
                  <el-image
                    v-if="previewUrl"
                    :src="previewUrl"
                    fit="contain"
                    class="preview-image"
                  />
                </div>
              </el-card>

              <el-card style="margin-top: 20px">
                <template #header>
                  <span>井盖信息确认</span>
                </template>
                <el-descriptions :column="1" size="small" border>
                  <el-descriptions-item label="编号">{{ manholeInfo?.manholeId }}</el-descriptions-item>
                  <el-descriptions-item label="地址">{{ manholeInfo?.address }}</el-descriptions-item>
                  <el-descriptions-item label="坐标">
                    {{ manholeInfo?.longitude?.toFixed(6) }}, {{ manholeInfo?.latitude?.toFixed(6) }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>

            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>检测参数设置</span>
                </template>

                <div class="params-form">
                  <div class="param-item">
                    <div class="param-label">
                      <span>置信度阈值</span>
                      <el-tooltip content="置信度低于此值的检测结果将被过滤">
                        <el-icon><InfoFilled /></el-icon>
                      </el-tooltip>
                    </div>
                    <el-slider
                      v-model="params.confThreshold"
                      :min="0.1"
                      :max="1"
                      :step="0.1"
                      show-stops
                      show-input
                    />
                  </div>

                  <div class="param-item">
                    <div class="param-label">
                      <span>IoU阈值</span>
                      <el-tooltip content="交并比阈值，用于合并重叠检测框">
                        <el-icon><InfoFilled /></el-icon>
                      </el-tooltip>
                    </div>
                    <el-slider
                      v-model="params.iouThreshold"
                      :min="0.1"
                      :max="1"
                      :step="0.1"
                      show-stops
                      show-input
                    />
                  </div>
                </div>

                <div class="detection-actions">
                  <el-button size="large" @click="prevStep">
                    <el-icon><ArrowLeft /></el-icon>
                    上一步
                  </el-button>
                  <el-button
                    type="primary"
                    size="large"
                    :loading="detecting"
                    @click="startDetection"
                  >
                    <el-icon><VideoCamera /></el-icon>
                    {{ detecting ? '检测中...' : '开始检测' }}
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 步骤4：查看结果 -->
        <div v-else-if="currentStep === 3" key="step3" class="step-panel">
          <div v-if="!result" class="loading-result">
            <el-skeleton :rows="10" animated />
          </div>

          <div v-else class="result-wrapper">
            <el-row :gutter="24">
              <el-col :span="16">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>检测结果对比</span>
                      <el-tag v-if="result.defectCount > 0" type="danger" effect="dark">
                        发现 {{ result.defectCount }} 个缺陷
                      </el-tag>
                      <el-tag v-else type="success" effect="dark">未发现缺陷</el-tag>
                    </div>
                  </template>

                  <div class="result-images">
                    <div class="image-compare">
                      <div class="image-box">
                        <div class="image-label">原始图片</div>
                        <el-image :src="getFullImageUrl(result.originalUrl)" fit="contain" />
                      </div>
                      <div class="image-box">
                        <div class="image-label">检测结果</div>
                        <el-image :src="getFullImageUrl(result.resultUrl)" fit="contain" />
                      </div>
                    </div>
                  </div>
                </el-card>

                <el-card v-if="result.defects && result.defects.length > 0" style="margin-top: 20px">
                  <template #header>
                    <span>缺陷详情</span>
                  </template>
                  <el-table :data="result.defects" border stripe>
                    <el-table-column type="index" width="50" />
                    <el-table-column prop="type" label="缺陷类型" width="150" />
                    <el-table-column prop="confidence" label="置信度">
                      <template #default="{ row }">
                        <el-progress
                          :percentage="Math.round(row.confidence * 100)"
                          :status="row.confidence > 0.8 ? 'success' : ''"
                        />
                      </template>
                    </el-table-column>
                  </el-table>
                </el-card>
              </el-col>

              <el-col :span="8">
                <el-card>
                  <template #header>
                    <span>检测信息</span>
                  </template>

                  <div class="result-stats">
                    <div class="stat-item">
                      <div class="stat-label">处理时间</div>
                      <div class="stat-value">{{ result.processTime }}ms</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-label">缺陷数量</div>
                      <div class="stat-value" :class="{ 'text-danger': result.defectCount > 0 }">
                        {{ result.defectCount }}
                      </div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-label">置信度阈值</div>
                      <div class="stat-value">{{ params.confThreshold }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-label">IoU阈值</div>
                      <div class="stat-value">{{ params.iouThreshold }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-label">井盖编号</div>
                      <div class="stat-value">{{ manholeInfo?.manholeId }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-label">检测时间</div>
                      <div class="stat-value">{{ new Date().toLocaleString() }}</div>
                    </div>
                    <div v-if="saveTime" class="stat-item">
                      <div class="stat-label">保存时间</div>
                      <div class="stat-value text-success">{{ saveTime }}</div>
                    </div>
                  </div>

                  <div class="result-actions-vertical">
                    <el-button
                      type="primary"
                      :loading="saving"
                      :disabled="saved"
                      @click="saveResult"
                    >
                      <el-icon v-if="!saved"><Check /></el-icon>
                      <el-icon v-else><CircleCheck /></el-icon>
                      {{ saved ? '已保存' : '保存结果' }}
                    </el-button>
                    <el-button type="success" @click="reset">
                      <el-icon><RefreshRight /></el-icon>
                      继续检测
                    </el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import {
  UploadFilled,
  VideoCamera,
  ArrowLeft,
  ArrowRight,
  InfoFilled,
  Check,
  CircleCheck,
  RefreshRight
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { detectionApi } from '@/api/detection'
import { manholeApi } from '@/api/manhole'
import type { DetectionResult } from '@/types/detection'
import type { Manhole } from '@/types/manhole'
import type { UploadFile, UploadInstance } from 'element-plus'

// 当前步骤
const currentStep = ref(0)

// 文件上传
const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | null>(null)
const previewUrl = ref<string>('')

// 井盖信息
const extractedManholeId = ref<string>('')
const manholeInfo = ref<Manhole | null>(null)
const loadingManhole = ref(false)

// 检测
const detecting = ref(false)
const result = ref<DetectionResult | null>(null)
const saving = ref(false)
const saved = ref(false)
const saveTime = ref<string>('')

const params = reactive({
  confThreshold: 0.7,
  iouThreshold: 0.5
})

// 步骤控制
const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const goBack = () => {
  currentStep.value = 0
  reset()
}

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

// 从文件名提取井盖ID
const extractManholeIdFromFileName = (fileName: string): string => {
  const baseName = fileName.replace(/\.[^/.]+$/, '')
  return baseName.trim()
}

// 格式化位置信息
const formatLocation = (manhole: Manhole): string => {
  const parts = [manhole.province, manhole.city, manhole.district].filter(Boolean)
  return parts.length > 0 ? parts.join(' / ') : '未知位置'
}

const handleFileChange = async (uploadFile: UploadFile) => {
  if (!uploadFile.raw) return

  // 保存文件
  selectedFile.value = uploadFile.raw

  // 生成预览URL
  previewUrl.value = URL.createObjectURL(uploadFile.raw)

  // 提取井盖ID
  const manholeId = extractManholeIdFromFileName(uploadFile.name)
  extractedManholeId.value = manholeId

  // 进入步骤2
  currentStep.value = 1

  // 查询井盖信息
  await queryManholeInfo(manholeId)
}

// 查询井盖信息
const queryManholeInfo = async (manholeId: string) => {
  loadingManhole.value = true
  try {
    const res = await manholeApi.getByManholeId(manholeId)
    if (res.data) {
      manholeInfo.value = res.data
      ElMessage.success(`已匹配井盖: ${manholeId}`)
    }
  } catch (error) {
    manholeInfo.value = null
    ElMessage.error(`未找到井盖: ${manholeId}`)
  } finally {
    loadingManhole.value = false
  }
}

// 开始检测
const startDetection = async () => {
  console.log('开始检测...')
  console.log('selectedFile:', selectedFile.value)
  console.log('manholeInfo:', manholeInfo.value)

  if (!selectedFile.value) {
    ElMessage.warning('请先上传图片')
    return
  }

  if (!manholeInfo.value) {
    ElMessage.warning('未找到井盖信息')
    return
  }

  if (!manholeInfo.value.manholeId) {
    ElMessage.warning('井盖编号缺失')
    return
  }

  detecting.value = true
  currentStep.value = 3

  try {
    console.log('调用检测API...')
    console.log('参数:', {
      file: selectedFile.value.name,
      confThreshold: params.confThreshold,
      iouThreshold: params.iouThreshold,
      manholeId: manholeInfo.value.manholeId
    })

    // 使用正确的API方法
    const res = await detectionApi.detectImage(
      selectedFile.value,
      params.confThreshold,
      params.iouThreshold,
      manholeInfo.value.manholeId
    )

    console.log('检测响应:', res)
    result.value = res.data

    // 检测完成，等待用户手动保存
    ElMessage.success('检测完成，请确认保存结果')
  } catch (error: any) {
    console.error('检测失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '检测失败'
    ElMessage.error(errorMsg)
    currentStep.value = 2
  } finally {
    detecting.value = false
  }
}

const reset = () => {
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  previewUrl.value = ''
  result.value = null
  manholeInfo.value = null
  extractedManholeId.value = ''
  currentStep.value = 0
  params.confThreshold = 0.7
  params.iouThreshold = 0.5
  saving.value = false
  saved.value = false
  saveTime.value = ''
}

// 保存检测结果到数据库
const saveResult = async () => {
  if (!result.value || !manholeInfo.value) {
    ElMessage.warning('没有可保存的结果')
    return
  }

  saving.value = true
  try {
    const res = await detectionApi.saveDetectionResult(
      manholeInfo.value.manholeId,
      result.value
    )

    if (res.code === 200) {
      const now = new Date()
      saved.value = true
      saveTime.value = now.toLocaleString()
      ElMessage.success('检测结果已保存到数据库')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '保存失败'
    ElMessage.error(errorMsg)
  } finally {
    saving.value = false
  }
}

</script>

<style scoped lang="scss">
.detection-page {
  display: flex;
  flex-direction: column;
}

.steps-wrapper {
  margin-bottom: 24px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.step-content {
  flex: 1;
  min-height: 0;
}

.step-panel {
  height: 100%;
}

// 步骤1：上传
.upload-card {
  max-width: 600px;
  margin: 40px auto;

  :deep(.el-card__body) {
    padding: 40px;
  }
}

.upload-wrapper {
  text-align: center;
}

.upload-area {
  :deep(.el-upload-dragger) {
    width: 100%;
    height: 320px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    border: 2px dashed var(--border-color);
    border-radius: var(--radius-lg);
    transition: all 0.3s;

    &:hover {
      border-color: var(--primary-color);
      background: rgba(64, 158, 255, 0.02);
    }
  }
}

.upload-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
  margin-top: 16px;
}

.upload-tip {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 12px;

  code {
    background: var(--bg-primary);
    padding: 2px 8px;
    border-radius: 4px;
    color: var(--primary-color);
  }
}

// 步骤2：确认信息
.preview-wrapper {
  height: 400px;
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  &.small {
    height: 200px;
  }

  .preview-image {
    max-width: 100%;
    max-height: 100%;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-wrapper {
  padding: 20px;
}

.manhole-info-wrapper {
  .info-item {
    padding: 16px 0;
    border-bottom: 1px solid var(--border-light);

    &:last-child {
      border-bottom: none;
    }

    label {
      font-size: 12px;
      color: var(--text-muted);
      margin-bottom: 6px;
      display: block;
    }

    .info-value {
      font-size: 15px;
      color: var(--text-primary);
      font-weight: 500;

      &.coords {
        font-family: monospace;
        color: var(--primary-color);
      }
    }
  }
}

.error-wrapper {
  padding: 30px 0;

  .error-text {
    text-align: center;

    p {
      color: var(--text-secondary);
      margin-bottom: 8px;
    }

    .sub-text {
      font-size: 13px;
      color: var(--text-muted);
    }
  }
}

.error-card {
  :deep(.el-card__header) {
    background: #fef0f0;
  }
}

.step-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
}

// 步骤3：参数设置
.params-form {
  padding: 10px 0;

  .param-item {
    margin-bottom: 30px;

    .param-label {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 12px;
      font-weight: 500;
      color: var(--text-primary);

      .el-icon {
        color: var(--text-muted);
        cursor: help;
      }
    }
  }
}

.detection-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
}

// 步骤4：结果
.loading-result {
  padding: 40px;
}

.result-wrapper {
  height: 100%;
}

.image-compare {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  .image-box {
    border: 1px solid var(--border-light);
    border-radius: var(--radius-md);
    overflow: hidden;

    .image-label {
      padding: 10px 16px;
      background: var(--bg-primary);
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
      border-bottom: 1px solid var(--border-light);
    }

    .el-image {
      width: 100%;
      height: 300px;
    }
  }
}

.result-stats {
  .stat-item {
    padding: 16px 0;
    border-bottom: 1px solid var(--border-light);

    &:last-child {
      border-bottom: none;
    }

    .stat-label {
      font-size: 12px;
      color: var(--text-muted);
      margin-bottom: 6px;
    }

    .stat-value {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);

      &.text-danger {
        color: #f56c6c;
      }

      &.text-success {
        color: #67c23a;
      }
    }
  }
}

.result-actions-vertical {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .el-button {
    justify-content: center;
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

// 响应式
@media (max-width: 1200px) {
  .detection-page {
    padding: 20px 24px;
  }

  .image-compare {
    grid-template-columns: 1fr;

    .image-box .el-image {
      height: 200px;
    }
  }
}

@media (max-width: 768px) {
  .detection-page {
    padding: 16px 20px;
  }

  .upload-card {
    margin: 20px auto;

    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .upload-area :deep(.el-upload-dragger) {
    height: 240px;
  }

  .preview-wrapper {
    height: 250px;
  }
}
</style>
