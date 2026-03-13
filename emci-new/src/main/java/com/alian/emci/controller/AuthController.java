package com.alian.emci.controller;

import com.alian.emci.common.Result;
import com.alian.emci.dto.user.LoginRequest;
import com.alian.emci.dto.user.RegisterRequest;
import com.alian.emci.dto.user.ResetPasswordRequest;
import com.alian.emci.dto.user.VerifyAccountRequest;
import com.alian.emci.service.AuthService;
import com.alian.emci.vo.user.LoginVO;
import com.alian.emci.vo.user.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "登录、注册等认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "使用用户名和密码登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "用户注册", description = "注册新用户")
    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserInfoVO> getCurrentUser() {
        return authService.getCurrentUser();
    }

    @Operation(summary = "刷新Token", description = "使用旧Token获取新Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestHeader("Authorization") String token) {
        return authService.refreshToken(token);
    }

    @Operation(summary = "检查邮箱是否已存在", description = "验证邮箱是否已被注册")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(
            @RequestParam
            @Email(message = "邮箱格式不正确")
            String email) {
        return authService.checkEmail(email);
    }

    @Operation(summary = "检查手机号是否已存在", description = "验证手机号是否已被注册")
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(
            @RequestParam
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            String phone) {
        return authService.checkPhone(phone);
    }

    @Operation(summary = "验证账号信息", description = "找回密码时验证账号、手机号和邮箱是否匹配")
    @PostMapping("/verify-account")
    public Result<Boolean> verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
        return authService.verifyAccount(request);
    }

    @Operation(summary = "重置密码", description = "验证通过后重置用户密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}
