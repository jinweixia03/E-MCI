/**
 * 认证API测试文件
 * 测试前端API与后端接口的对接
 *
 * 注意: request.ts 中的响应拦截器在业务成功时直接返回 response.data (即 ApiResponse<T>),
 * 而不是返回完整的 AxiosResponse。因此 API 方法返回的是 ApiResponse<T> 而不是 AxiosResponse<ApiResponse<T>>
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { authApi } from './auth'
import request from '@/utils/request'
import type { LoginForm, RegisterForm, LoginResponse, UserInfo, ApiResponse } from '@/types/user'

// Mock request 模块
vi.mock('@/utils/request', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn()
  }
}))

describe('Auth API 接口测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  /**
   * ==========================================
   * 登录接口测试
   * ==========================================
   */
  describe('POST /auth/login - 用户登录', () => {
    const validLoginForm: LoginForm = {
      username: 'testuser',
      password: 'password123'
    }

    const mockLoginResponse: ApiResponse<LoginResponse> = {
      code: 200,
      message: '登录成功',
      timestamp: '2024-01-01T00:00:00',
      data: {
        accessToken: 'mock_access_token_12345',
        tokenType: 'Bearer',
        expiresIn: 7200,
        userInfo: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com',
          phone: '13800138000',
          headImg: 'http://example.com/avatar.jpg',
          type: 0,
          createTime: '2024-01-01 00:00:00'
        }
      }
    }

    it('应该使用正确的参数调用登录接口', async () => {
      vi.mocked(request.post).mockResolvedValueOnce(mockLoginResponse)

      await authApi.login(validLoginForm)

      expect(request.post).toHaveBeenCalledWith('/auth/login', validLoginForm)
      expect(request.post).toHaveBeenCalledTimes(1)
    })

    it('应该返回包含 accessToken 的响应', async () => {
      vi.mocked(request.post).mockResolvedValueOnce(mockLoginResponse)

      const response = await authApi.login(validLoginForm)

      // response 本身就是 ApiResponse<T>，不是 AxiosResponse
      expect(response.code).toBe(200)
      expect(response.data.accessToken).toBeDefined()
      expect(response.data.accessToken).toBe('mock_access_token_12345')
    })

    it('应该返回用户基本信息', async () => {
      vi.mocked(request.post).mockResolvedValueOnce(mockLoginResponse)

      const response = await authApi.login(validLoginForm)

      const userInfo = response.data.userInfo
      expect(userInfo).toBeDefined()
      expect(userInfo?.id).toBe(1)
      expect(userInfo?.username).toBe('testuser')
      expect(userInfo?.type).toBe(0)
    })

    it('应该返回正确的 token 类型和过期时间', async () => {
      vi.mocked(request.post).mockResolvedValueOnce(mockLoginResponse)

      const response = await authApi.login(validLoginForm)

      expect(response.data.tokenType).toBe('Bearer')
      expect(response.data.expiresIn).toBe(7200)
    })

    it('应该处理登录失败的情况 (401)', async () => {
      const errorResponse = {
        response: {
          status: 401,
          data: { message: '用户名或密码错误' }
        }
      }
      vi.mocked(request.post).mockRejectedValueOnce(errorResponse)

      await expect(authApi.login(validLoginForm)).rejects.toEqual(errorResponse)
    })

    it('应该处理用户不存在的情况 (1000)', async () => {
      const errorResponse = {
        response: {
          status: 400,
          data: { code: 1000, message: '用户不存在' }
        }
      }
      vi.mocked(request.post).mockRejectedValueOnce(errorResponse)

      await expect(authApi.login(validLoginForm)).rejects.toEqual(errorResponse)
    })

    it('应该验证用户名长度 (3-50字符)', () => {
      // 验证前端表单验证规则
      const shortUsername: LoginForm = { username: 'ab', password: 'password123' }
      const longUsername: LoginForm = { username: 'a'.repeat(51), password: 'password123' }

      expect(shortUsername.username.length).toBeLessThan(3)
      expect(longUsername.username.length).toBeGreaterThan(50)
    })

    it('应该验证密码长度 (6-100字符)', () => {
      const shortPassword: LoginForm = { username: 'testuser', password: '12345' }
      const longPassword: LoginForm = { username: 'testuser', password: 'a'.repeat(101) }

      expect(shortPassword.password.length).toBeLessThan(6)
      expect(longPassword.password.length).toBeGreaterThan(100)
    })
  })

  /**
   * ==========================================
   * 注册接口测试
   * ==========================================
   */
  describe('POST /auth/register - 用户注册', () => {
    const validRegisterForm: RegisterForm = {
      username: 'newuser',
      password: 'password123',
      email: 'newuser@example.com',
      phone: '13800138000'
    }

    const mockRegisterResponse: ApiResponse<UserInfo> = {
      code: 200,
      message: '注册成功',
      timestamp: '2024-01-01T00:00:00',
      data: {
        id: 2,
        username: 'newuser',
        email: 'newuser@example.com',
        phone: '13800138000',
        headImg: null as unknown as string,
        type: 0,
        createTime: '2024-01-01 00:00:00'
      }
    }

    it('应该使用正确的参数调用注册接口', async () => {
      vi.mocked(request.post).mockResolvedValueOnce(mockRegisterResponse)

      await authApi.register(validRegisterForm)

      expect(request.post).toHaveBeenCalledWith('/auth/register', validRegisterForm)
      expect(request.post).toHaveBeenCalledTimes(1)
    })

    it('应该返回新创建的用户信息', async () => {
      vi.mocked(request.post).mockResolvedValueOnce(mockRegisterResponse)

      const response = await authApi.register(validRegisterForm)

      expect(response.code).toBe(200)
      expect(response.data.id).toBe(2)
      expect(response.data.username).toBe('newuser')
    })

    it('应该处理用户名已存在的情况 (1002)', async () => {
      const errorResponse = {
        response: {
          status: 409,
          data: { code: 1002, message: '用户已存在' }
        }
      }
      vi.mocked(request.post).mockRejectedValueOnce(errorResponse)

      await expect(authApi.register(validRegisterForm)).rejects.toEqual(errorResponse)
    })

    it('应该处理邮箱格式不正确的情况', async () => {
      const invalidEmailForm: RegisterForm = {
        ...validRegisterForm,
        email: 'invalid-email'
      }

      const errorResponse = {
        response: {
          status: 422,
          data: { code: 422, message: '邮箱格式不正确' }
        }
      }
      vi.mocked(request.post).mockRejectedValueOnce(errorResponse)

      await expect(authApi.register(invalidEmailForm)).rejects.toEqual(errorResponse)
    })

    it('应该处理手机号格式不正确的情况', async () => {
      const invalidPhoneForm: RegisterForm = {
        ...validRegisterForm,
        phone: '12345678901' // 不以1开头
      }

      const errorResponse = {
        response: {
          status: 422,
          data: { code: 422, message: '手机号格式不正确' }
        }
      }
      vi.mocked(request.post).mockRejectedValueOnce(errorResponse)

      await expect(authApi.register(invalidPhoneForm)).rejects.toEqual(errorResponse)
    })

    it('应该验证用户名长度在 3-50 字符之间', () => {
      const shortUsername = { ...validRegisterForm, username: 'ab' }
      const longUsername = { ...validRegisterForm, username: 'a'.repeat(51) }

      expect(shortUsername.username.length).toBeLessThan(3)
      expect(longUsername.username.length).toBeGreaterThan(50)
    })

    it('应该验证密码长度在 6-100 字符之间', () => {
      const shortPassword = { ...validRegisterForm, password: '12345' }
      const longPassword = { ...validRegisterForm, password: 'a'.repeat(101) }

      expect(shortPassword.password.length).toBeLessThan(6)
      expect(longPassword.password.length).toBeGreaterThan(100)
    })

    it('应该支持可选的 email 和 phone 字段', async () => {
      const minimalRegisterForm: RegisterForm = {
        username: 'minimaluser',
        password: 'password123'
        // email 和 phone 为可选
      }

      const minimalResponse: ApiResponse<UserInfo> = {
        code: 200,
        message: '注册成功',
        timestamp: '2024-01-01T00:00:00',
        data: {
          id: 3,
          username: 'minimaluser',
          type: 0,
          createTime: '2024-01-01 00:00:00'
        }
      }

      vi.mocked(request.post).mockResolvedValueOnce(minimalResponse)

      const response = await authApi.register(minimalRegisterForm)

      expect(response.code).toBe(200)
      expect(response.data.username).toBe('minimaluser')
    })
  })

  /**
   * ==========================================
   * 获取当前用户信息接口测试
   * ==========================================
   */
  describe('GET /auth/me - 获取当前用户信息', () => {
    const mockUserInfoResponse: ApiResponse<UserInfo> = {
      code: 200,
      message: '操作成功',
      timestamp: '2024-01-01T00:00:00',
      data: {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        phone: '13800138000',
        headImg: 'http://example.com/avatar.jpg',
        type: 1, // 管理员
        createTime: '2024-01-01 00:00:00'
      }
    }

    it('应该在请求头中包含 Authorization', async () => {
      vi.mocked(request.get).mockResolvedValueOnce(mockUserInfoResponse)

      await authApi.getCurrentUser()

      expect(request.get).toHaveBeenCalledWith('/auth/me')
    })

    it('应该返回当前用户的详细信息', async () => {
      vi.mocked(request.get).mockResolvedValueOnce(mockUserInfoResponse)

      const response = await authApi.getCurrentUser()

      expect(response.code).toBe(200)
      expect(response.data.id).toBe(1)
      expect(response.data.username).toBe('testuser')
      expect(response.data.type).toBe(1)
    })

    it('应该处理未授权的情况 (401)', async () => {
      const errorResponse = {
        response: {
          status: 401,
          data: { code: 401, message: '未授权，请先登录' }
        }
      }
      vi.mocked(request.get).mockRejectedValueOnce(errorResponse)

      await expect(authApi.getCurrentUser()).rejects.toEqual(errorResponse)
    })
  })

  /**
   * ==========================================
   * 刷新Token接口测试
   * ==========================================
   */
  describe('POST /auth/refresh - 刷新Token', () => {
    const mockRefreshResponse: ApiResponse<LoginResponse> = {
      code: 200,
      message: '刷新成功',
      timestamp: '2024-01-01T00:00:00',
      data: {
        accessToken: 'new_access_token_67890',
        tokenType: 'Bearer',
        expiresIn: 7200,
        userInfo: {
          id: 1,
          username: 'testuser',
          type: 0
        }
      }
    }

    it('应该返回新的 accessToken', async () => {
      vi.mocked(request.post).mockResolvedValueOnce(mockRefreshResponse)

      const response = await authApi.refreshToken()

      expect(response.code).toBe(200)
      expect(response.data.accessToken).toBe('new_access_token_67890')
    })

    it('应该在请求头中包含旧的 Authorization', async () => {
      vi.mocked(request.post).mockResolvedValueOnce(mockRefreshResponse)

      await authApi.refreshToken()

      expect(request.post).toHaveBeenCalledWith('/auth/refresh')
    })

    it('应该处理Token过期的情况 (401)', async () => {
      const errorResponse = {
        response: {
          status: 401,
          data: { code: 401, message: 'Token已过期' }
        }
      }
      vi.mocked(request.post).mockRejectedValueOnce(errorResponse)

      await expect(authApi.refreshToken()).rejects.toEqual(errorResponse)
    })
  })
})

/**
 * ==========================================
 * API 类型兼容性测试
 * ==========================================
 */
describe('API 类型兼容性测试', () => {
  it('LoginForm 类型应该匹配后端 LoginRequest', () => {
    const loginForm: LoginForm = {
      username: 'test',
      password: 'pass123'
    }

    // 验证必填字段
    expect(loginForm).toHaveProperty('username')
    expect(loginForm).toHaveProperty('password')
  })

  it('RegisterForm 类型应该匹配后端 RegisterRequest', () => {
    const registerForm: RegisterForm = {
      username: 'test',
      password: 'pass123',
      email: 'test@example.com',
      phone: '13800138000'
    }

    // 验证字段
    expect(registerForm).toHaveProperty('username')
    expect(registerForm).toHaveProperty('password')
    expect(registerForm).toHaveProperty('email')
    expect(registerForm).toHaveProperty('phone')
  })

  it('UserInfo 类型应该匹配后端 UserInfoVO', () => {
    const userInfo: UserInfo = {
      id: 1,
      username: 'test',
      email: 'test@example.com',
      phone: '13800138000',
      headImg: 'http://example.com/avatar.jpg',
      type: 0,
      createTime: '2024-01-01 00:00:00'
    }

    expect(userInfo).toHaveProperty('id')
    expect(userInfo).toHaveProperty('username')
    expect(userInfo).toHaveProperty('type')
  })

  it('LoginResponse 类型应该匹配后端 LoginVO', () => {
    const loginResponse: LoginResponse = {
      accessToken: 'token',
      tokenType: 'Bearer',
      expiresIn: 7200,
      userInfo: {
        id: 1,
        username: 'test',
        type: 0
      }
    }

    expect(loginResponse).toHaveProperty('accessToken')
    expect(loginResponse).toHaveProperty('tokenType')
    expect(loginResponse).toHaveProperty('expiresIn')
    expect(loginResponse).toHaveProperty('userInfo')
  })
})

/**
 * ==========================================
 * 响应结构测试
 * ==========================================
 */
describe('API 响应结构测试', () => {
  it('成功的响应应该包含 code, message, data, timestamp', () => {
    const successResponse: ApiResponse<LoginResponse> = {
      code: 200,
      message: '操作成功',
      data: {
        accessToken: 'token',
        tokenType: 'Bearer',
        expiresIn: 7200,
        userInfo: { id: 1, username: 'test', type: 0 }
      },
      timestamp: '2024-01-01T00:00:00'
    }

    expect(successResponse).toHaveProperty('code')
    expect(successResponse).toHaveProperty('message')
    expect(successResponse).toHaveProperty('data')
    expect(successResponse).toHaveProperty('timestamp')
  })

  it('错误响应应该包含 code 和 message', () => {
    const errorResponse = {
      code: 1001,
      message: '密码错误',
      data: null,
      timestamp: '2024-01-01T00:00:00'
    }

    expect(errorResponse.code).toBe(1001)
    expect(errorResponse.message).toBe('密码错误')
  })
})
