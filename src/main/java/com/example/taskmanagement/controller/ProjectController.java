package com.example.taskmanagement.controller;

import com.example.taskmanagement.model.Project;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.service.ProjectService;
import org.springframework.web.bind.annotation.*;
import com.example.taskmanagement.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service; // <-- обязательно объявляем сервис
    private UserService userService;

    public ProjectController(ProjectService service) {
        this.service = service; // <-- присваиваем через конструктор
    }

    @GetMapping
    public List<Project> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Project getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Project create(@RequestBody Project project) {
        return service.create(project);
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable Long id, @RequestBody Project project) {
        return service.update(id, project);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ----------------- Две новые функции -----------------

    @PostMapping("/{projectId}/users/{userId}")
    public Project assignUser(@PathVariable Long projectId, @PathVariable Long userId) {
        return service.assignUser(projectId, userId);
    }

    @GetMapping("/{projectId}/tasks")
    public List<Task> getProjectTasks(@PathVariable Long projectId) {
        Project project = service.getById(projectId);
        if (project != null) {
            return project.getTasks();
        } else {
            throw new RuntimeException("Project not found");
        }
    }
}