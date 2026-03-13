/**
 * 用户相关类型定义
 */

export interface UserInfo {
  id: number
  username: string
  email?: string
  phone?: string
  headImg?: string
  type: number
  createTime?: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  email?: string
  phone?: string
}

export interface LoginResponse {
  accessToken?: string
  token?: string
  tokenType?: string
  expiresIn?: number
  userInfo?: UserInfo
  [key: string]: any
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
  path?: string
}
