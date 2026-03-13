package com.alian.emci.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传响应")
public class FileUploadVO {

    @Schema(description = "文件URL")
    private String url;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件大小(字节)")
    private Long size;

    @Schema(description = "文件类型")
    private String contentType;
}
