package com.example.taskmanagement.service;

import com.example.taskmanagement.model.SessionStatus;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.model.UserSession;
import com.example.taskmanagement.repository.UserSessionRepository;
import com.example.taskmanagement.security.JwtTokenProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenService {

    private final UserSessionRepository sessionRepository;
    private final JwtTokenProvider tokenProvider;

    public TokenService(UserSessionRepository sessionRepository, JwtTokenProvider tokenProvider) {
        this.sessionRepository = sessionRepository;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public TokenPair createTokenPair(UserDetails userDetails, User user) {
        // Создаем запись сессии с refresh токеном
        String refreshToken = tokenProvider.generateRefreshToken(userDetails, null); // sessionId пока null
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(tokenProvider.getRefreshExpirationMs() / 1000);

        UserSession session = new UserSession(user, refreshToken, now, expiresAt);
        session = sessionRepository.save(session); // после сохранения получаем id

        // Генерируем refresh токен заново с sessionId (или можно обновить claim)
        refreshToken = tokenProvider.generateRefreshToken(userDetails, session.getId());
        session.setRefreshToken(refreshToken);
        sessionRepository.save(session);

        // Генерируем access токен
        String accessToken = tokenProvider.generateAccessToken(
                userDetails,
                user.getId(),
                user.getRole().name()
        );

        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional
    public TokenPair  refreshTokens(String refreshToken) {
        // 1. Проверить валидность токена (подпись, срок)
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 2. Найти сессию по токену
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 3. Проверить статус сессии
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Session is not active");
        }

        // 4. Проверить, не истек ли срок сессии в БД (на случай рассинхрона)
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new RuntimeException("Refresh token expired");
        }

        User user = session.getUser();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();

        // 5. Отозвать текущую сессию
        session.setStatus(SessionStatus.REVOKED);
        sessionRepository.save(session);

        // 6. Создать новую сессию и пару токенов
        return createTokenPair(userDetails, user);
    }

    // Вспомогательный класс для возврата пары токенов
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }

    // Дополнительные методы: logout (отозвать сессию), очистка устаревших
    @Transactional
    public void revokeSession(String refreshToken) {
        sessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setStatus(SessionStatus.REVOKED);
                    sessionRepository.save(session);
                });
    }
}