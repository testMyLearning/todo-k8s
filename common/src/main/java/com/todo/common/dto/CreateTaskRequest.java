package com.todo.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "Название задачи обязательно")
        @Size(min=3,max=30,message="Длина названия от 3 до 30 символов")
        String name,
        @NotBlank(message = "Описание обязательно")
        String description,

        @NotNull(message = "Дедлайн обязателен")
        LocalDate deadline
) {}
