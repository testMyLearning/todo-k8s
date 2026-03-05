package com.todo.task.mapper;

import com.todo.common.dto.CreateTaskRequest;
import com.todo.common.enums.StatusTask;
import com.todo.task.entity.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


public class TaskMapperTest {
    private final TaskMapper mapper = new TaskMapperImpl();


    @Test
    void toEntity() {
        CreateTaskRequest request = new CreateTaskRequest("Иван", "Описание",
                LocalDate.of(2026,2, 2));

        Task task = mapper.toEntity(request);


        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Иван");
        assertThat(task.getDescription()).isEqualTo("Описание");
        assertThat(task.getDeadline()).isEqualTo(LocalDate.of(2026,2, 2));
        assertThat(task.getStatus()).isEqualTo(StatusTask.ACTIVE); // значение по умолчанию
        assertThat(task.getUserId()).isNull(); // userId не должен маппиться из запроса
    }
}
