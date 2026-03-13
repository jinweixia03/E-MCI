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

            LoginVO loginVO = buildLoginVO(token, userDetails);
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
        // 检查用户名、邮箱、手机号是否已存在
        checkUserExists(request);

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswd(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setType(0);

        userMapper.insert(user);

        log.info("User registered: {}", user.getUsername());
        return Result.success("注册成功", buildUserInfoVO(user));
    }

    @Override
    public Result<UserInfoVO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new BusinessException(UNAUTHORIZED);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = getUserById(userDetails.getId());
        return Result.success(buildUserInfoVO(user));
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

        User user = getUserById(userId);
        return Result.success("刷新成功", buildLoginVO(newToken, user));
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
        validateUserContact(request.getUsername(), request.getPhone(), request.getEmail());
        return Result.success("验证通过", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resetPassword(ResetPasswordRequest request) {
        User user = validateUserContact(request.getUsername(), request.getPhone(), request.getEmail());
        user.setPasswd(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("User reset password: {}", user.getUsername());
        return Result.success("密码重置成功", null);
    }

    // ==================== 私有辅助方法 ====================

    private void checkUserExists(RegisterRequest request) {
        if (userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()))) {
            throw new BusinessException(USER_ALREADY_EXISTS, "用户名已被注册");
        }
        if (userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()))) {
            throw new BusinessException(USER_ALREADY_EXISTS, "邮箱已被注册");
        }
        if (userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone()))) {
            throw new BusinessException(USER_ALREADY_EXISTS, "手机号已被注册");
        }
    }

    private User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }
        return user;
    }

    private User validateUserContact(String username, String phone, String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException(USER_NOT_FOUND, "账号不存在");
        }
        if (!phone.equals(user.getPhone()) || !email.equals(user.getEmail())) {
            throw new BusinessException(USER_PASSWORD_ERROR, "信息验证失败");
        }
        return user;
    }

    private UserInfoVO buildUserInfoVO(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .headImg(user.getHeadImg())
                .type(user.getType())
                .createTime(user.getCreateTime())
                .build();
    }

    private LoginVO buildLoginVO(String token, UserDetailsImpl userDetails) {
        return LoginVO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationDateFromToken(token).getTime() - new Date().getTime())
                .userInfo(UserInfoVO.builder()
                        .id(userDetails.getId())
                        .username(userDetails.getUsername())
                        .email(userDetails.getEmail())
                        .type(userDetails.getUserType())
                        .build())
                .build();
    }

    private LoginVO buildLoginVO(String token, User user) {
        return LoginVO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationDateFromToken(token).getTime() - new Date().getTime())
                .userInfo(buildUserInfoVO(user))
                .build();
    }
}
