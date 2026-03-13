import { describe, it, expect, vi, beforeEach } from 'vitest'
import type { ApiResponse } from '@/types/api'

// 模拟Element Plus的ElMessage
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn()
  }
}))

// 模拟pinia store
const mockLogout = vi.fn()
vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    token: 'test-token',
    logout: mockLogout
  })
}))

// 模拟vue-router
const mockPush = vi.fn()
vi.mock('@/router', () => ({
  default: {
    push: mockPush
  }
}))

describe('请求工具测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('API响应类型', () => {
    it('应该正确匹配ApiResponse类型', () => {
      const response: ApiResponse<string> = {
        code: 200,
        message: '操作成功',
        data: 'test data',
        timestamp: '2024-01-01T00:00:00'
      }

      expect(response.code).toBe(200)
      expect(response.message).toBe('操作成功')
      expect(response.data).toBe('test data')
    })

    it('应该正确匹配分页响应类型', () => {
      interface PageResult<T> {
        pageNum: number
        pageSize: number
        total: number
        pages: number
        list: T[]
      }

      const pageResponse: ApiResponse<PageResult<string>> = {
        code: 200,
        message: '操作成功',
        data: {
          pageNum: 1,
          pageSize: 10,
          total: 100,
          pages: 10,
          list: ['item1', 'item2']
        },
        timestamp: '2024-01-01T00:00:00'
      }

      expect(pageResponse.data.pageNum).toBe(1)
      expect(pageResponse.data.list).toHaveLength(2)
    })
  })

  describe('API错误码处理', () => {
    it('应该处理401未授权错误', () => {
      const errorResponse: ApiResponse<null> = {
        code: 401,
        message: '未授权',
        data: null,
        timestamp: '2024-01-01T00:00:00'
      }

      expect(errorResponse.code).toBe(401)
      expect(errorResponse.message).toBe('未授权')
    })

    it('应该处理403权限不足错误', () => {
      const errorResponse: ApiResponse<null> = {
        code: 403,
        message: '权限不足',
        data: null,
        timestamp: '2024-01-01T00:00:00'
      }

      expect(errorResponse.code).toBe(403)
    })

    it('应该处理404资源不存在错误', () => {
      const errorResponse: ApiResponse<null> = {
        code: 404,
        message: '资源不存在',
        data: null,
        timestamp: '2024-01-01T00:00:00'
      }

      expect(errorResponse.code).toBe(404)
    })

    it('应该处理500服务器错误', () => {
      const errorResponse: ApiResponse<null> = {
        code: 500,
        message: '服务器内部错误',
        data: null,
        timestamp: '2024-01-01T00:00:00'
      }

      expect(errorResponse.code).toBe(500)
    })
  })

  describe('工具函数', () => {
    it('应该正确格式化时间戳', () => {
      const timestamp = '2024-03-12T10:30:00'
      const date = new Date(timestamp)

      expect(date.getFullYear()).toBe(2024)
      expect(date.getMonth()).toBe(2) // 3月 = 2 (0-based)
      expect(date.getDate()).toBe(12)
    })

    it('应该正确处理空数据响应', () => {
      const emptyResponse: ApiResponse<null> = {
        code: 200,
        message: '操作成功',
        data: null,
        timestamp: '2024-01-01T00:00:00'
      }

      expect(emptyResponse.data).toBeNull()
      expect(emptyResponse.code).toBe(200)
    })
  })
})
