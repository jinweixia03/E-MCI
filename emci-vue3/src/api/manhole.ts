import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Manhole, ManholeCreateParams, ManholeQueryParams, ManholeUpdateParams, ManholeMapVO, ManholeClusterVO, ManholeMapStats, ManholeMapQueryParams } from '@/types/manhole'

export const manholeApi = {
  /**
   * 创建井盖
   */
  create(data: ManholeCreateParams): Promise<ApiResponse<Manhole>> {
    return request.post<Manhole>('/manhole', data)
  },

  /**
   * 更新井盖
   */
  update(id: number, data: ManholeUpdateParams): Promise<ApiResponse<Manhole>> {
    return request.put<Manhole>(`/manhole/${id}`, data)
  },

  /**
   * 删除井盖
   */
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete<void>(`/manhole/${id}`)
  },

  /**
   * 获取井盖详情
   */
  getById(id: number): Promise<ApiResponse<Manhole>> {
    return request.get<Manhole>(`/manhole/${id}`)
  },

  /**
   * 分页查询井盖
   */
  pageQuery(params: ManholeQueryParams): Promise<ApiResponse<PageResult<Manhole>>> {
    return request.get<PageResult<Manhole>>('/manhole/page', { params })
  },

  /**
   * 获取所有井盖
   */
  getAll(): Promise<ApiResponse<Manhole[]>> {
    return request.get<Manhole[]>('/manhole/all')
  },

  /**
   * 根据编号查询
   */
  getByManholeId(manholeId: string): Promise<ApiResponse<Manhole>> {
    return request.get<Manhole>(`/manhole/by-code/${manholeId}`)
  },

  // ==================== 地图相关接口 ====================

  /**
   * 地图范围查询井盖
   */
  queryForMap(params: ManholeMapQueryParams): Promise<ApiResponse<ManholeMapVO[]>> {
    return request.get<ManholeMapVO[]>('/manhole/map/query', { params })
  },

  /**
   * 获取地图聚合数据
   */
  getClusterData(params: ManholeMapQueryParams): Promise<ApiResponse<ManholeClusterVO[]>> {
    return request.get<ManholeClusterVO[]>('/manhole/map/cluster', { params })
  },

  /**
   * 获取地图统计信息
   */
  getMapStats(params?: ManholeMapQueryParams): Promise<ApiResponse<ManholeMapStats>> {
    return request.get<ManholeMapStats>('/manhole/map/stats', { params })
  },

  /**
   * 获取附近井盖
   */
  getNearbyManholes(longitude: number, latitude: number, radius = 1000): Promise<ApiResponse<ManholeMapVO[]>> {
    return request.get<ManholeMapVO[]>('/manhole/map/nearby', {
      params: { longitude, latitude, radius }
    })
  },

  /**
   * 获取所有城市列表
   */
  getAllCities(): Promise<ApiResponse<string[]>> {
    return request.get<string[]>('/manhole/cities')
  },

  /**
   * 获取城市下的区县列表
   */
  getDistrictsByCity(city: string): Promise<ApiResponse<string[]>> {
    return request.get<string[]>(`/manhole/cities/${encodeURIComponent(city)}/districts`)
  }
}
