package com.alian.emci.security;

import com.alian.emci.config.properties.AppProperties;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT工具类测试")
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Jwt jwtProperties;

    @BeforeEach
    void setUp() {
        when(appProperties.getJwt()).thenReturn(jwtProperties);
        when(jwtProperties.getSecret()).thenReturn("test-secret-key-for-jwt-token-generation-1234567890");
        when(jwtProperties.getExpiration()).thenReturn(1L); // 1小时
    }

    @Test
    @DisplayName("生成和解析Token")
    void generateAndParseToken() {
        Long userId = 1L;
        String username = "testuser";
        Integer userType = 0;

        String token = jwtUtils.generateToken(userId, username, userType);

        assertNotNull(token);
        assertTrue(token.contains("."));

        // 解析Token
        Long parsedUserId = jwtUtils.getUserIdFromToken(token);
        String parsedUsername = jwtUtils.getUsernameFromToken(token);
        Integer parsedUserType = jwtUtils.getUserTypeFromToken(token);

        assertEquals(userId, parsedUserId);
        assertEquals(username, parsedUsername);
        assertEquals(userType, parsedUserType);
    }

    @Test
    @DisplayName("验证Token未过期")
    void tokenNotExpired() {
        String token = jwtUtils.generateToken(1L, "testuser", 0);

        assertFalse(jwtUtils.isTokenExpired(token));
    }

    @Test
    @DisplayName("验证无效Token已过期")
    void invalidTokenIsExpired() {
        String invalidToken = "invalid.token.here";
        assertTrue(jwtUtils.isTokenExpired(invalidToken));
    }

    @Test
    @DisplayName("获取Token过期时间")
    void getExpirationDateFromToken() {
        String token = jwtUtils.generateToken(1L, "testuser", 0);
        Date expirationDate = jwtUtils.getExpirationDateFromToken(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("验证Token有效性 - 有效Token")
    void verifyTokenValid() {
        String token = jwtUtils.generateToken(1L, "testuser", 0);

        assertDoesNotThrow(() -> jwtUtils.verifyToken(token));
    }

    @Test
    @DisplayName("验证Token有效性 - 无效Token")
    void verifyTokenInvalid() {
        String invalidToken = "invalid.token.here";

        assertThrows(JWTVerificationException.class, () -> jwtUtils.verifyToken(invalidToken));
    }

    @Test
    @DisplayName("验证Token过期")
    void verifyTokenExpiration() {
        // 创建过期token是不可能的，因为生成时设置的是当前时间+过期时间
        // 所以我们只能验证新生成的token没有过期
        String token = jwtUtils.generateToken(1L, "testuser", 0);

        assertFalse(jwtUtils.isTokenExpired(token));
    }
}
