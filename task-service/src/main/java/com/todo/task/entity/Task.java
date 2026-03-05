package com.todo.task.entity;

import com.todo.common.enums.StatusTask;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTask status = StatusTask.ACTIVE;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // ← только ID, не связь с User!

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "date_time_of_completion")
    private LocalDateTime dateTimeOfCompletion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Task(String name, String description, LocalDate deadline, Long userId) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.userId = userId;
        this.status = StatusTask.ACTIVE;
    }

    public void complete() {
        this.status = StatusTask.COMPLETED;
        this.dateTimeOfCompletion = LocalDateTime.now();
    }

    public void activate() {
        this.status = StatusTask.ACTIVE;
        this.dateTimeOfCompletion = null;
    }
}