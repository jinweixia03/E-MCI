import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Detection, DetectionCreateParams, DetectionQueryParams, DetectionResult } from '@/types/detection'

export const detectionApi = {
  /**
   * 创建检测记录
   */
  create(data: DetectionCreateParams): Promise<ApiResponse<Detection>> {
    return request.post<Detection>('/detection', data)
  },

  /**
   * 删除检测记录
   */
  delete(id: number): Promise<ApiResponse<void>> {
    return request.delete<void>(`/detection/${id}`)
  },

  /**
   * 获取检测记录详情
   */
  getById(id: number): Promise<ApiResponse<Detection>> {
    return request.get<Detection>(`/detection/${id}`)
  },

  /**
   * 分页查询检测记录
   */
  pageQuery(params: DetectionQueryParams): Promise<ApiResponse<PageResult<Detection>>> {
    return request.get<PageResult<Detection>>('/detection/page', { params })
  },

  /**
   * 根据井盖编号查询
   */
  getByManholeId(manholeId: string): Promise<ApiResponse<Detection[]>> {
    return request.get<Detection[]>(`/detection/by-manhole/${manholeId}`)
  },

  /**
   * 获取待维修列表
   */
  getNeedRepaired(): Promise<ApiResponse<Detection[]>> {
    return request.get<Detection[]>('/detection/need-repair')
  },

  /**
   * 标记为已修复
   */
  markRepaired(id: number): Promise<ApiResponse<Detection>> {
    return request.patch<Detection>(`/detection/${id}/repair`)
  },

  /**
   * AI图片检测
   */
  detectImage(file: File, confThreshold = 0.7, iouThreshold = 0.5, manholeId?: string): Promise<ApiResponse<DetectionResult>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('confThreshold', String(confThreshold))
    formData.append('iouThreshold', String(iouThreshold))
    if (manholeId) {
      formData.append('manholeId', manholeId)
    }

    return request.post<DetectionResult>('/ai/detect/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * AI视频检测
   */
  detectVideo(file: File, confThreshold = 0.7, iouThreshold = 0.5): Promise<ApiResponse<DetectionResult>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('confThreshold', String(confThreshold))
    formData.append('iouThreshold', String(iouThreshold))

    return request.post<DetectionResult>('/ai/detect/video', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * AI图片检测（带井盖信息）
   */
  detectImageWithManhole(formData: FormData): Promise<ApiResponse<DetectionResult>> {
    return request.post<DetectionResult>('/ai/detect/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * AI视频检测（带井盖信息）
   */
  detectVideoWithManhole(formData: FormData): Promise<ApiResponse<DetectionResult>> {
    return request.post<DetectionResult>('/ai/detect/video', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * 保存AI检测结果到数据库
   */
  saveDetectionResult(manholeId: string, result: DetectionResult): Promise<ApiResponse<DetectionResult>> {
    return request.post<DetectionResult>(`/ai/save?manholeId=${encodeURIComponent(manholeId)}`, result)
  }
}
