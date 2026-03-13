import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type { Drone, DroneTask, DroneDeployRequest, PathPlanResult, DroneUpdateParams } from '@/types/drone'

export const droneApi = {
  /**
   * 获取无人机列表（含任务信息）
   */
  getList(): Promise<ApiResponse<Drone[]>> {
    return request.get<Drone[]>('/drone/list')
  },

  /**
   * 获取可用无人机列表
   */
  getAvailable(): Promise<ApiResponse<Drone[]>> {
    return request.get<Drone[]>('/drone/available')
  },

  /**
   * 获取无人机详情
   */
  getDetail(id: number): Promise<ApiResponse<Drone>> {
    return request.get<Drone>(`/drone/${id}`)
  },

  /**
   * 部署无人机：设置检测区域并规划路径
   */
  deploy(data: DroneDeployRequest): Promise<ApiResponse<Drone>> {
    return request.post<Drone>('/drone/deploy', data)
  },

  /**
   * 预览路径规划
   */
  previewPathPlan(data: DroneDeployRequest): Promise<ApiResponse<PathPlanResult>> {
    return request.post<PathPlanResult>('/drone/preview', data)
  },

  /**
   * 开始巡检
   */
  startInspection(droneId: number): Promise<ApiResponse<void>> {
    return request.post<void>(`/drone/${droneId}/start`)
  },

  /**
   * 结束巡检
   */
  stopInspection(droneId: number): Promise<ApiResponse<void>> {
    return request.post<void>(`/drone/${droneId}/stop`)
  },

  /**
   * 更新无人机
   */
  update(id: number, data: DroneUpdateParams): Promise<ApiResponse<Drone>> {
    return request.put<Drone>(`/drone/${id}`, data)
  },

  /**
   * 获取所有检测任务列表（直接查询drone_task表）
   */
  getTaskList(): Promise<ApiResponse<DroneTask[]>> {
    return request.get<DroneTask[]>('/drone/tasks')
  }
}
