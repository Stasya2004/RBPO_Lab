package com.example.taskmanagement.repository;

import com.example.taskmanagement.model.SessionStatus;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findAllByUserAndStatus(User user, SessionStatus status);

    // Можно добавить метод для удаления старых сессий
    void deleteByExpiresAtBefore(LocalDateTime now);
}