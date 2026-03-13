package com.alian.emci.service;

import com.alian.emci.common.Result;
import com.alian.emci.dto.user.LoginRequest;
import com.alian.emci.dto.user.RegisterRequest;
import com.alian.emci.dto.user.ResetPasswordRequest;
import com.alian.emci.dto.user.VerifyAccountRequest;
import com.alian.emci.vo.user.LoginVO;
import com.alian.emci.vo.user.UserInfoVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    Result<LoginVO> login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户信息
     */
    Result<UserInfoVO> register(RegisterRequest request);

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    Result<UserInfoVO> getCurrentUser();

    /**
     * 刷新Token
     *
     * @param token 旧Token
     * @return 新Token
     */
    Result<LoginVO> refreshToken(String token);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    Result<Boolean> checkEmail(String email);

    /**
     * 检查手机号是否已存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    Result<Boolean> checkPhone(String phone);

    /**
     * 验证账号信息（找回密码用）
     *
     * @param request 验证请求
     * @return 是否验证通过
     */
    Result<Boolean> verifyAccount(VerifyAccountRequest request);

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     * @return 结果
     */
    Result<Void> resetPassword(ResetPasswordRequest request);
}
