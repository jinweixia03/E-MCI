<template>
  <div class="repair-page page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Tools /></el-icon>
        <div class="title-content">
          <h1>{{ pageTitle }}</h1>
          <p>{{ pageDesc }}</p>
        </div>
      </div>
    </div>

    <!-- 流程步骤 -->
    <div class="content-section process-section">
      <el-steps :active="1" finish-status="success" simple>
        <el-step title="故障上报" description="检测发现问题" />
        <el-step title="任务分配" description="指派维修人员" />
        <el-step title="维修中" description="现场维修处理" />
        <el-step title="待确认" description="等待验收确认" />
        <el-step title="已完成" description="维修任务结束" />
      </el-steps>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid cols-5">
      <div class="stat-card pending">
        <div class="stat-icon"><el-icon><Timer /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card progress">
        <div class="stat-icon"><el-icon><Loading /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.inProgress }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      <div class="stat-card to-confirm">
        <div class="stat-icon"><el-icon><Warning /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.toConfirm }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </div>
      <div class="stat-card completed">
        <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card total">
        <div class="stat-icon"><el-icon><Document /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总计</div>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" v-loading="loading">
      <template #header>
        <div class="table-header">
          <div class="filter-tabs">
            <el-radio-group v-model="filterStatus" @change="handleFilterChange">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="0">待处理</el-radio-button>
              <el-radio-button :label="1">进行中</el-radio-button>
              <el-radio-button :label="2">待确认</el-radio-button>
              <el-radio-button :label="3">已完成</el-radio-button>
            </el-radio-group>
          </div>
          <div class="search-box">
            <el-input
              v-model="searchDetectionId"
              placeholder="关联检测ID"
              clearable
              style="width: 180px"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button text @click="resetSearch">重置</el-button>
          </div>
          <div class="table-actions">
            <el-button text @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="repairList" stripe style="width: 100%">
        <el-table-column type="index" width="60" align="center" />
        <el-table-column prop="repairId" label="关联检测ID" width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetection(row)">#{{ row.repairId }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="principal" label="负责人" min-width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="28" :icon="User" />
              <span>{{ row.principal || '未分配' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark" round>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="110">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="110">
          <template #default="{ row }">
            <span v-if="row.endTime" class="text-muted">{{ formatDate(row.endTime) }}</span>
            <span v-else class="text-placeholder">--</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isRepairer ? 350 : 290" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetailDialog(row)" style="color: #fff">
              <el-icon><View /></el-icon>查看详情
            </el-button>
            <!-- 维修员操作：开始维修 -->
            <el-button
              v-if="isRepairer && row.status === 0"
              link
              type="warning"
              @click="startRepair(row.id)"
            >
              <el-icon><Edit /></el-icon>开始维修
            </el-button>
            <!-- 维修员操作：提交完成 -->
            <el-button
              v-if="isRepairer && row.status === 1"
              link
              type="success"
              @click="showCompleteDialog(row)"
            >
              <el-icon><Check /></el-icon>提交完成
            </el-button>
            <!-- 管理员操作：确认完成 -->
            <el-button
              v-if="!isRepairer && row.status === 2 && isAdmin"
              link
              type="success"
              @click="confirmComplete(row.id)"
            >
              <el-icon><Check /></el-icon>确认完成
            </el-button>
            <!-- 管理员操作：重新分配 -->
            <el-button
              v-if="!isRepairer && row.status !== 3 && row.status !== 1 && isAdmin"
              link
              type="warning"
              @click="showReassignDialog(row)"
            >
              <el-icon><RefreshLeft /></el-icon>重新分配
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑维修任务' : '新增维修任务'" width="520px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="关联检测" prop="detectionId">
          <el-input-number v-model="form.detectionId" style="width: 100%" placeholder="请输入检测记录ID" />
        </el-form-item>
        <el-form-item label="井盖编号" prop="manholeId">
          <el-input v-model="form.manholeId" placeholder="请输入井盖编号" />
        </el-form-item>
        <el-form-item label="维修人员ID">
          <el-input-number v-model="form.repairUserId" style="width: 100%" placeholder="请输入维修人员ID" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="0">待处理</el-radio-button>
            <el-radio-button :label="1">进行中</el-radio-button>
            <el-radio-button :label="2">已完成</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="维修任务详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="维修ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="关联检测ID">{{ detailData.repairId }}</el-descriptions-item>
        <el-descriptions-item label="井盖编号">{{ detailData.manholeId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detailData.principal || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" effect="dark" round>
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="缺陷类型">{{ detailData.defectTypes || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDate(detailData.startTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ formatDate(detailData.endTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 维修前图片 -->
      <div class="detail-images" v-if="detailData?.beforeImg">
        <h4>维修前图片</h4>
        <el-image
          :src="getFullImageUrl(detailData.beforeImg)"
          :preview-src-list="[getFullImageUrl(detailData.beforeImg)]"
          fit="cover"
          style="width: 100%; max-height: 300px; border-radius: 8px;"
        />
      </div>

      <!-- 维修后图片 -->
      <div class="detail-images" v-if="detailData?.afterImg">
        <h4>维修后图片</h4>
        <el-image
          :src="getFullImageUrl(detailData.afterImg)"
          :preview-src-list="[getFullImageUrl(detailData.afterImg)]"
          fit="cover"
          style="width: 100%; max-height: 300px; border-radius: 8px;"
        />
      </div>

      <!-- 检测图片（参考用） -->
      <div class="detail-images" v-if="detailData?.detectionResultImgUrl">
        <h4>AI检测结果图</h4>
        <el-image
          :src="getFullImageUrl(detailData.detectionResultImgUrl)"
          :preview-src-list="[getFullImageUrl(detailData.detectionResultImgUrl)]"
          fit="cover"
          style="width: 100%; max-height: 300px; border-radius: 8px;"
        />
      </div>

      <template #footer>
        <div class="detail-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button
            v-if="detailData?.status === 1"
            type="primary"
            @click="completeRepair(detailData!.id); detailVisible = false"
          >
            提交完成
          </el-button>
          <el-button
            v-if="detailData?.status === 2 && isAdmin"
            type="success"
            @click="confirmComplete(detailData!.id); detailVisible = false"
          >
            确认完成
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重新分配对话框 -->
    <el-dialog v-model="reassignVisible" title="重新分配维修人员" width="400px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="选择人员">
          <el-select v-model="reassignForm.repairUserId" placeholder="请选择维修人员" style="width: 100%">
            <el-option
              v-for="user in repairUsers"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reassignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReassign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 维修员提交完成对话框 -->
    <el-dialog v-model="completeVisible" title="提交维修完成" width="600px" destroy-on-close>
      <el-form label-width="100px">
        <!-- 井盖ID提示 -->
        <el-form-item label="井盖编号">
          <div class="manhole-id-display">
            <el-tag size="large" type="info" effect="dark">{{ completeForm.manholeId || '暂无编号' }}</el-tag>
            <span class="manhole-tip">请确认上传图片文件名包含此编号：{{ completeForm.manholeId || '任意' }}</span>
          </div>
        </el-form-item>

        <el-form-item label="维修后照片" :error="imageVerifyError">
          <el-upload
            class="repair-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="completeForm.afterImg" :src="getFullImageUrl(completeForm.afterImg)" class="uploaded-image" />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>点击上传维修后照片</span>
            </div>
          </el-upload>
          <div class="upload-tip">
            <div>请上传维修完成后的井盖照片，支持 jpg/png 格式</div>
            <div v-if="uploadedFileName" class="file-verify-info" :class="{ 'verify-success': isImageVerified, 'verify-warning': !isImageVerified }">
              <el-icon v-if="isImageVerified"><CircleCheck /></el-icon>
              <el-icon v-else><Warning /></el-icon>
              <span>已上传：{{ uploadedFileName }} {{ isImageVerified ? '（井盖编号校验通过）' : '（文件名与井盖编号不一致，请确认）' }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="维修备注">
          <el-select
            v-model="completeForm.remark"
            placeholder="请选择维修情况"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="item in remarkOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <div class="remark-tip">如没有符合的选项，可选择"其他"并在备注中补充</div>
        </el-form-item>

        <!-- 自定义备注（当选择"其他"时显示） -->
        <el-form-item v-if="completeForm.remark === '其他'" label="补充说明">
          <el-input
            v-model="completeForm.customRemark"
            type="textarea"
            :rows="3"
            placeholder="请补充说明维修情况"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCompleteSubmit" :loading="completeLoading" :disabled="!isImageVerified">
          提交完成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { repairApi } from '@/api/repair'
import type { Repair } from '@/api/repair'
import { formatDate } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import {
  Plus, Tools, Timer, Loading, CircleCheck, Document,
  Download, Edit, Check, Delete, User, View, RefreshLeft, Warning, Search
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 用户角色
const isAdmin = computed(() => userStore.isAdmin)
const isRepairer = computed(() => userStore.isRepairer)
const userId = computed(() => userStore.userInfo?.id)

// 页面标题和描述
const pageTitle = computed(() => isRepairer.value ? '我的维修任务' : '维修调度')
const pageDesc = computed(() => isRepairer.value ? '查看和处理分配给我的维修任务' : '系统自动分配维修任务，管理员可调度和跟踪进度')

// 默认占位图片
const defaultImage = 'https://via.placeholder.com/400x400?text=No+Image'

// 获取完整图片 URL（模仿井盖详情页面）
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

const loading = ref(false)
const repairList = ref<Repair[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref<number | null>(null)
const searchDetectionId = ref<number | undefined>(undefined)
const stats = reactive({
  pending: 0,
  inProgress: 0,
  toConfirm: 0,
  completed: 0,
  total: 0
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentId = ref<number>()

const form = reactive({
  detectionId: undefined as number | undefined,
  manholeId: '',
  repairUserId: undefined as number | undefined,
  remark: '',
  status: 0
})

const rules: FormRules = {
  detectionId: [{ required: true, message: '请输入检测ID', trigger: 'blur' }],
  manholeId: [{ required: true, message: '请输入井盖编号', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    // 维修员只能查看自己的任务
    if (isRepairer.value && userId.value) {
      const res = await repairApi.pageQueryMyRepairs(
        userId.value,
        pageNum.value,
        pageSize.value,
        filterStatus.value ?? undefined,
        searchDetectionId.value
      )
      repairList.value = res.data.list
      total.value = res.data.total
      stats.total = res.data.total
      updateMyStats()
    } else {
      const res = await repairApi.pageQuery(
        pageNum.value,
        pageSize.value,
        filterStatus.value ?? undefined,
        searchDetectionId.value
      )
      repairList.value = res.data.list
      total.value = res.data.total
      stats.total = res.data.total
      updateStats()
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchDetectionId.value = undefined
  pageNum.value = 1
  fetchData()
}

const updateStats = async () => {
  try {
    const res = await repairApi.getStats()
    stats.pending = res.data.pending
    stats.inProgress = res.data.inProgress
    stats.toConfirm = res.data.toConfirm
    stats.completed = res.data.completed
    stats.total = res.data.total
  } catch (error) {
    // ignore
  }
}

const updateMyStats = async () => {
  if (!userId.value) return
  try {
    const res = await repairApi.getMyStats(userId.value)
    stats.pending = res.data.pending
    stats.inProgress = res.data.inProgress
    stats.toConfirm = res.data.toConfirm
    stats.completed = res.data.completed
    stats.total = res.data.total
  } catch (error) {
    // ignore
  }
}

const handleFilterChange = () => {
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

const showAddDialog = () => {
  isEdit.value = false
  form.detectionId = undefined
  form.manholeId = ''
  form.repairUserId = undefined
  form.remark = ''
  form.status = 0
  dialogVisible.value = true
}

// 详情对话框
const detailVisible = ref(false)
const detailData = ref<Repair | null>(null)

const showDetailDialog = async (row: Repair) => {
  try {
    const res = await repairApi.getById(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 重新分配对话框
const reassignVisible = ref(false)
const reassignForm = reactive({
  id: 0,
  repairUserId: undefined as number | undefined
})
const repairUsers = ref<{id: number, username: string}[]>([])

const showReassignDialog = async (row: Repair) => {
  reassignForm.id = row.id
  reassignForm.repairUserId = undefined
  try {
    const res = await repairApi.getRepairUsers()
    repairUsers.value = res.data
  } catch (error) {
    ElMessage.error('获取维修人员列表失败')
    repairUsers.value = []
  }
  reassignVisible.value = true
}

const handleReassign = async () => {
  if (!reassignForm.repairUserId) {
    ElMessage.warning('请选择维修人员')
    return
  }
  try {
    await repairApi.reassign(reassignForm.id, reassignForm.repairUserId)
    ElMessage.success('重新分配成功，任务状态已重置为待处理')
    reassignVisible.value = false
    fetchData()
    updateStats()
  } catch (error) {
    ElMessage.error('重新分配失败')
  }
}

// ========== 维修员提交完成对话框 ==========
const completeVisible = ref(false)
const completeLoading = ref(false)
const completeForm = reactive({
  id: 0,
  manholeId: '',
  afterImg: '',
  remark: '',
  customRemark: ''
})

// 上传文件相关
const uploadedFileName = ref('')
const isImageVerified = ref(false)
const imageVerifyError = ref('')

// 备注选项（枚举类）
const remarkOptions = [
  { label: '井盖破损已更换', value: '井盖破损已更换' },
  { label: '井盖松动已加固', value: '井盖松动已加固' },
  { label: '井圈损坏已修复', value: '井圈损坏已修复' },
  { label: '清理杂物完成', value: '清理杂物完成' },
  { label: '更换密封圈', value: '更换密封圈' },
  { label: '调整井盖高度', value: '调整井盖高度' },
  { label: '修复防盗装置', value: '修复防盗装置' },
  { label: '其他', value: '其他' }
]

// 上传配置
const uploadUrl = 'http://localhost:8086/api/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const showCompleteDialog = (row: Repair) => {
  completeForm.id = row.id
  completeForm.manholeId = row.manholeId || ''
  completeForm.afterImg = ''
  completeForm.remark = ''
  completeForm.customRemark = ''
  uploadedFileName.value = ''
  isImageVerified.value = false
  imageVerifyError.value = ''
  completeVisible.value = true
}

// 校验文件名是否包含井盖编号
const verifyImageFilename = (filename: string): boolean => {
  if (!completeForm.manholeId) return true // 如果没有井盖编号，跳过校验
  const normalizedFilename = filename.toLowerCase()
  const normalizedManholeId = completeForm.manholeId.toLowerCase()
  return normalizedFilename.includes(normalizedManholeId)
}

const handleUploadSuccess = (response: any, file: any) => {
  if (response.code === 200 && response.data?.url) {
    completeForm.afterImg = response.data.url
    uploadedFileName.value = file.name

    // 校验文件名是否包含井盖编号
    if (verifyImageFilename(file.name)) {
      isImageVerified.value = true
      imageVerifyError.value = ''
      ElMessage.success('图片上传成功，井盖编号校验通过')
    } else {
      isImageVerified.value = false
      imageVerifyError.value = `文件名"${file.name}"与井盖编号"${completeForm.manholeId}"不一致，请确认上传正确的照片`
      ElMessage.warning('文件名与井盖编号不一致，请确认上传正确')
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleCompleteSubmit = async () => {
  if (!completeForm.afterImg) {
    ElMessage.warning('请上传维修后照片')
    return
  }

  if (!isImageVerified.value) {
    ElMessageBox.confirm(
      '上传的图片文件名与井盖编号不一致，是否确认提交？',
      '校验警告',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '重新上传',
        type: 'warning'
      }
    ).then(() => {
      // 用户选择确认提交，继续提交
      doSubmitComplete()
    }).catch(() => {
      // 用户选择重新上传，不做任何操作
    })
    return
  }

  await doSubmitComplete()
}

const doSubmitComplete = async () => {
  // 组装备注（如果选择"其他"，则加上自定义备注）
  let finalRemark = completeForm.remark
  if (completeForm.remark === '其他' && completeForm.customRemark) {
    finalRemark = `其他：${completeForm.customRemark}`
  }

  completeLoading.value = true
  try {
    await repairApi.complete(completeForm.id, completeForm.afterImg, finalRemark)
    ElMessage.success('维修完成已提交，等待管理员确认')
    completeVisible.value = false
    fetchData()
    if (isRepairer.value) {
      updateMyStats()
    } else {
      updateStats()
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    completeLoading.value = false
  }
}

const showEditDialog = (row: Repair) => {
  isEdit.value = true
  currentId.value = row.id
  form.detectionId = row.detectionId
  form.manholeId = row.manholeId || ''
  form.repairUserId = row.repairUserId
  form.remark = row.remark || ''
  form.status = row.status
  dialogVisible.value = true
}

// 开始维修（从待处理变为维修中）
const startRepair = async (id: number) => {
  // 找到当前行数据
  const row = repairList.value.find(r => r.id === id)
  if (!row?.principal) {
    ElMessage.warning('该维修任务未分配负责人，请先分配')
    return
  }
  try {
    await ElMessageBox.confirm('确定要开始此维修任务吗？', '开始维修', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await repairApi.start(id)
    ElMessage.success('维修任务已开始')
    fetchData()
    updateStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 确认维修完成（从待确认变为已完成）
const confirmComplete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定此维修任务已完成吗？', '确认完成', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await repairApi.confirm(id)
    ElMessage.success('维修任务已确认完成')
    fetchData()
    updateStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 完成维修（从维修中变为待确认）- 旧方法，现在使用 showCompleteDialog
const completeRepair = async (id: number) => {
  // 现在使用 showCompleteDialog 方法替代
  console.warn('completeRepair 已弃用，请使用 showCompleteDialog')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value && currentId.value) {
        const params = {
          repairUserId: form.repairUserId,
          remark: form.remark,
          status: form.status
        }
        await repairApi.update(currentId.value, params)
        ElMessage.success('更新成功')
      } else {
        const params = {
          detectionId: form.detectionId!,
          manholeId: form.manholeId,
          repairUserId: form.repairUserId,
          remark: form.remark,
          status: form.status
        }
        await repairApi.create(params)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 导出功能
const handleExport = () => {
  if (repairList.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  // 构建 CSV 数据
  const headers = ['维修ID', '关联检测ID', '井盖编号', '负责人', '状态', '分配时间', '开始时间', '完成时间', '备注']
  const rows = repairList.value.map(item => [
    item.id,
    item.repairId,
    item.manholeId || '',
    item.principal || '未分配',
    getStatusText(item.status),
    item.assignedTime || '',
    item.startTime || '',
    item.endTime || '',
    item.remark || ''
  ])

  // 转换为 CSV 格式
  const csvContent = [headers, ...rows].map(e => e.join(',')).join('\n')

  // 下载文件
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `维修记录_${new Date().toISOString().split('T')[0]}.csv`
  link.click()
  URL.revokeObjectURL(link.href)

  ElMessage.success('导出成功')
}

const viewDetection = (row: Repair) => {
  // 打开详情对话框查看检测信息
  showDetailDialog(row)
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'danger',
    3: 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待处理',
    1: '进行中',
    2: '待确认',
    3: '已完成'
  }
  return texts[status] || '未知'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
// 使用全局布局系统

.process-section {
  padding: 24px;

  :deep(.el-steps--simple) {
    background: transparent;
    padding: 0;
  }
}

.stat-card {
  &.pending .stat-icon {
    background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  }

  &.progress .stat-icon {
    background: linear-gradient(135deg, #e6a23c 0%, #f5c77c 100%);
  }

  &.to-confirm .stat-icon {
    background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%);
  }

  &.completed .stat-icon {
    background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  }

  &.total .stat-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
}

:deep(.el-table) {
  th {
    background: var(--bg-primary) !important;
    font-weight: 600;
  }

  .user-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .text-muted {
    color: var(--text-secondary);
  }

  .text-placeholder {
    color: var(--text-muted);
  }
}

// 详情对话框样式
:deep(.el-descriptions) {
  .el-descriptions__label {
    color: var(--text-primary);
    font-weight: 600;
  }

  .el-descriptions__content {
    color: var(--text-primary);
  }
}

.detail-images {
  margin-top: 20px;

  h4 {
    color: var(--text-primary);
    margin-bottom: 12px;
    font-size: 16px;
    font-weight: 600;
  }
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 上传组件样式
.repair-uploader {
  :deep(.el-upload) {
    width: 100%;
    height: 200px;
    border: 2px dashed var(--border-color);
    border-radius: 8px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;

    &:hover {
      border-color: var(--primary-color);
      background: rgba(64, 158, 255, 0.05);
    }
  }

  .uploaded-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: var(--text-muted);

    .el-icon {
      font-size: 48px;
      margin-bottom: 12px;
    }

    span {
      font-size: 14px;
    }
  }
}

.upload-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
  text-align: center;
}

// 井盖编号显示样式
.manhole-id-display {
  display: flex;
  align-items: center;
  gap: 12px;

  .manhole-tip {
    font-size: 13px;
    color: var(--text-muted);
  }
}

// 文件校验信息样式
.file-verify-info {
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;

  &.verify-success {
    background: rgba(103, 194, 58, 0.1);
    color: #67c23a;
  }

  &.verify-warning {
    background: rgba(230, 162, 60, 0.1);
    color: #e6a23c;
  }

  .el-icon {
    font-size: 16px;
  }
}

// 备注提示
.remark-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

// 搜索框样式
.search-box {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-input {
    :deep(.el-input__wrapper) {
      border-radius: 8px;
    }
  }

  .el-button {
    border-radius: 8px;
  }
}
</style>
