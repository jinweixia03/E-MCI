package com.alian.emci.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页请求基类
 */
@Data
public class PageRequest {

    @Schema(description = "当前页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "每页最少1条")
    @Max(value = 100, message = "每页最多100条")
    private Integer pageSize = 10;
}
