package com.todo.common.security;

import com.todo.common.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Сервис для работы с JWT токенами
 *
 * Зачем в common:
 * 1. Используется в user-service для создания токенов
 * 2. Используется в api-gateway для проверки токенов
 * 3. Единая логика для всех сервисов
 */
@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String jwtSecret;
    public JwtService() {
        jwtSecret= SecretService.getSecret("JWT_SECRET");
        jwtExpirationMs = 86400000;
        log.info("✅ JwtService initialized with secret from ENV");
    }

    private final long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерация токена для пользователя
     */
    public String generateToken(String email, Long userId, String name, Role role) {
        Claims claims = Jwts.claims()
                .subject(email)
                .add("id", userId)
                .add("name", name)
                .add("role", role.name())
                .build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Проверка валидности токена
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Получение email из токена
     */
    public String getEmailFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Получение userId из токена
     */
    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("id", Long.class);
    }

    /**
     * Получение роли из токена
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
