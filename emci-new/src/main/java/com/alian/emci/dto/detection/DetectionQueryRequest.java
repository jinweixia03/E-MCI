package com.alian.emci.dto.detection;

import com.alian.emci.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检测记录查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "检测记录查询请求")
public class DetectionQueryRequest extends PageRequest {

    @Schema(description = "关联井盖编号")
    private String manholeId;

    @Schema(description = "检测编号")
    private String detectionNo;

    @Schema(description = "是否有缺陷：0-无，1-有")
    private Integer hasDefect;

    @Schema(description = "是否已修复：0-否，1-是")
    private Integer isRepaired;

    @Schema(description = "检测状态：0-失败，1-成功")
    private Integer detectionStatus;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
