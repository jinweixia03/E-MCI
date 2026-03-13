/**
 * 数据大屏类型定义
 */

export interface DashboardStats {
  manholeStats: ManholeStats
  detectionStats: DetectionStats
  repairStats: RepairStats
  droneStats: DroneStats
  defectTypeDistribution: DefectTypeStat[]
  manholeTypeDistribution: TypeStat[]
  statusDistribution: StatusStat[]
  detectionTrend: TrendData[]
  cityDistribution: CityStat[]
  recentDetections: RecentDetection[]
  safetyRanking: SafetyRank[]
}

export interface ManholeStats {
  totalCount: number
  normalCount: number
  damagedCount: number
  repairingCount: number
  normalRate: number
  defectCount: number
  defectRate: number
}

export interface DetectionStats {
  todayCount: number
  weekCount: number
  monthCount: number
  totalCount: number
  todayDefectCount: number
  weekDefectCount: number
  monthDefectCount: number
}

export interface RepairStats {
  pendingCount: number
  inProgressCount: number
  completedCount: number
  totalCount: number
  completionRate: number
}

export interface DroneStats {
  totalCount: number
  onlineCount: number
  offlineCount: number
  chargingCount: number
  faultCount: number
  onlineRate: number
  totalFlightHours: number
  totalInspectionCount: number
}

export interface DefectTypeStat {
  type: string
  name: string
  count: number
  percentage: number
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

export interface TrendData {
  date: string
  detectionCount: number
  defectCount: number
}

export interface CityStat {
  city: string
  count: number
  percentage: number
}

export interface RecentDetection {
  manholeId: string
  address: string
  detectionTime: string
  defectCount: number
  defectTypes: string
  confidence: number
  status: number
}

export interface SafetyRank {
  manholeId: string
  address: string
  score: number
  level: number
  levelName: string
}
