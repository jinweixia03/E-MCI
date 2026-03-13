<template>
  <div class="app-container">
    <h2 class="page-title">用户中心</h2>
    <el-card class="user-center-card">
      <div class="user-profile">
        <div class="avatar-section">
          <div class="avatar-default">
            <el-icon><User /></el-icon>
          </div>
        </div>
        <div class="info-section">
          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ username }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ mobile }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">用户类型</span>
            <el-tag :type="userTypeTagType">{{ userTypeText }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="action-card">
      <template #header>
        <span>账户操作</span>
      </template>
      <div class="action-buttons">
        <el-button type="primary" @click="updatePassword">
          <el-icon><Lock /></el-icon> 修改密码
        </el-button>
        <el-button type="danger" @click="logout">
          <el-icon><SwitchButton /></el-icon> 退出登录
        </el-button>
      </div>
    </el-card>

    <UpdateInfo v-model:dialog-visible="dialogVisible" @handleClose="handleClose" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import UpdateInfo from './UpdateInfo.vue'

const router = useRouter()
const userStore = useUserStore()

const username = computed(() => userStore.userInfo?.username || '未登录')
const mobile = computed(() => userStore.userInfo?.phone || '未绑定')

// 用户类型映射
const userTypeText = computed(() => {
  const type = userStore.userInfo?.type
  const typeMap: Record<number, string> = { 0: '普通用户', 1: '管理员', 2: '维修员' }
  return typeMap[type ?? 0] || '普通用户'
})

const userTypeTagType = computed(() => {
  const type = userStore.userInfo?.type
  const tagMap: Record<number, '' | 'success' | 'warning' | 'danger'> = {
    0: '',
    1: 'success',
    2: 'warning'
  }
  return tagMap[type ?? 0] || ''
})

// 获取当前用户信息
const fetchUserInfo = async () => {
  try {
    const res = await authApi.getCurrentUser()
    if (res.code === 200) {
      userStore.setUserInfo(res.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

onMounted(() => {
  fetchUserInfo()
})
const dialogVisible = ref(false)

const updatePassword = () => {
  dialogVisible.value = true
}

const handleClose = () => {
  dialogVisible.value = false
}

const logout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  min-height: calc(100vh - 84px);
  background-color: #f5f7fa;
}

.page-title {
  margin: 0 0 20px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.user-center-card {
  margin-bottom: 20px;

  .user-profile {
    display: flex;
    align-items: center;
    gap: 40px;
    padding: 20px;
  }

  .avatar-section {
    text-align: center;
  }

  .avatar-default {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 60px;
    color: #fff;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }

  .info-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .info-item {
    display: flex;
    align-items: center;
    gap: 20px;

    .info-label {
      width: 80px;
      color: #606266;
      font-size: 14px;
    }

    .info-value {
      color: #303133;
      font-size: 16px;
      font-weight: 500;
    }
  }
}

.action-card {
  .action-buttons {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;

    .el-button {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}
</style>
