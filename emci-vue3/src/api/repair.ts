import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/api'

export interface Repair {
  id: number
  repairId: number
  detectionId?: number
  manholeId?: string
  principal?: string
  repairUserId?: number
  repairUserName?: string
  status: number
  statusText?: string
  defectTypes?: string
  startTime?: string
  endTime?: string
  beforeImg?: string
  afterImg?: string
  remark?: string
  createTime?: string
  updateTime?: string
  assignedTime?: string
  completeTime?: string
  // 检测记录相关信息
  detectionNo?: string
  detectionTime?: string
  detectionOriginalImgUrl?: string
  detectionResultImgUrl?: string
  hasDefect?: number
  defectCount?: number
  primaryDefectType?: string
  primaryConfidence?: number
}

export interface RepairCreateParams {
  detectionId: number
  manholeId?: string
  repairUserId?: number
  remark?: string
  status?: number
}

export interface RepairUpdateParams {
  repairUserId?: number
  remark?: string
  status?: number
}

export const repairApi = {
  /**
   * 创建维修记录
   */
  create(data: RepairCreateParams): Promise<ApiResponse<Repair>> {
    return request.post<Repair>('/repair', data)
  },

  /**
   * 更新维修记录
   */
  update(id: number, data: RepairUpdateParams): Promise<ApiResponse<Repair>> {
    return request.put<Repair>(`/repair/${id}`, data)
  },

  /**
   * 删除维修记录
   */
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete<void>(`/repair/${id}`)
  },

  /**
   * 获取维修记录详情
   */
  getById(id: number): Promise<ApiResponse<Repair>> {
    return request.get<Repair>(`/repair/${id}`)
  },

  /**
   * 分页查询维修记录
   */
  pageQuery(pageNum = 1, pageSize = 10, status?: number, detectionId?: number): Promise<ApiResponse<PageResult<Repair>>> {
    return request.get<PageResult<Repair>>('/repair/page', {
      params: { pageNum, pageSize, status, detectionId }
    })
  },

  /**
   * 获取所有维修记录
   */
  getAll(): Promise<ApiResponse<Repair[]>> {
    return request.get<Repair[]>('/repair/all')
  },

  /**
   * 根据状态查询
   */
  getByStatus(status: number): Promise<ApiResponse<Repair[]>> {
    return request.get<Repair[]>(`/repair/by-status/${status}`)
  },

  /**
   * 开始维修
   */
  start(id: number): Promise<ApiResponse<Repair>> {
    return request.post<Repair>(`/repair/${id}/start`)
  },

  /**
   * 完成维修
   */
  complete(id: number, afterImg?: string, remark?: string): Promise<ApiResponse<Repair>> {
    return request.post<Repair>(`/repair/${id}/complete`, null, {
      params: { afterImg, remark }
    })
  },

  /**
   * 分配维修任务
   */
  assign(id: number, repairUserId: number): Promise<ApiResponse<Repair>> {
    return request.post<Repair>(`/repair/${id}/assign`, null, {
      params: { repairUserId }
    })
  },

  /**
   * 重新分配维修任务
   */
  reassign(id: number, repairUserId: number): Promise<ApiResponse<Repair>> {
    return request.post<Repair>(`/repair/${id}/reassign`, null, {
      params: { repairUserId }
    })
  },

  /**
   * 获取可分配的维修人员列表
   */
  getRepairUsers(): Promise<ApiResponse<{id: number, username: string}[]>> {
    return request.get<{id: number, username: string}[]>('/repair/users')
  },

  /**
   * 确认维修完成
   */
  confirm(id: number): Promise<ApiResponse<Repair>> {
    return request.post<Repair>(`/repair/${id}/confirm`)
  },

  /**
   * 获取维修统计
   */
  getStats(): Promise<ApiResponse<{pending: number, inProgress: number, toConfirm: number, completed: number, total: number}>> {
    return request.get('/repair/stats')
  },

  /**
   * 维修员分页查询自己的维修记录
   */
  pageQueryMyRepairs(repairUserId: number, pageNum = 1, pageSize = 10, status?: number, detectionId?: number): Promise<ApiResponse<PageResult<Repair>>> {
    return request.get<PageResult<Repair>>('/repair/my/page', {
      params: { pageNum, pageSize, status, detectionId, repairUserId }
    })
  },

  /**
   * 获取维修员的维修统计
   */
  getMyStats(repairUserId: number): Promise<ApiResponse<{pending: number, inProgress: number, toConfirm: number, completed: number, total: number}>> {
    return request.get('/repair/my/stats', {
      params: { repairUserId }
    })
  }
}
