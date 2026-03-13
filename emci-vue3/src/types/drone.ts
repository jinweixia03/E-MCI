/**
 * 无人机相关类型（极简版）
 * 一个无人机对应一个固定检测任务
 */

// 无人机信息（含任务）
export interface Drone {
  id: number
  name: string
  status: number
  statusText?: string
  battery?: number
  latitude?: number
  longitude?: number
  radius?: number
  // 任务信息（一对一）
  centerLat?: number
  centerLng?: number
  taskRadius?: number
  pathPoints?: PathPoint[]
  manholeCount?: number
  estimatedTime?: number
  createTime?: string
  updateTime?: string
}

// 路径规划点
export interface PathPoint {
  manholeId: number
  manholeNo: string
  lat: number
  lng: number
  sequence: number
  distanceFromStart: number
}

// 部署请求
export interface DroneDeployRequest {
  droneId: number
  centerLat: number
  centerLng: number
  radius: number
  pathPoints?: PathPoint[]  // 预览时计算的路径点
}

// 路径规划结果
export interface PathPlanResult {
  centerLat: number
  centerLng: number
  radius: number
  manholeCount: number
  pathPoints: PathPoint[]
  estimatedDistance: number
  estimatedTime: number
  algorithmType: string
}

// 无人机任务（对应drone_task表）
export interface DroneTask {
  id: number
  droneId: number
  droneName?: string
  centerLat?: number
  centerLng?: number
  radius?: number
  pathPoints?: PathPoint[]
  manholeCount?: number
  estimatedTime?: number
  createTime?: string
  updateTime?: string
}

// 兼容旧版本
export interface DroneUpdateParams {
  latitude?: number
  longitude?: number
  radius?: number
  status?: number
}
