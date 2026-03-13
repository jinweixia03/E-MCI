<template>
  <div class="forgot-password-page">
    <!-- 背景动画元素 -->
    <div class="bg-animation">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>

    <!-- 主容器 -->
    <div class="main-container" :class="{ 'fade-in': showContainer }">
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
            <p class="greeting">密码找回</p>
            <p class="welcome">重置您的密码</p>
            <p class="description">通过账号、手机号和邮箱验证身份后重置密码</p>
          </div>
          <div class="features">
            <div class="feature-item" v-for="(item, index) in features" :key="index">
              <el-icon :size="20" color="#67e8f9"><Check /></el-icon>
              <span>{{ item }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="form-wrapper" :class="{ 'slide-up': showForm }">
          <!-- 步骤条 -->
          <el-steps :active="currentStep" finish-status="success" simple class="steps">
            <el-step title="验证身份" />
            <el-step title="重置密码" />
            <el-step title="完成" />
          </el-steps>

          <!-- 步骤1: 验证身份 -->
          <div v-if="currentStep === 0" class="step-content">
            <h2 class="form-title">找回密码</h2>
            <p class="form-subtitle">请填写以下信息验证您的身份</p>

            <el-form
              ref="verifyFormRef"
              :model="verifyForm"
              :rules="verifyRules"
              class="forgot-form"
              @keyup.enter="handleVerify"
            >
              <!-- 账号输入框 -->
              <el-form-item prop="username" class="form-item-animated" :class="{ 'focused': usernameFocused }">
                <div class="input-label">
                  <span>账号</span>
                </div>
                <el-input
                  v-model="verifyForm.username"
                  placeholder="请输入您的账号"
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

              <!-- 手机号输入框 -->
              <el-form-item prop="phone" class="form-item-animated" :class="{ 'focused': phoneFocused }">
                <div class="input-label">
                  <span>手机号</span>
                </div>
                <el-input
                  v-model="verifyForm.phone"
                  placeholder="请输入注册时的手机号"
                  size="large"
                  class="custom-input"
                  @focus="phoneFocused = true"
                  @blur="phoneFocused = false"
                >
                  <template #prefix>
                    <el-icon :size="18"><Iphone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <!-- 邮箱输入框 -->
              <el-form-item prop="email" class="form-item-animated" :class="{ 'focused': emailFocused }">
                <div class="input-label">
                  <span>邮箱</span>
                </div>
                <el-input
                  v-model="verifyForm.email"
                  placeholder="请输入注册时的邮箱"
                  size="large"
                  class="custom-input"
                  @focus="emailFocused = true"
                  @blur="emailFocused = false"
                >
                  <template #prefix>
                    <el-icon :size="18"><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <!-- 验证按钮 -->
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  :loading="verifyLoading"
                  class="submit-btn"
                  @click="handleVerify"
                >
                  <span class="btn-text">验 证</span>
                  <el-icon class="btn-icon" v-if="!verifyLoading"><ArrowRight /></el-icon>
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 步骤2: 重置密码 -->
          <div v-if="currentStep === 1" class="step-content">
            <h2 class="form-title">重置密码</h2>
            <p class="form-subtitle">验证成功，请设置新密码</p>

            <el-form
              ref="resetFormRef"
              :model="resetForm"
              :rules="resetRules"
              class="forgot-form"
              @keyup.enter="handleReset"
            >
              <!-- 新密码输入框 -->
              <el-form-item prop="newPassword" class="form-item-animated" :class="{ 'focused': newPasswordFocused }">
                <div class="input-label">
                  <span>新密码</span>
                </div>
                <el-input
                  v-model="resetForm.newPassword"
                  type="password"
                  placeholder="请输入新密码 (6-20个字符)"
                  size="large"
                  class="custom-input"
                  show-password
                  @focus="newPasswordFocused = true"
                  @blur="newPasswordFocused = false"
                >
                  <template #prefix>
                    <el-icon :size="18"><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <!-- 确认密码输入框 -->
              <el-form-item prop="confirmPassword" class="form-item-animated" :class="{ 'focused': confirmPasswordFocused }">
                <div class="input-label">
                  <span>确认密码</span>
                </div>
                <el-input
                  v-model="resetForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  size="large"
                  class="custom-input"
                  show-password
                  @focus="confirmPasswordFocused = true"
                  @blur="confirmPasswordFocused = false"
                >
                  <template #prefix>
                    <el-icon :size="18"><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <!-- 重置按钮 -->
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  :loading="resetLoading"
                  class="submit-btn"
                  @click="handleReset"
                >
                  <span class="btn-text">重置密码</span>
                  <el-icon class="btn-icon" v-if="!resetLoading"><ArrowRight /></el-icon>
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 步骤3: 完成 -->
          <div v-if="currentStep === 2" class="step-content success-step">
            <div class="success-icon">
              <el-icon :size="80" color="#67c23a"><CircleCheck /></el-icon>
            </div>
            <h2 class="form-title">密码重置成功</h2>
            <p class="form-subtitle">您的密码已重置成功，请使用新密码登录</p>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              @click="goLogin"
            >
              <span class="btn-text">去登录</span>
              <el-icon class="btn-icon"><ArrowRight /></el-icon>
            </el-button>
          </div>

          <!-- 返回登录 -->
          <div class="back-to-login">
            <span>想起密码了？</span>
            <a class="link-text" @click="goLogin">返回登录</a>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="page-footer">
      <p>© 2024 E-MCI 智能井盖检测系统 | 版权所有</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight, Monitor, Check, Message, Iphone, CircleCheck } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

// 响应式状态
const currentStep = ref(0)
const verifyFormRef = ref<FormInstance>()
const resetFormRef = ref<FormInstance>()
const verifyLoading = ref(false)
const resetLoading = ref(false)
const showContainer = ref(false)
const showForm = ref(false)
const usernameFocused = ref(false)
const phoneFocused = ref(false)
const emailFocused = ref(false)
const newPasswordFocused = ref(false)
const confirmPasswordFocused = ref(false)

// 特性列表
const features = [
  '安全验证保护',
  '多重身份确认',
  '快速密码重置',
  '账户安全保障'
]

// 验证表单数据
const verifyForm = reactive({
  username: '',
  phone: '',
  email: ''
})

// 重置密码表单数据
const resetForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 自定义验证规则：确认密码
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 验证表单规则
const verifyRules: FormRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度应在 3-20 个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

// 重置密码表单规则
const resetRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在 6-20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 触发动画
onMounted(() => {
  setTimeout(() => {
    showContainer.value = true
  }, 100)
  setTimeout(() => {
    showForm.value = true
  }, 300)
})

// 处理验证
const handleVerify = async () => {
  if (!verifyFormRef.value) return

  await verifyFormRef.value.validate(async (valid) => {
    if (!valid) return

    verifyLoading.value = true
    try {
      const response = await authApi.verifyAccount({
        username: verifyForm.username,
        phone: verifyForm.phone,
        email: verifyForm.email
      })

      if (response.code === 200 && response.data === true) {
        ElMessage.success('身份验证成功')
        currentStep.value = 1
      } else {
        ElMessage.error('信息不匹配，请检查账号、手机号和邮箱是否正确')
      }
    } catch (error: any) {
      console.error('验证错误:', error)
      ElMessage.error(error.response?.data?.message || '验证失败，请检查输入信息')
    } finally {
      verifyLoading.value = false
    }
  })
}

// 处理重置密码
const handleReset = async () => {
  if (!resetFormRef.value) return

  await resetFormRef.value.validate(async (valid) => {
    if (!valid) return

    resetLoading.value = true
    try {
      const response = await authApi.resetPassword({
        username: verifyForm.username,
        phone: verifyForm.phone,
        email: verifyForm.email,
        newPassword: resetForm.newPassword
      })

      if (response.code === 200) {
        ElMessage.success('密码重置成功')
        currentStep.value = 2
      } else {
        ElMessage.error(response.message || '密码重置失败')
      }
    } catch (error: any) {
      console.error('重置错误:', error)
      ElMessage.error(error.response?.data?.message || '密码重置失败，请稍后重试')
    } finally {
      resetLoading.value = false
    }
  })
}

// 跳转登录
const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.forgot-password-page {
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

// 主容器
.main-container {
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
  justify-content: center;
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

// 右侧表单区域
.form-section {
  flex: 0 0 480px;
  padding: 40px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  overflow-y: auto;
  max-height: 90vh;
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

// 步骤条
.steps {
  margin-bottom: 30px;

  :deep(.el-step__title) {
    font-size: 13px;
  }
}

// 步骤内容
.step-content {
  min-height: 400px;
}

.success-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 0;

  .success-icon {
    margin-bottom: 24px;
  }

  .form-title {
    text-align: center;
  }

  .form-subtitle {
    text-align: center;
    margin-bottom: 30px;
  }
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #888;
  margin-bottom: 30px;
}

// 表单样式
.forgot-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
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
    height: 48px;
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

// 提交按钮
.submit-btn {
  width: 100%;
  height: 48px;
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
  margin-top: 10px;

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

// 返回登录
.back-to-login {
  margin-top: 30px;
  text-align: center;
  font-size: 14px;
  color: #666;

  .link-text {
    margin-left: 5px;
    font-size: 14px;
  }
}

// 页脚
.page-footer {
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
  .main-container {
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
  .forgot-password-page {
    padding: 0;
  }

  .main-container {
    border-radius: 0;
    min-height: 100vh;
  }

  .form-section {
    padding: 30px 24px;
  }
}
</style>
