<template>
  <div class="drone-page page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Aim /></el-icon>
        <div class="title-content">
          <h1>无人机管理</h1>
          <p>管理无人机设备、监控运行状态</p>
        </div>
      </div>
      <div class="header-actions">
        <el-radio-group v-model="filterStatus" @change="handleFilterChange">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="1">运行中</el-radio-button>
          <el-radio-button :label="0">已停用</el-radio-button>
        </el-radio-group>
        <el-button type="primary" size="large" @click="showAddDialog">
          <el-icon><Plus /></el-icon>新增无人机
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid cols-4">
      <div class="stat-card total">
        <div class="stat-icon"><el-icon><Aim /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ total }}</div>
          <div class="stat-label">无人机总数</div>
        </div>
      </div>
      <div class="stat-card active">
        <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ activeCount }}</div>
          <div class="stat-label">运行中</div>
        </div>
      </div>
      <div class="stat-card inactive">
        <div class="stat-icon"><el-icon><CircleClose /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ inactiveCount }}</div>
          <div class="stat-label">已停用</div>
        </div>
      </div>
    </div>

    <!-- 无人机卡片网格 -->
    <div v-loading="loading" class="drone-grid">
      <div v-for="drone in droneList" :key="drone.id" class="drone-card" :class="{ active: drone.status === 1 }">
        <div class="card-header">
          <div class="status-badge" :class="drone.status === 1 ? 'online' : 'offline'">
            <el-icon><component :is="drone.status === 1 ? 'CircleCheck' : 'CircleClose'" /></el-icon>
            {{ drone.status === 1 ? '运行中' : '已停用' }}
          </div>
          <el-dropdown trigger="click" @command="handleCommand($event, drone)">
            <el-icon class="more-icon"><More /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit"><el-icon><Edit /></el-icon>编辑</el-dropdown-item>
                <el-dropdown-item command="toggle">
                  <el-icon><component :is="drone.status === 1 ? 'VideoPause' : 'VideoPlay'" /></el-icon>
                  {{ drone.status === 1 ? '停用' : '启动' }}
                </el-dropdown-item>
                <el-dropdown-item divided command="delete" class="danger">
                  <el-icon><Delete /></el-icon>删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="drone-icon">
          <el-icon><Aim /></el-icon>
        </div>

        <div class="drone-info">
          <h3 class="drone-id">{{ drone.droneId }}</h3>
          <div class="drone-meta">
            <div class="meta-item">
              <el-icon><Timer /></el-icon>
              <span>续航 {{ drone.maxHour || 0 }} 小时</span>
            </div>
            <div class="meta-item">
              <el-icon><FullScreen /></el-icon>
              <span>半径 {{ drone.radius || 0 }} 米</span>
            </div>
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ formatCoordinate(drone.latitude) }}, {{ formatCoordinate(drone.longitude) }}</span>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <el-button text type="primary" @click="showEditDialog(drone)">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button text :type="drone.status === 1 ? 'warning' : 'success'" @click="toggleStatus(drone)">
            <el-icon><component :is="drone.status === 1 ? 'VideoPause' : 'VideoPlay'" /></el-icon>
            {{ drone.status === 1 ? '停用' : '启动' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && droneList.length === 0" description="暂无无人机数据">
      <el-button type="primary" @click="showAddDialog">新增无人机</el-button>
    </el-empty>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[9, 18, 36]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑无人机' : '新增无人机'" width="520px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="无人机编号" prop="droneId">
          <el-input v-model="form.droneId" :disabled="isEdit" placeholder="请输入编号" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="续航时间">
              <el-input-number v-model="form.maxHour" :min="0" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="覆盖半径">
              <el-input-number v-model="form.radius" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input-number v-model="form.latitude" :precision="6" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input-number v-model="form.longitude" :precision="6" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">停用</el-radio>
            <el-radio :label="1">运行中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { droneApi } from '@/api/drone'
import type { Drone } from '@/types/drone'
import {
  Plus, Aim, CircleCheck, CircleClose, Edit, Delete,
  VideoPlay, VideoPause, More, Timer, FullScreen, Location
} from '@element-plus/icons-vue'

const loading = ref(false)
const droneList = ref<Drone[]>([])
const pageNum = ref(1)
const pageSize = ref(9)
const total = ref(0)
const filterStatus = ref<number | null>(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentId = ref<number>()

const form = reactive({
  droneId: '',
  maxHour: undefined as number | undefined,
  radius: undefined as number | undefined,
  latitude: undefined as number | undefined,
  longitude: undefined as number | undefined,
  status: 0
})

const rules: FormRules = {
  droneId: [{ required: true, message: '请输入无人机编号', trigger: 'blur' }]
}

const activeCount = computed(() => droneList.value.filter(d => d.status === 1).length)
const inactiveCount = computed(() => droneList.value.filter(d => d.status === 0).length)

const formatCoordinate = (val?: number) => {
  return val?.toFixed(4) || '--'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await droneApi.pageQuery(pageNum.value, pageSize.value, filterStatus.value ?? undefined)
    droneList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
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
  form.droneId = ''
  form.maxHour = undefined
  form.radius = undefined
  form.latitude = undefined
  form.longitude = undefined
  form.status = 0
  dialogVisible.value = true
}

const showEditDialog = (row: Drone) => {
  isEdit.value = true
  currentId.value = row.id
  form.droneId = row.droneId
  form.maxHour = row.maxHour
  form.radius = row.radius
  form.latitude = row.latitude
  form.longitude = row.longitude
  form.status = row.status
  dialogVisible.value = true
}

const toggleStatus = async (row: Drone) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await droneApi.updateStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '无人机已启动' : '无人机已停用')
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const params = {
        droneId: form.droneId,
        maxHour: form.maxHour,
        radius: form.radius,
        latitude: form.latitude,
        longitude: form.longitude,
        status: form.status
      }
      if (isEdit.value && currentId.value) {
        await droneApi.update(currentId.value, params)
        ElMessage.success('更新成功')
      } else {
        await droneApi.create(params)
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

const handleDelete = async (id: number) => {
  try {
    await droneApi.delete(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleCommand = (command: string, drone: Drone) => {
  switch (command) {
    case 'edit':
      showEditDialog(drone)
      break
    case 'toggle':
      toggleStatus(drone)
      break
    case 'delete':
      ElMessageBox.confirm('确定删除该无人机吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => handleDelete(drone.id))
      break
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
// 使用全局布局变量

.drone-page {
  // 使用全局 page-container 类
}

.page-header {
  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }
}

.stats-grid {
  &.cols-4 {
    grid-template-columns: repeat(3, 1fr);
  }
}

.stat-card {
  &.total .stat-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.active .stat-icon {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  }

  &.inactive .stat-icon {
    background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  }
}

.drone-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--card-gap);
  flex: 1;
  min-height: 0;
}

.drone-card {
  background: var(--bg-secondary);
  border-radius: var(--card-border-radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  position: relative;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  }

  &.active::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #11998e, #38ef7d);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;

    .status-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 500;

      &.online {
        background: rgba(103, 194, 58, 0.1);
        color: #67c23a;
      }

      &.offline {
        background: rgba(144, 147, 153, 0.1);
        color: #909399;
      }
    }

    .more-icon {
      color: #c0c4cc;
      cursor: pointer;
      padding: 4px;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        color: #606266;
        background: #f5f7fa;
      }
    }
  }

  .drone-icon {
    display: flex;
    justify-content: center;
    padding: 20px 0;

    .el-icon {
      font-size: 64px;
      color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
      padding: 20px;
      border-radius: 50%;
    }
  }

  .drone-info {
    padding: 0 20px 16px;
    text-align: center;

    .drone-id {
      font-size: 20px;
      font-weight: 600;
      color: #1a1a2e;
      margin-bottom: 12px;
    }

    .drone-meta {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .meta-item {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        font-size: 13px;
        color: #606266;

        .el-icon {
          color: #909399;
        }
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding: 16px 20px;
    border-top: 1px solid #f0f2f5;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--page-gap);
  border-top: 1px solid var(--border-light);
  flex-shrink: 0;
}

// ========================
// 响应式布局
// ========================
@media (max-width: 1200px) {
  .stats-grid.cols-4 {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .stats-grid.cols-4 {
    grid-template-columns: repeat(2, 1fr);
  }

  .drone-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  }
}

@media (max-width: 768px) {
  .stats-grid.cols-4 {
    grid-template-columns: 1fr;
  }

  .drone-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .drone-card {
    .drone-icon .el-icon {
      font-size: 48px;
      padding: 16px;
    }

    .drone-info .drone-id {
      font-size: 18px;
    }
  }
}
</style>
