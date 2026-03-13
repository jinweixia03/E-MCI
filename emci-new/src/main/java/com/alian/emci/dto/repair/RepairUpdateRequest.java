package com.alian.emci.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 维修记录更新请求
 */
@Data
@Schema(description = "维修记录更新请求")
public class RepairUpdateRequest {

    @Schema(description = "维修人员ID")
    private Long repairUserId;

    @Schema(description = "状态：0-待维修, 1-维修中, 2-已完成")
    private Integer status;

    @Schema(description = "缺陷类型（逗号分隔）")
    private String defectTypes;

    @Schema(description = "维修内容")
    private String repairContent;

    @Schema(description = "维修前图片")
    private String beforeImg;

    @Schema(description = "维修后图片")
    private String afterImg;

    @Schema(description = "备注")
    private String remark;
}
