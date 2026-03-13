package com.alian.emci.vo.detection;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI检测结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI检测结果")
public class DetectionResultVO {

    @JsonAlias({"original_url", "originalUrl"})
    @Schema(description = "原始图片/视频URL")
    private String originalUrl;

    @JsonAlias({"result_url", "resultUrl"})
    @Schema(description = "检测结果图片/视频URL")
    private String resultUrl;

    @JsonAlias({"txt_url", "txtUrl"})
    @Schema(description = "检测结果文本URL")
    private String txtUrl;

    @JsonAlias({"defect_count", "defectCount"})
    @Schema(description = "检测到的缺陷数量")
    private Integer defectCount;

    @Schema(description = "缺陷详情列表")
    private List<DefectInfo> defects;

    @JsonAlias({"process_time_ms", "processTimeMs", "process_time"})
    @Schema(description = "处理时间(ms)")
    private Long processTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "缺陷信息")
    public static class DefectInfo {
        @Schema(description = "缺陷类型")
        private String type;

        @Schema(description = "置信度")
        private Double confidence;

        @Schema(description = "位置坐标 [x, y, width, height]")
        private List<Double> bbox;

        @JsonAlias({"class_id", "classId"})
        @Schema(description = "类别ID")
        private Integer classId;
    }
}
