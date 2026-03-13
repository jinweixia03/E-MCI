import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type { DashboardStats } from '@/types/dashboard'

export const dashboardApi = {
  /**
   * 获取数据大屏统计数据
   */
  getStats(): Promise<ApiResponse<DashboardStats>> {
    return request.get<DashboardStats>('/dashboard/stats')
  }
}
