import request from '@/utils/request'
import type { ApiResponse, LoginForm, RegisterForm, LoginResponse, UserInfo } from '@/types/user'

export const authApi = {
  /**
   * 用户登录
   */
  login(data: LoginForm): Promise<ApiResponse<LoginResponse>> {
    return request.post<LoginResponse>('/auth/login', data)
  },

  /**
   * 用户注册
   */
  register(data: RegisterForm): Promise<ApiResponse<UserInfo>> {
    return request.post<UserInfo>('/auth/register', data)
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser(): Promise<ApiResponse<UserInfo>> {
    return request.get<UserInfo>('/auth/me')
  },

  /**
   * 刷新Token
   */
  refreshToken(): Promise<ApiResponse<LoginResponse>> {
    return request.post<LoginResponse>('/auth/refresh')
  },

  /**
   * 检查邮箱是否已存在
   */
  checkEmail(email: string): Promise<ApiResponse<boolean>> {
    return request.get<boolean>('/auth/check-email', { params: { email } })
  },

  /**
   * 检查手机号是否已存在
   */
  checkPhone(phone: string): Promise<ApiResponse<boolean>> {
    return request.get<boolean>('/auth/check-phone', { params: { phone } })
  },

  /**
   * 找回密码 - 验证账号信息
   */
  verifyAccount(data: { username: string; phone: string; email: string }): Promise<ApiResponse<boolean>> {
    return request.post<boolean>('/auth/verify-account', data)
  },

  /**
   * 重置密码
   */
  resetPassword(data: { username: string; phone: string; email: string; newPassword: string }): Promise<ApiResponse<void>> {
    return request.post<void>('/auth/reset-password', data)
  }
}
