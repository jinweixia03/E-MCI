package com.alian.emci.vo.drone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检记录VO
 */
@Data
@Schema(description = "巡检记录")
public class InspectionLogVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "无人机ID")
    private Long droneId;

    @Schema(description = "无人机名称")
    private String droneName;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "应检井盖数")
    private Integer manholeCount;

    @Schema(description = "实检井盖数")
    private Integer inspectedCount;

    @Schema(description = "发现缺陷数")
    private Integer defectFound;

    @Schema(description = "备注")
    private String remark;
}
