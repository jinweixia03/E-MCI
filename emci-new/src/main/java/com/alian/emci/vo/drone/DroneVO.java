package com.alian.emci.vo.drone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 无人机信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "无人机信息")
public class DroneVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "无人机编号")
    private String droneId;

    @Schema(description = "注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registerTime;

    @Schema(description = "最大续航时间(小时)")
    private Double maxHour;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "覆盖半径(米)")
    private Integer radius;

    @Schema(description = "状态：0-停用，1-运行中")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
