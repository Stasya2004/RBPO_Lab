package com.example.taskmanagement.controller;

import com.example.taskmanagement.model.Project;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* ===================== GET ===================== */

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    /* ===================== CREATE ===================== */

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    /* ===================== UPDATE ===================== */

    // 👤 пользователь редактирует себя
    @PutMapping("/me")
    public User updateMyself(@RequestBody User request,
                             Authentication auth) {

        String username = auth.getName();
        return userService.updateMyProfile(username, request);
    }

    // 🔐 админ редактирует любого
    @PutMapping("/{id}")
    public User updateByAdmin(@PathVariable Long id,
                              @RequestBody User request) {

        return userService.updateByAdmin(id, request);
    }

    /* ===================== DELETE ===================== */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
                       Authentication auth) {
        userService.deleteByAdmin(id, auth.getAuthorities());
    }

    /* ===================== PROJECTS ===================== */

    @GetMapping("/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable Long userId) {
        return userService.getUserProjects(userId);
    }
}