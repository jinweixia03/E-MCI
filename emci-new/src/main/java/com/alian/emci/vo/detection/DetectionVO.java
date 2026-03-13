package com.alian.emci.vo.detection;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 检测记录VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "检测记录信息")
public class DetectionVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联井盖编号")
    private String manholeId;

    @Schema(description = "检测编号")
    private String detectionNo;

    @Schema(description = "检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime detectionTime;

    @Schema(description = "检测状态：0-失败，1-成功")
    private Integer detectionStatus;

    @Schema(description = "原始图片URL")
    private String originalImgUrl;

    @Schema(description = "检测结果图片URL")
    private String resultImgUrl;

    @Schema(description = "是否有缺陷：0-无，1-有")
    private Integer hasDefect;

    @Schema(description = "缺陷总数")
    private Integer defectCount;

    @Schema(description = "缺陷类型列表")
    private String defectTypes;

    @Schema(description = "使用的模型版本")
    private String modelVersion;

    @Schema(description = "置信度阈值")
    private Double confThreshold;

    @Schema(description = "IoU阈值")
    private Double iouThreshold;

    @Schema(description = "处理耗时（毫秒）")
    private Integer processTimeMs;

    @Schema(description = "主要缺陷类型")
    private String primaryDefectType;

    @Schema(description = "最高置信度")
    private Double primaryConfidence;

    @Schema(description = "是否已修复：0-未修复，1-已修复")
    private Integer isRepaired;

    @Schema(description = "修复状态文本")
    private String repairStatusText;

    @Schema(description = "修复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime repairedTime;

    @Schema(description = "修复人ID")
    private Long repairedBy;

    @Schema(description = "修复备注")
    private String repairRemark;

    @Schema(description = "完整检测结果JSON")
    private String resultJson;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
