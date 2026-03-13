package com.alian.emci.vo.map;

import com.alian.emci.vo.BaseManholeVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 地图用井盖信息VO（精简版，适合地图展示）
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "地图用井盖信息")
public class ManholeMapVO extends BaseManholeVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否存在最新检测")
    private Boolean hasLatestDetection;

    @Schema(description = "最新检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestDetectionTime;

    @Schema(description = "最新检测缺陷数")
    private Integer latestDefectCount;

    @Schema(description = "最新检测是否已修复")
    private Integer latestIsRepaired;
}
