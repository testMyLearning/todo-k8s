package com.todo.task.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.common.dto.CreateTaskRequest;
import com.todo.common.dto.TaskDto;
import com.todo.common.dto.UpdateTaskRequest;
import com.todo.common.enums.StatusTask;
import com.todo.task.entity.Task;
import com.todo.task.kafka.SelfTransaction;
import com.todo.task.mapper.TaskMapper;
import com.todo.task.mapper.TaskMapperImpl;
import com.todo.task.repository.OutboxRepository;
import com.todo.task.repository.TaskRepository;
import jakarta.xml.bind.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private SelfTransaction selfTransaction;
    @Mock
    private ObjectMapper objectMapper;

    private TaskMapper mapper = new TaskMapperImpl();
    @InjectMocks
    private TaskService taskService;



    private Task beforeTask;
    private UpdateTaskRequest updateTask;
    private Task afterTask;
    private Long userId = 1L;
    private UUID taskId = UUID.randomUUID();
    private TaskDto expectedDto;
    private CreateTaskRequest createTaskRequest;
    private Task afterCreate;

@BeforeEach
    void setUp() {
    taskService = new TaskService(
            taskRepository,
            mapper,
            outboxRepository,
            objectMapper
    );


    beforeTask = new Task();
    beforeTask.setUserId(userId);
    beforeTask.setId(taskId);
    beforeTask.setStatus(StatusTask.ACTIVE);
    beforeTask.setName("старое имя");
    beforeTask.setDescription("старое описание");
    beforeTask.setDeadline(LocalDate.of(2026, 3, 5));

    updateTask = new UpdateTaskRequest(taskId,
            null,
            null,
            "COMPLETED",
            null);

    afterTask = new Task();
    afterTask.setId(taskId);
    afterTask.setUserId(userId);
    afterTask.setName("старое имя");           // осталось
    afterTask.setDescription("старое описание"); // осталось
    afterTask.setStatus(StatusTask.COMPLETED);   // изменилось!
    afterTask.setDeadline(LocalDate.of(2026, 3, 5)); // осталось

    expectedDto = new TaskDto(
            taskId,
            "старое имя",
            "старое описание",
            "COMPLETED",
            LocalDate.of(2026, 3, 5),
            null,
            userId
    );

}

   @Test
   @DisplayName(value="Проверка обновления задачи")
    void testUpdateTest() throws JsonProcessingException {
    when(mapper.toDto(any(Task.class))).thenReturn(expectedDto);
       when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(beforeTask));
        when(taskRepository.save(any(Task.class))).thenReturn(afterTask);
        TaskDto taskDto = taskService.update(updateTask,userId);

        assertThat(taskDto.status()).isEqualTo("COMPLETED");

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(outboxRepository, times(1)).save(any());



    }

    @Test
    @DisplayName("тест на создание задач")
    void create() throws JsonProcessingException {
    createTaskRequest = new CreateTaskRequest(
            "Новая задача",
            "описание",
            LocalDate.of(2026,3,6)
    );
        expectedDto = new TaskDto(
                taskId,
                "Новая задача",
                "описание",
                "ACTIVE",
                LocalDate.of(2026, 3, 6),
                null,
                userId
        );
        Task savedTask = new Task();
        savedTask.setId(taskId);
        savedTask.setName("Новая задача");
        savedTask.setDescription("описание");
        savedTask.setDeadline(LocalDate.of(2026, 3, 6));
        savedTask.setStatus(StatusTask.ACTIVE);
        savedTask.setUserId(userId);

    when(mapper.toEntity(any(CreateTaskRequest.class))).thenReturn(savedTask);
    when(mapper.toDto(any(Task.class))).thenReturn(expectedDto);
    when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        TaskDto result = taskService.create(createTaskRequest, userId);
        assertThat(result).isNotNull();
    assertThat(result.name()).isEqualToIgnoringCase("Новая задача");
        assertThat(result.description()).isEqualToIgnoringCase("описание");
        assertThat(result.deadline()).isEqualTo(LocalDate.of(2026,3,6));
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.status()).isEqualTo("ACTIVE");

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(outboxRepository, times(1)).save(any());
        verify(objectMapper, times(1)).writeValueAsString(any());
    }


}
