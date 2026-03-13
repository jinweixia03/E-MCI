/**
 * API通用类型定义
 */

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
  path?: string
}

export interface PageResult<T> {
  pageNum: number
  pageSize: number
  total: number
  pages: number
  list: T[]
}

export interface PageParams {
  pageNum?: number
  pageSize?: number
}
