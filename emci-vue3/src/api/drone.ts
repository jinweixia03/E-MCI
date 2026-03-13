import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Drone, DroneCreateParams, DroneUpdateParams } from '@/types/drone'

export const droneApi = {
  /**
   * 创建无人机
   */
  create(data: DroneCreateParams): Promise<ApiResponse<Drone>> {
    return request.post<Drone>('/drone', data)
  },

  /**
   * 更新无人机
   */
  update(id: number, data: DroneUpdateParams): Promise<ApiResponse<Drone>> {
    return request.put<Drone>(`/drone/${id}`, data)
  },

  /**
   * 更新状态
   */
  updateStatus(id: number, status: number): Promise<ApiResponse<Drone>> {
    return request.patch<Drone>(`/drone/${id}/status`, { status })
  },

  /**
   * 删除无人机
   */
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete<void>(`/drone/${id}`)
  },

  /**
   * 获取无人机详情
   */
  getById(id: number): Promise<ApiResponse<Drone>> {
    return request.get<Drone>(`/drone/${id}`)
  },

  /**
   * 分页查询无人机
   */
  pageQuery(pageNum = 1, pageSize = 10, status?: number): Promise<ApiResponse<PageResult<Drone>>> {
    return request.get<PageResult<Drone>>('/drone/page', {
      params: { pageNum, pageSize, status }
    })
  },

  /**
   * 获取所有无人机
   */
  getAll(): Promise<ApiResponse<Drone[]>> {
    return request.get<Drone[]>('/drone/all')
  },

  /**
   * 获取运行中无人机
   */
  getActiveDrones(): Promise<ApiResponse<Drone[]>> {
    return request.get<Drone[]>('/drone/active')
  }
}
