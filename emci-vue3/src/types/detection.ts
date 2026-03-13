/**
 * 检测记录相关类型
 */

// 检测记录
export interface Detection {
  id: number
  manholeId: string
  detectionNo: string
  detectionTime?: string
  detectionStatus: number

  // 图片
  originalImgUrl?: string
  resultImgUrl?: string

  // 检测结果
  hasDefect: number
  defectCount: number
  defectTypes?: string

  // 检测参数
  modelVersion?: string
  confThreshold?: number
  iouThreshold?: number
  processTimeMs?: number

  // 主要缺陷
  primaryDefectType?: string
  primaryConfidence?: number

  // 修复状态
  isRepaired: number
  repairedTime?: string
  repairedBy?: number
  repairRemark?: string

  // 扩展
  resultJson?: string
  createTime?: string
  updateTime?: string
}

// 缺陷详情
export interface DetectionDefectDetail {
  id: number
  detectionId: number
  defectType: string
  confidence: number
  bboxX?: number
  bboxY?: number
  bboxWidth?: number
  bboxHeight?: number
  pixelX?: number
  pixelY?: number
  pixelWidth?: number
  pixelHeight?: number
  createTime?: string
}

// 创建检测记录参数
export interface DetectionCreateParams {
  manholeId: string
  detectionNo?: string
  detectionTime?: string
  originalImgUrl?: string
  resultImgUrl?: string
  hasDefect?: number
  defectCount?: number
  defectTypes?: string
  modelVersion?: string
  confThreshold?: number
  iouThreshold?: number
  processTimeMs?: number
  primaryDefectType?: string
  primaryConfidence?: number
  resultJson?: string
}

// 查询参数
export interface DetectionQueryParams {
  pageNum?: number
  pageSize?: number
  manholeId?: string
  detectionNo?: string
  hasDefect?: number
  isRepaired?: number
  detectionStatus?: number
  keyword?: string
  startTime?: string
  endTime?: string
}

export interface DetectionResult {
  originalUrl: string
  original_url?: string
  resultUrl: string
  result_url?: string
  txtUrl: string
  txt_url?: string
  defectCount: number
  defect_count?: number
  defects: DefectInfo[]
  processTime: number
  process_time_ms?: number
}

export interface DefectInfo {
  type: string
  confidence: number
  bbox: number[]
}
