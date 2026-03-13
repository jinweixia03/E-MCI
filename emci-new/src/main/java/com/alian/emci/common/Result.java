package com.alian.emci.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一API响应结构
 *
 * @param <T> 数据类型
 */
@Data
@Schema(description = "统一响应结构")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "时间戳")
    private LocalDateTime timestamp;

    @Schema(description = "请求路径")
    private String path;

    public Result() {
        this.timestamp = LocalDateTime.now();
    }

    public Result(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 成功响应 ==========

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ========== 失败响应 ==========

    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // ========== 链式操作 ==========

    public Result<T> path(String path) {
        this.path = path;
        return this;
    }

    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
