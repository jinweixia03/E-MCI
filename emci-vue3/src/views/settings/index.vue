<template>
  <div class="app-container">
    <el-card class="setting-box">
      <div class="setting-info">
        <el-upload
          class="avatar-uploader"
          action="https://jsonplaceholder.typicode.com/posts/"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="imageUrl" :src="imageUrl" class="avatar" alt="">
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <el-row class="info">
          <el-col class="user-info" :span="24">用户名：{{ username }}</el-col>
          <el-col class="user-info" :span="24">手机号：{{ mobile }}</el-col>
        </el-row>
      </div>
      <div class="setting-btn">
        <el-button class="setting-btn-update" type="primary" @click="updatePassword">修改密码</el-button>
        <el-button type="danger" @click="logout">退出登录</el-button>
      </div>
      <UpdateInfo v-model:dialog-visible="dialogVisible" @handleClose="handleClose" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import UpdateInfo from './UpdateInfo.vue'

const router = useRouter()
const userStore = useUserStore()

const imageUrl = ref('')
const loading = ref(false)

const username = computed(() => userStore.userInfo?.username || '未登录')
const mobile = computed(() => userStore.userInfo?.phone || '未绑定')

// 获取当前用户信息
const fetchUserInfo = async () => {
  try {
    const res = await authApi.getCurrentUser()
    if (res.code === 200) {
      userStore.setUserInfo(res.data)
      if (res.data.headImg) {
        imageUrl.value = res.data.headImg
      }
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

const handleAvatarSuccess = (res: any, file: File) => {
  imageUrl.value = URL.createObjectURL(file)
}

const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}
</script>

<style lang="scss" scoped>
.app-container {
  height: calc(100vh - 84px);
}

.setting-box {
  padding-top: 10vh;
  display: flex;
  align-content: center;
  flex-direction: column;
  height: 100%;

  .setting-info {
    display: grid;
    grid-template-columns: 1fr 2fr;
    justify-content: center;
    align-items: center;
  }

  .avatar-uploader {
    margin: 0 auto;
  }

  .info {
    position: relative;
    top: -32px
  }

  .user-info {
    color: #1f2d3d;
    font-size: 32px;
    font-weight: 600;
    line-height: 80px;
    height: 80px;
  }

  .setting-btn {
    width: 100%;
    text-align: center;
    margin: 10vh auto 0;

    .setting-btn-update {
      margin-right: 10vw
    }
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 240px;
  height: 240px;
  line-height: 240px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;

  &:hover {
    border-color: #409EFF;
  }
}

.avatar {
  width: 240px;
  height: 240px;
  display: block;
  border-radius: 50%;
}
</style>
