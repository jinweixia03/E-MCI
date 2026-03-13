package com.alian.emci.dto.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 维修记录创建请求
 */
@Data
@Schema(description = "维修记录创建请求")
public class RepairCreateRequest {

    @Schema(description = "关联检测记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "检测记录ID不能为空")
    private Long detectionId;

    @Schema(description = "关联井盖编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "井盖编号不能为空")
    private String manholeId;

    @Schema(description = "维修人员ID")
    private Long repairUserId;

    @Schema(description = "缺陷类型（逗号分隔）")
    private String defectTypes;

    @Schema(description = "维修内容")
    private String repairContent;

    @Schema(description = "维修前图片（默认使用检测结果图片）")
    private String beforeImg;

    @Schema(description = "备注")
    private String remark;
}
