package com.example.taskmanagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /* ---------------- ЗАДАЧИ ---------------- */

    @ManyToMany(mappedBy = "tags")
    @JsonIgnoreProperties({
            "tags",
            "users",
            "comments"
    })
    private List<Task> tasks;

    /* ---------------- КОНСТРУКТОРЫ ---------------- */

    public Tag() {}

    public Tag(Long id, String name, List<Task> tasks) {
        this.id = id;
        this.name = name;
        this.tasks = tasks;
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

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
