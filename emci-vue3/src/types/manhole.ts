/**
 * 井盖相关类型
 */

export interface Manhole {
  id: number
  manholeId: string
  imgUrl?: string
  latitude: number
  longitude: number
  address?: string
  nextDetTime?: string
  createTime?: string
  updateTime?: string
  status?: number
  statusText?: string
  province?: string
  city?: string
  district?: string
  manholeType?: number
  manholeTypeText?: string
  lastDetTime?: string
  detectionCount?: number
  defectCount?: number
  safetyScore?: number
}

export interface ManholeCreateParams {
  manholeId: string
  imgUrl?: string
  latitude: number
  longitude: number
  nextDetTime?: string
  address?: string
  status?: number
  province?: string
  city?: string
  district?: string
  manholeType?: number
}

export interface ManholeUpdateParams {
  imgUrl?: string
  latitude?: number
  longitude?: number
  nextDetTime?: string
  address?: string
  status?: number
  province?: string
  city?: string
  district?: string
  manholeType?: number
}

export interface ManholeQueryParams {
  pageNum?: number
  pageSize?: number
  manholeId?: string
  keyword?: string
  status?: number
  city?: string
  district?: string
  manholeType?: number
  minLongitude?: number
  maxLongitude?: number
  minLatitude?: number
  maxLatitude?: number
  hasDefect?: number
}

// ==================== 地图相关类型 ====================

export interface ManholeMapVO {
  id: number
  manholeId: string
  imgUrl?: string
  latitude: number
  longitude: number
  address?: string
  nextDetTime?: string
  status: number
  statusText?: string
  manholeType?: number
  manholeTypeText?: string
  detectionCount?: number
  defectCount?: number
  safetyScore?: number
  lastDetTime?: string
  hasLatestDetection?: boolean
  latestDetectionTime?: string
  latestDefectCount?: number
  latestIsRepaired?: number
}

export interface ManholeClusterVO {
  longitude: number
  latitude: number
  count: number
  clusterId: string
  manholes: ManholeMapVO[]
  swLng: number
  swLat: number
  neLng: number
  neLat: number
}

export interface ManholeMapStats {
  totalCount: number
  normalCount: number
  damagedCount: number
  repairingCount: number
  defectCount: number
  typeStats: Record<string, number>
  cityStats: Record<string, number>
  districtStats: Record<string, number>
  typeDistribution: TypeStat[]
  statusDistribution: StatusStat[]
}

export interface TypeStat {
  type: number
  name: string
  count: number
  percentage: number
}

export interface StatusStat {
  status: number
  name: string
  count: number
  percentage: number
}

export interface ManholeMapQueryParams {
  minLongitude?: number
  maxLongitude?: number
  minLatitude?: number
  maxLatitude?: number
  centerLongitude?: number
  centerLatitude?: number
  radius?: number
  manholeId?: string
  keyword?: string
  status?: number
  city?: string
  district?: string
  manholeType?: number
  hasDefect?: number
  onlyDefect?: boolean
  zoom?: number
  enableCluster?: boolean
  gridSize?: number
  maxResults?: number
}
