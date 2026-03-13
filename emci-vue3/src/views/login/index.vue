<template>
  <div class="login-page">
    <!-- 背景动画元素 -->
    <div class="bg-animation">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>

    <!-- 登录容器 -->
    <div class="login-container" :class="{ 'fade-in': showContainer }">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-wrapper">
            <div class="logo-icon">
              <el-icon :size="60" color="#fff"><Monitor /></el-icon>
            </div>
            <h1 class="brand-title">E-MCI</h1>
          </div>
          <div class="brand-text">
            <p class="greeting">Hi! 你好!</p>
            <p class="welcome">欢迎进入 E-MCI</p>
            <p class="description">智能无人机光缆井盖检测运维系统</p>
          </div>
          <div class="features">
            <div class="feature-item" v-for="(item, index) in features" :key="index">
              <el-icon :size="20" color="#67e8f9"><Check /></el-icon>
              <span>{{ item }}</span>
            </div>
          </div>
        </div>
        <div class="brand-image">
          <img src="../../assets/loginLogo.jpg" alt="login" />
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <div class="form-wrapper" :class="{ 'slide-up': showForm }">
          <h2 class="form-title">登录系统</h2>
          <p class="form-subtitle">请输入您的账号信息</p>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <!-- 用户名输入框 -->
            <el-form-item prop="username" class="form-item-animated" :class="{ 'focused': usernameFocused }">
              <div class="input-label">
                <span>用户账号</span>
                <a class="link-text" @click="goRegister">注册账号</a>
              </div>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                class="custom-input"
                @focus="usernameFocused = true"
                @blur="usernameFocused = false"
              >
                <template #prefix>
                  <el-icon :size="18"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 密码输入框 -->
            <el-form-item prop="password" class="form-item-animated" :class="{ 'focused': passwordFocused }">
              <div class="input-label">
                <span>用户密码</span>
                <a class="link-text" @click="forgotPassword">忘记密码?</a>
              </div>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                class="custom-input"
                show-password
                @focus="passwordFocused = true"
                @blur="passwordFocused = false"
              >
                <template #prefix>
                  <el-icon :size="18"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 记住密码 -->
            <div class="form-options">
              <el-checkbox v-model="rememberPassword" class="remember-checkbox">
                记住密码
              </el-checkbox>
            </div>

            <!-- 登录按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                <span class="btn-text">登 录</span>
                <el-icon class="btn-icon" v-if="!loading"><ArrowRight /></el-icon>
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 其他登录方式 -->
          <div class="other-login">
            <div class="divider">
              <span>其他登录方式</span>
            </div>
            <div class="login-icons">
              <div class="icon-item" @click="otherLogin('wechat')">
                <el-icon :size="24" color="#07c160"><ChatDotRound /></el-icon>
              </div>
              <div class="icon-item" @click="otherLogin('phone')">
                <el-icon :size="24" color="#409eff"><Iphone /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="login-footer">
      <p>© 2024 E-MCI 智能井盖检测系统 | 版权所有</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight, Monitor, Check, ChatDotRound, Iphone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 响应式状态
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberPassword = ref(false)
const showContainer = ref(false)
const showForm = ref(false)
const usernameFocused = ref(false)
const passwordFocused = ref(false)

// 特性列表
const features = [
  'AI 智能检测识别',
  '实时井盖监控',
  '无人机巡检管理',
  '数据可视化分析'
]

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 从本地存储读取记住的密码
onMounted(() => {
  const savedUsername = localStorage.getItem('emci_username')
  const savedPassword = localStorage.getItem('emci_password')
  if (savedUsername && savedPassword) {
    loginForm.username = savedUsername
    loginForm.password = atob(savedPassword) // base64解码
    rememberPassword.value = true
  }

  // 触发动画
  setTimeout(() => {
    showContainer.value = true
  }, 100)
  setTimeout(() => {
    showForm.value = true
  }, 300)
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在 3-20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在 6-20 个字符之间', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const response = await authApi.login({
        username: loginForm.username,
        password: loginForm.password
      })

      if (response.code === 200) {
        const { data } = response

        // 保存登录信息
        userStore.login({
          accessToken: data.accessToken || data.token || '',
          tokenType: data.tokenType || 'Bearer',
          expiresIn: data.expiresIn || 7200,
          userInfo: data.userInfo || data.user || (data as any)
        })

        // 处理记住密码
        if (rememberPassword.value) {
          localStorage.setItem('emci_username', loginForm.username)
          localStorage.setItem('emci_password', btoa(loginForm.password)) // base64编码
        } else {
          localStorage.removeItem('emci_username')
          localStorage.removeItem('emci_password')
        }

        ElMessage.success('登录成功！欢迎回来')
        router.push('/')
      } else {
        ElMessage.error(response.message || '登录失败，请检查用户名和密码')
      }
    } catch (error: any) {
      console.error('登录错误:', error)
      ElMessage.error(error.response?.data?.message || '网络错误，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

// 跳转注册
const goRegister = () => {
  router.push('/register')
}

// 忘记密码
const forgotPassword = () => {
  router.push('/forgot-password')
}

// 其他登录方式
const otherLogin = (type: string) => {
  ElMessage.info(`${type === 'wechat' ? '微信' : '手机'}登录功能开发中...`)
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

// 背景动画
.bg-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.circle-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  right: -100px;
  animation-delay: -5s;
}

.circle-3 {
  width: 300px;
  height: 300px;
  top: 50%;
  left: 50%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

// 登录容器
.login-container {
  display: flex;
  width: 100%;
  max-width: 1200px;
  min-height: 640px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  z-index: 1;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);

  &.fade-in {
    opacity: 1;
    transform: translateY(0);
  }
}

// 左侧品牌区域
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #0076f0 0%, #00c6ff 100%);
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #fff;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
    animation: rotate 30s linear infinite;
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.brand-content {
  position: relative;
  z-index: 1;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 40px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-text {
  margin-bottom: 40px;

  .greeting {
    font-size: 24px;
    font-weight: 300;
    margin-bottom: 8px;
    opacity: 0.9;
  }

  .welcome {
    font-size: 32px;
    font-weight: 600;
    margin-bottom: 16px;
  }

  .description {
    font-size: 16px;
    opacity: 0.8;
    line-height: 1.6;
  }
}

.features {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  opacity: 0.9;
}

.brand-image {
  position: relative;
  z-index: 1;
  margin-top: 40px;

  img {
    width: 100%;
    max-width: 400px;
    border-radius: 16px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  }
}

// 右侧表单区域
.form-section {
  flex: 0 0 460px;
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.form-wrapper {
  width: 100%;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s ease-out;

  &.slide-up {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #888;
  margin-bottom: 40px;
}

// 表单样式
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
}

.form-item-animated {
  transition: transform 0.3s ease;

  &.focused {
    transform: translateX(5px);
  }
}

.input-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.link-text {
  color: #0076f0;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #0056b3;
    text-decoration: underline;
  }
}

// 自定义输入框
:deep(.custom-input) {
  .el-input__wrapper {
    padding: 0 15px;
    height: 52px;
    border-radius: 12px;
    box-shadow: 0 0 0 1px #e0e0e0 inset;
    transition: all 0.3s ease;

    &:hover, &.is-focus {
      box-shadow: 0 0 0 2px #0076f0 inset;
    }
  }

  .el-input__prefix {
    margin-right: 10px;
    color: #999;
  }

  .el-input__inner {
    font-size: 15px;

    &::placeholder {
      color: #aaa;
    }
  }
}

// 记住密码
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

:deep(.remember-checkbox) {
  .el-checkbox__label {
    font-size: 13px;
    color: #666;
  }

  .el-checkbox__input.is-checked .el-checkbox__inner {
    background-color: #0076f0;
    border-color: #0076f0;
  }
}

// 登录按钮
.login-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #0076f0 0%, #00c6ff 100%);
  border: none;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(0, 118, 240, 0.3);
  }

  &:active {
    transform: translateY(0);
  }

  .btn-text {
    letter-spacing: 2px;
  }

  .btn-icon {
    transition: transform 0.3s ease;
  }

  &:hover .btn-icon {
    transform: translateX(4px);
  }
}

// 其他登录方式
.other-login {
  margin-top: 40px;
}

.divider {
  position: relative;
  text-align: center;
  margin-bottom: 20px;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, #ddd, transparent);
  }

  span {
    position: relative;
    background: #fff;
    padding: 0 16px;
    font-size: 13px;
    color: #999;
  }
}

.login-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.icon-item {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background: #e8e8e8;
    transform: translateY(-3px);
  }
}

// 页脚
.login-footer {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  z-index: 1;
}

// 响应式适配
@media (max-width: 1024px) {
  .login-container {
    flex-direction: column;
    max-width: 480px;
  }

  .brand-section {
    display: none;
  }

  .form-section {
    flex: 1;
    padding: 40px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 0;
  }

  .login-container {
    border-radius: 0;
    min-height: 100vh;
  }

  .form-section {
    padding: 30px 24px;
  }
}
</style>
