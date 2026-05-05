package com.example.taskmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.access.expiration}")
    private long accessExpirationMs;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    private Key accessKey;
    private Key refreshKey;

    @PostConstruct
    public void init() {
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }

    // ---------- Генерация токенов ----------
    public String generateAccessToken(UserDetails userDetails, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), accessExpirationMs, accessKey);
    }

    public String generateRefreshToken(UserDetails userDetails, Long sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId);
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpirationMs, refreshKey);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs, Key key) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ---------- Извлечение информации ----------
    public String extractUsername(String token, TokenType type) {
        return extractClaim(token, Claims::getSubject, getKeyForType(type));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class), accessKey);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class), accessKey);
    }

    public Long extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get("sessionId", Long.class), refreshKey);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class), accessKey); // или refreshKey в зависимости от ожидаемого типа
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Key key) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ---------- Валидация ----------
    public boolean validateAccessToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token, TokenType.ACCESS);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, accessKey));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            extractAllClaims(token, refreshKey);
            return !isTokenExpired(token, refreshKey);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token, Key key) {
        final Date expiration = extractClaim(token, Claims::getExpiration, key);
        return expiration.before(new Date());
    }

    // Вспомогательный метод для выбора ключа по типу токена
    private Key getKeyForType(TokenType type) {
        return type == TokenType.ACCESS ? accessKey : refreshKey;
    }

    public enum TokenType {
        ACCESS, REFRESH
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}