package com.example.taskmanagement.model;

public enum SessionStatus {
    ACTIVE,    // сессия активна
    REVOKED,   // отозвана (выполнен refresh или logout)
    EXPIRED    // истек срок действия (может устанавливаться планировщиком)
}