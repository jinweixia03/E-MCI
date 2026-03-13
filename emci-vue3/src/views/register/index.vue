<template>
  <div class="register-page">
    <!-- 背景动画元素 -->
    <div class="bg-animation">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>

    <!-- 注册容器 -->
    <div class="register-container" :class="{ 'fade-in': showContainer }">
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
            <p class="greeting">加入我们</p>
            <p class="welcome">注册 E-MCI 账号</p>
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
          <img src="../../assets/loginLogo.jpg" alt="register" />
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="form-section">
        <div class="form-wrapper" :class="{ 'slide-up': showForm }">
          <h2 class="form-title">注册账号</h2>
          <p class="form-subtitle">请填写以下信息完成注册</p>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
            @keyup.enter="handleRegister"
          >
            <!-- 用户名输入框 -->
            <el-form-item prop="username" class="form-item-animated" :class="{ 'focused': usernameFocused }">
              <div class="input-label">
                <span>用户名</span>
              </div>
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名 (3-20个字符)"
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
                <span>密码</span>
              </div>
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码 (6-20个字符)"
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

            <!-- 确认密码输入框 -->
            <el-form-item prop="confirmPassword" class="form-item-animated" :class="{ 'focused': confirmPasswordFocused }">
              <div class="input-label">
                <span>确认密码</span>
              </div>
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
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

            <!-- 邮箱输入框 (必填) -->
            <el-form-item prop="email" class="form-item-animated" :class="{ 'focused': emailFocused }">
              <div class="input-label">
                <span>邮箱 <span class="required">*</span></span>
              </div>
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱地址"
                size="large"
                class="custom-input"
                @focus="emailFocused = true"
                @blur="handleEmailBlur"
              >
                <template #prefix>
                  <el-icon :size="18"><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 手机号输入框 (必填) -->
            <el-form-item prop="phone" class="form-item-animated" :class="{ 'focused': phoneFocused }">
              <div class="input-label">
                <span>手机号 <span class="required">*</span></span>
              </div>
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                size="large"
                class="custom-input"
                @focus="phoneFocused = true"
                @blur="handlePhoneBlur"
              >
                <template #prefix>
                  <el-icon :size="18"><Iphone /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 用户协议 -->
            <div class="form-options">
              <el-checkbox v-model="agreeTerms" class="terms-checkbox">
                我已阅读并同意 <a @click.stop="showTerms">用户协议</a> 和 <a @click.stop="showPrivacy">隐私政策</a>
              </el-checkbox>
            </div>

            <!-- 注册按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="register-btn"
                @click="handleRegister"
                :disabled="!agreeTerms"
              >
                <span class="btn-text">注 册</span>
                <el-icon class="btn-icon" v-if="!loading"><ArrowRight /></el-icon>
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 返回登录 -->
          <div class="back-to-login">
            <span>已有账号？</span>
            <a class="link-text" @click="goLogin">立即登录</a>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="register-footer">
      <p>© 2024 E-MCI 智能井盖检测系统 | 版权所有</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight, Monitor, Check, Message, Iphone } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

// 响应式状态
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const agreeTerms = ref(false)
const showContainer = ref(false)
const showForm = ref(false)
const usernameFocused = ref(false)
const passwordFocused = ref(false)
const confirmPasswordFocused = ref(false)
const emailFocused = ref(false)
const phoneFocused = ref(false)

// 特性列表
const features = [
  'AI 智能检测识别',
  '实时井盖监控',
  '无人机巡检管理',
  '数据可视化分析'
]

// 表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

// 自定义验证规则：确认密码
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 自定义验证规则：手机号
const validatePhone = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在 3-20 个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在 6-20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
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

// 检查邮箱是否已存在
const checkEmailExists = async (email: string): Promise<boolean> => {
  try {
    const response = await authApi.checkEmail(email)
    return response.data === true
  } catch (error) {
    return false
  }
}

// 检查手机号是否已存在
const checkPhoneExists = async (phone: string): Promise<boolean> => {
  try {
    const response = await authApi.checkPhone(phone)
    return response.data === true
  } catch (error) {
    return false
  }
}

// 邮箱失去焦点时检查
const handleEmailBlur = async () => {
  emailFocused.value = false
  if (registerForm.email && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    const exists = await checkEmailExists(registerForm.email)
    if (exists) {
      ElMessage.warning('该邮箱已被注册')
    }
  }
}

// 手机号失去焦点时检查
const handlePhoneBlur = async () => {
  phoneFocused.value = false
  if (registerForm.phone && /^1[3-9]\d{9}$/.test(registerForm.phone)) {
    const exists = await checkPhoneExists(registerForm.phone)
    if (exists) {
      ElMessage.warning('该手机号已被注册')
    }
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (!agreeTerms.value) {
      ElMessage.warning('请阅读并同意用户协议和隐私政策')
      return
    }

    // 检查邮箱是否已存在
    const emailExists = await checkEmailExists(registerForm.email)
    if (emailExists) {
      ElMessage.error('该邮箱已被注册，请使用其他邮箱')
      return
    }

    // 检查手机号是否已存在
    const phoneExists = await checkPhoneExists(registerForm.phone)
    if (phoneExists) {
      ElMessage.error('该手机号已被注册，请使用其他手机号')
      return
    }

    loading.value = true
    try {
      const response = await authApi.register({
        username: registerForm.username,
        password: registerForm.password,
        email: registerForm.email,
        phone: registerForm.phone
      })

      if (response.code === 200) {
        ElMessage.success('注册成功！请登录')
        router.push('/login')
      } else {
        ElMessage.error(response.message || '注册失败')
      }
    } catch (error: any) {
      console.error('注册错误:', error)
      const errorMsg = error.response?.data?.message
      if (errorMsg?.includes('邮箱')) {
        ElMessage.error('该邮箱已被注册')
      } else if (errorMsg?.includes('手机号')) {
        ElMessage.error('该手机号已被注册')
      } else if (errorMsg?.includes('用户名')) {
        ElMessage.error('该用户名已被注册')
      } else {
        ElMessage.error(errorMsg || '网络错误，请稍后重试')
      }
    } finally {
      loading.value = false
    }
  })
}

// 跳转登录
const goLogin = () => {
  router.push('/login')
}

// 显示用户协议
const showTerms = () => {
  ElMessage.info('用户协议功能开发中...')
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessage.info('隐私政策功能开发中...')
}
</script>

<style scoped lang="scss">
.register-page {
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

// 注册容器
.register-container {
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
  padding: 40px 60px;
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

.form-title {
  font-size: 32px;
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
.register-form {
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

// 必填项标记
.required {
  color: #f56c6c;
  margin-left: 2px;
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

// 用户协议
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

:deep(.terms-checkbox) {
  .el-checkbox__label {
    font-size: 13px;
    color: #666;

    a {
      color: #0076f0;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .el-checkbox__input.is-checked .el-checkbox__inner {
    background-color: #0076f0;
    border-color: #0076f0;
  }
}

// 注册按钮
.register-btn {
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

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(0, 118, 240, 0.3);
  }

  &:active {
    transform: translateY(0);
  }

  &[disabled] {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
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
.register-footer {
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
  .register-container {
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
  .register-page {
    padding: 0;
  }

  .register-container {
    border-radius: 0;
    min-height: 100vh;
  }

  .form-section {
    padding: 30px 24px;
  }
}
</style>
