package com.example.taskmanagement.repository;

import com.example.taskmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔑 Основной метод для Spring Security
    Optional<User> findByUsername(String username);

    // (опционально, но полезно для регистрации)
    boolean existsByUsername(String username);
}