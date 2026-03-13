/**
 * 无人机相关类型
 */

export interface Drone {
  id: number
  droneId: string
  registerTime?: string
  maxHour?: number
  latitude?: number
  longitude?: number
  radius?: number
  status: number
  statusText?: string
  createTime?: string
  updateTime?: string
}

export interface DroneCreateParams {
  droneId: string
  registerTime?: string
  maxHour?: number
  latitude?: number
  longitude?: number
  radius?: number
  status: number
}

export interface DroneUpdateParams {
  registerTime?: string
  maxHour?: number
  latitude?: number
  longitude?: number
  radius?: number
  status?: number
}
