package com.vanish.common.util;

import com.vanish.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：token 签发与解析（token 自包含 userId，无状态认证）
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours:72}")
    private long expireHours;

    /**
     * 为用户签发 token
     *
     * @param userId 用户 ID
     * @return JWT token
     */
    public String generateToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expire)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 token，返回用户 ID
     *
     * @param token JWT token
     * @return 用户 ID
     * @throws BusinessException token 无效或已过期
     */
    public String parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("JwtUtil.parseToken | fail | error:{}", e.getMessage());
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
    }
}
