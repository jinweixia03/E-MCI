package com.alian.emci.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    ACCEPTED(202, "请求已接受"),

    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "参数校验失败"),

    // 服务端错误
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误 (1000-1999)
    USER_NOT_FOUND(1000, "用户不存在"),
    USER_PASSWORD_ERROR(1001, "密码错误"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_DISABLED(1003, "用户已被禁用"),

    MANHOLE_NOT_FOUND(1100, "井盖不存在"),
    DRONE_NOT_FOUND(1200, "无人机不存在"),
    DETECTION_NOT_FOUND(1300, "检测记录不存在"),

    FILE_UPLOAD_ERROR(1400, "文件上传失败"),
    FILE_TOO_LARGE(1401, "文件过大"),
    FILE_TYPE_NOT_SUPPORTED(1402, "不支持的文件类型"),

    COS_OPERATION_ERROR(1500, "云存储操作失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
