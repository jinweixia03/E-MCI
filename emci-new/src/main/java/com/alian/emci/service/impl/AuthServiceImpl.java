package com.alian.emci.service.impl;

import com.alian.emci.common.Result;
import com.alian.emci.dto.user.LoginRequest;
import com.alian.emci.dto.user.RegisterRequest;
import com.alian.emci.dto.user.ResetPasswordRequest;
import com.alian.emci.dto.user.VerifyAccountRequest;
import com.alian.emci.entity.User;
import com.alian.emci.exception.BusinessException;
import com.alian.emci.mapper.UserMapper;
import com.alian.emci.security.JwtUtils;
import com.alian.emci.security.UserDetailsImpl;
import com.alian.emci.service.AuthService;
import com.alian.emci.vo.user.LoginVO;
import com.alian.emci.vo.user.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.alian.emci.common.ResultCode.*;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public Result<LoginVO> login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 生成JWT Token
            String token = jwtUtils.generateToken(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getUserType()
            );

            // 构建用户信息
            UserInfoVO userInfo = UserInfoVO.builder()
                    .id(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .type(userDetails.getUserType())
                    .build();

            // 构建登录响应
            LoginVO loginVO = LoginVO.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtils.getExpirationDateFromToken(token).getTime() - new Date().getTime())
                    .userInfo(userInfo)
                    .build();

            log.info("User logged in: {}", userDetails.getUsername());
            return Result.success("登录成功", loginVO);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new BusinessException(USER_PASSWORD_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserInfoVO> register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()))) {
            throw new BusinessException(USER_ALREADY_EXISTS, "用户名已被注册");
        }

        // 检查邮箱是否已存在
        if (userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()))) {
            throw new BusinessException(USER_ALREADY_EXISTS, "邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone()))) {
            throw new BusinessException(USER_ALREADY_EXISTS, "手机号已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswd(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setType(0); // 默认为普通用户

        userMapper.insert(user);

        // 构建响应
        UserInfoVO userInfo = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .type(user.getType())
                .createTime(user.getCreateTime())
                .build();

        log.info("User registered: {}", user.getUsername());
        return Result.success("注册成功", userInfo);
    }

    @Override
    public Result<UserInfoVO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new BusinessException(UNAUTHORIZED);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 查询完整用户信息
        User user = userMapper.selectById(userDetails.getId());
        if (user == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        UserInfoVO userInfo = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .headImg(user.getHeadImg())
                .type(user.getType())
                .createTime(user.getCreateTime())
                .build();

        return Result.success(userInfo);
    }

    @Override
    public Result<LoginVO> refreshToken(String token) {
        if (!token.startsWith("Bearer ")) {
            throw new BusinessException(UNAUTHORIZED, "Token格式错误");
        }

        String jwt = token.substring(7);
        if (jwtUtils.isTokenExpired(jwt)) {
            throw new BusinessException(UNAUTHORIZED, "Token已过期");
        }

        // 获取用户信息
        Long userId = jwtUtils.getUserIdFromToken(jwt);
        String username = jwtUtils.getUsernameFromToken(jwt);
        Integer userType = jwtUtils.getUserTypeFromToken(jwt);

        // 生成新Token
        String newToken = jwtUtils.generateToken(userId, username, userType);

        // 查询用户信息
        User user = userMapper.selectById(userId);
        UserInfoVO userInfo = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .type(user.getType())
                .build();

        LoginVO loginVO = LoginVO.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationDateFromToken(newToken).getTime() - new Date().getTime())
                .userInfo(userInfo)
                .build();

        return Result.success("刷新成功", loginVO);
    }

    @Override
    public Result<Boolean> checkEmail(String email) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        return Result.success(exists);
    }

    @Override
    public Result<Boolean> checkPhone(String phone) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        return Result.success(exists);
    }

    @Override
    public Result<Boolean> verifyAccount(VerifyAccountRequest request) {
        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException(USER_NOT_FOUND, "账号不存在");
        }

        // 验证手机号和邮箱是否匹配
        if (!request.getPhone().equals(user.getPhone())) {
            throw new BusinessException(USER_PASSWORD_ERROR, "手机号不匹配");
        }

        if (!request.getEmail().equals(user.getEmail())) {
            throw new BusinessException(USER_PASSWORD_ERROR, "邮箱不匹配");
        }

        return Result.success("验证通过", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resetPassword(ResetPasswordRequest request) {
        // 先验证账号信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException(USER_NOT_FOUND, "账号不存在");
        }

        // 验证手机号和邮箱是否匹配
        if (!request.getPhone().equals(user.getPhone())) {
            throw new BusinessException(USER_PASSWORD_ERROR, "信息验证失败");
        }

        if (!request.getEmail().equals(user.getEmail())) {
            throw new BusinessException(USER_PASSWORD_ERROR, "信息验证失败");
        }

        // 更新密码
        user.setPasswd(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("User reset password: {}", user.getUsername());
        return Result.success("密码重置成功", null);
    }
}
