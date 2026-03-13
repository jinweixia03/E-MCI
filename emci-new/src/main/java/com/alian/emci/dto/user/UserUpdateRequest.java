package com.alian.emci.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户信息更新请求DTO
 */
@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateRequest {

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "头像URL")
    private String headImg;

    @Schema(description = "旧密码（修改密码时需要）")
    private String oldPassword;

    @Schema(description = "新密码（修改密码时需要）")
    private String newPassword;
}
