package com.alian.emci.controller;

import com.alian.emci.common.Result;
import com.alian.emci.dto.user.LoginRequest;
import com.alian.emci.dto.user.RegisterRequest;
import com.alian.emci.service.AuthService;
import com.alian.emci.vo.user.LoginVO;
import com.alian.emci.vo.user.UserInfoVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private LoginVO loginVO;

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

        UserInfoVO userInfo = UserInfoVO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .type(0)
                .build();

        loginVO = LoginVO.builder()
                .accessToken("test.jwt.token")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userInfo(userInfo)
                .build();
    }

    @Test
    @DisplayName("登录接口 - 成功")
    void loginSuccess() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Result.success("登录成功", loginVO));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("test.jwt.token"));
    }

    @Test
    @DisplayName("登录接口 - 参数校验失败")
    void loginValidationFailed() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername(""); // 空用户名
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("注册接口 - 成功")
    void registerSuccess() throws Exception {
        UserInfoVO userInfo = UserInfoVO.builder()
                .id(2L)
                .username("newuser")
                .email("test@example.com")
                .type(0)
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(Result.success("注册成功", userInfo));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @DisplayName("检查邮箱接口")
    void checkEmail() throws Exception {
        when(authService.checkEmail("test@example.com"))
                .thenReturn(Result.success(false));

        mockMvc.perform(get("/auth/check-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("检查手机号接口")
    void checkPhone() throws Exception {
        when(authService.checkPhone("13800138000"))
                .thenReturn(Result.success(true));

        mockMvc.perform(get("/auth/check-phone")
                        .param("phone", "13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("检查邮箱接口 - 无效邮箱格式")
    void checkEmailInvalid() throws Exception {
        mockMvc.perform(get("/auth/check-email")
                        .param("email", "invalid-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("检查手机号接口 - 无效手机号格式")
    void checkPhoneInvalid() throws Exception {
        mockMvc.perform(get("/auth/check-phone")
                        .param("phone", "123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("获取当前用户信息 - 已登录")
    void getCurrentUserWithAuth() throws Exception {
        UserInfoVO userInfo = UserInfoVO.builder()
                .id(1L)
                .username("testuser")
                .type(0)
                .build();

        when(authService.getCurrentUser())
                .thenReturn(Result.success(userInfo));

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
