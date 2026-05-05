package com.example.taskmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.access.expiration}")
    private long accessExpirationMs;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    private Key getAccessSigningKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes());
    }

    private Key getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }

    // ---------- Генерация токенов ----------

    public String generateAccessToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getAccessSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails, Long sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId);
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getRefreshSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ---------- Извлечение данных ----------

    public String extractUsernameFromAccess(String token) {
        return extractClaims(token, getAccessSigningKey()).getSubject();
    }

    public String extractUsernameFromRefresh(String token) {
        return extractClaims(token, getRefreshSigningKey()).getSubject();
    }

    public Long extractSessionId(String refreshToken) {
        return extractClaims(refreshToken, getRefreshSigningKey()).get("sessionId", Long.class);
    }

    private Claims extractClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ---------- Валидация ----------

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsernameFromAccess(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token, getAccessSigningKey());
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsernameFromRefresh(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token, getRefreshSigningKey());
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token, Key key) {
        return extractClaims(token, key).getExpiration().before(new Date());
    }
}