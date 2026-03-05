package com.todo.task;

import com.todo.common.enums.StatusTask;
import com.todo.task.entity.Task;
import com.todo.task.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager manager;

    @Test
    @DisplayName("тестирование сохранения задачи")
    void saveTask() {
        Task task = new Task();
        task.setName("Тестовая задача");
        task.setDescription("Описание");
        task.setUserId(1L);
        task.setStatus(StatusTask.ACTIVE);
        task.setDeadline(LocalDate.now().plusDays(1));

        Task saved = manager.persistFlushFind(task);
        Optional<Task> find = taskRepository.findById(saved.getId());

        assertThat(find).isPresent();
        assertThat(find.get().getName()).isEqualTo("Тестовая задача");
    }
}
