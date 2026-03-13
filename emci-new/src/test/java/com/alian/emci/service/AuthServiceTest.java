package com.alian.emci.service;

import com.alian.emci.common.Result;
import com.alian.emci.dto.user.LoginRequest;
import com.alian.emci.dto.user.RegisterRequest;
import com.alian.emci.dto.user.ResetPasswordRequest;
import com.alian.emci.dto.user.VerifyAccountRequest;
import com.alian.emci.entity.User;
import com.alian.emci.exception.BusinessException;
import com.alian.emci.mapper.UserMapper;
import com.alian.emci.security.JwtUtils;
import com.alian.emci.service.impl.AuthServiceImpl;
import com.alian.emci.vo.user.LoginVO;
import com.alian.emci.vo.user.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhone("13800138000");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setType(0);
    }

    @Test
    @DisplayName("登录成功")
    void loginSuccess() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(
                com.alian.emci.security.UserDetailsImpl.build(testUser));
        when(jwtUtils.generateToken(any(), anyString(), any()))
                .thenReturn("test.jwt.token");
        when(jwtUtils.getExpirationDateFromToken(anyString()))
                .thenReturn(new Date(System.currentTimeMillis() + 3600000));

        // When
        Result<LoginVO> result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("test.jwt.token", result.getData().getAccessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void loginFailedBadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("注册成功")
    void registerSuccess() {
        // Given
        when(userMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        Result<UserInfoVO> result = authService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("newuser", result.getData().getUsername());
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void registerFailedUsernameExists() {
        // Given
        when(userMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("检查邮箱 - 邮箱不存在")
    void checkEmailNotExists() {
        // Given
        when(userMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

        // When
        Result<Boolean> result = authService.checkEmail("new@example.com");

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertFalse(result.getData());
    }

    @Test
    @DisplayName("检查邮箱 - 邮箱已存在")
    void checkEmailExists() {
        // Given
        when(userMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

        // When
        Result<Boolean> result = authService.checkEmail("exists@example.com");

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("验证账号信息 - 成功")
    void verifyAccountSuccess() {
        // Given
        VerifyAccountRequest request = new VerifyAccountRequest();
        request.setUsername("testuser");
        request.setPhone("13800138000");
        request.setEmail("test@example.com");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // When
        Result<Boolean> result = authService.verifyAccount(request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("验证账号信息 - 用户不存在")
    void verifyAccountUserNotFound() {
        // Given
        VerifyAccountRequest request = new VerifyAccountRequest();
        request.setUsername("notexist");
        request.setPhone("13800138000");
        request.setEmail("test@example.com");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.verifyAccount(request));
    }

    @Test
    @DisplayName("重置密码 - 成功")
    void resetPasswordSuccess() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUsername("testuser");
        request.setPhone("13800138000");
        request.setEmail("test@example.com");
        request.setNewPassword("newPassword123");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        Result<Void> result = authService.resetPassword(request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("刷新Token - 成功")
    void refreshTokenSuccess() {
        // Given
        String oldToken = "Bearer old.jwt.token";
        when(jwtUtils.isTokenExpired(anyString())).thenReturn(false);
        when(jwtUtils.getUserIdFromToken(anyString())).thenReturn(1L);
        when(jwtUtils.getUsernameFromToken(anyString())).thenReturn("testuser");
        when(jwtUtils.getUserTypeFromToken(anyString())).thenReturn(0);
        when(jwtUtils.generateToken(any(), anyString(), any())).thenReturn("new.jwt.token");
        when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(userMapper.selectById(any())).thenReturn(testUser);

        // When
        Result<LoginVO> result = authService.refreshToken(oldToken);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("new.jwt.token", result.getData().getAccessToken());
    }

    @Test
    @DisplayName("刷新Token - Token格式错误")
    void refreshTokenInvalidFormat() {
        // Given
        String invalidToken = "InvalidToken";

        // When & Then
        assertThrows(BusinessException.class, () -> authService.refreshToken(invalidToken));
    }
}
