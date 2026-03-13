package com.alian.emci.security;

import com.alian.emci.config.properties.AppProperties;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final AppProperties appProperties;

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username, Integer userType) {
        Algorithm algorithm = Algorithm.HMAC256(appProperties.getJwt().getSecret());

        Instant now = Instant.now();
        Instant expiration = now.plus(appProperties.getJwt().getExpiration(), ChronoUnit.HOURS);

        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("username", username)
                .withClaim("userType", userType)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiration))
                .sign(algorithm);
    }

    /**
     * 验证Token
     */
    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(appProperties.getJwt().getSecret());
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    /**
     * 从Token获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return Long.valueOf(decodedJWT.getSubject());
    }

    /**
     * 从Token获取用户名
     */
    public String getUsernameFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("username").asString();
    }

    /**
     * 从Token获取用户类型
     */
    public Integer getUserTypeFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userType").asInt();
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }

    /**
     * 获取Token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getExpiresAt();
    }
}
