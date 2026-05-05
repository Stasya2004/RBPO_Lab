package com.example.taskmanagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.IN_PROGRESS;

    /* ---------------- ПРОЕКТ ---------------- */

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"tasks", "users"})
    private Project project;

    /* ---------------- ПОЛЬЗОВАТЕЛИ ---------------- */

    @ManyToMany
    @JoinTable(
            name = "task_users",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"projects"})
    private List<User> users;

    /* ---------------- КОММЕНТАРИИ ---------------- */

    @OneToMany(mappedBy = "task")
    @JsonIgnoreProperties({"task", "project", "user"})
    private List<Comment> comments;

    /* ---------------- ТЕГИ (этап 4) ---------------- */

    @ManyToMany
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties({"tasks"})
    private List<Tag> tags;

    /* ---------------- КОНСТРУКТОРЫ ---------------- */

    public Task() {}

    public Task(
            Long id,
            String name,
            String description,
            TaskStatus status,
            Project project,
            List<User> users,
            List<Comment> comments,
            List<Tag> tags
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status != null ? status : TaskStatus.IN_PROGRESS;
        this.project = project;
        this.users = users;
        this.comments = comments;
        this.tags = tags;
    }

    /* ---------------- GET / SET ---------------- */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
