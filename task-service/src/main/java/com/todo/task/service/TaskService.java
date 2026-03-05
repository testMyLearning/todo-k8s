package com.todo.task.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.common.dto.CreateTaskRequest;
import com.todo.common.dto.TaskDto;

import com.todo.common.dto.UpdateTaskRequest;
import com.todo.common.dtoAsync.PageResponse;
import com.todo.common.enums.StatusTask;
import com.todo.common.event.TaskEvent;
import com.todo.task.entity.OutboxEvent;
import com.todo.task.entity.Task;
import com.todo.task.mapper.TaskMapper;
import com.todo.task.repository.OutboxRepository;
import com.todo.task.repository.TaskRepository;
import io.micrometer.observation.annotation.Observed;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Service
@Slf4j
@RequiredArgsConstructor
@Observed(name = "task.service")
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    @Transactional(readOnly = true)
    public PageResponse<TaskDto> findAll(Long userId,
                                                            int page,
                                                            int size,
                                                            String sortBy,
                                                            String sortDirection) {
        log.info("Пришел запрос в асинхронный findall от {} в потоке {}", userId, Thread.currentThread().getName());
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findByUserId(userId, pageable);
            PageResponse<TaskDto> response = new PageResponse<>();
            response.setContent(taskMapper.toDtoList(taskPage.getContent()));
            response.setPage(taskPage.getNumber());
            response.setSize(taskPage.getSize());
            response.setTotalElements(taskPage.getTotalElements());
            response.setTotalPages(taskPage.getTotalPages());
            response.setFirstPage(taskPage.isFirst());
            response.setLastPage(taskPage.isLast());
            return response;
    }
@Observed(name = "create")
    @Transactional
    public TaskDto create(@Valid CreateTaskRequest request, Long userId) {
        Task task = taskMapper.toEntity(request);
        task.setUserId(userId);
        Task savedTask = taskRepository.save(task);

        // Отправляем событие в outbox (синхронно, быстро)
        sendEvent(savedTask, userId, "TASK_CREATED");

        log.info("Task created with id: {}", savedTask.getId());
        return taskMapper.toDto(savedTask);
    }
@Observed(name="update")
    @Transactional
    public TaskDto update(@Valid UpdateTaskRequest request, Long userId) {

    try {
        Task task = taskRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Обновляем поля
        if (request.name() != null && !request.name().isBlank()) {
            task.setName(request.name());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.deadline() != null) {
            task.setDeadline(request.deadline());
        }
        if (request.status() != null) {
            task.setStatus(StatusTask.valueOf(request.status()));
        }

        Task savedTask = taskRepository.save(task);
        sendEvent(savedTask, userId, "TASK_UPDATED");

        log.info("Task updated with id: {}", savedTask.getId());
        return taskMapper.toDto(savedTask);
    } catch (OptimisticLockException e) {
        // Кто-то уже изменил задачу
        log.warn("Conflict updating task {} for user {}",
                request.id(), userId);
        throw new OptimisticLockException("занято");
    }
}
@Observed(name ="sendEvent")
@Transactional(propagation = MANDATORY)
    private void sendEvent(Task task, Long userId, String eventType) {
        TaskEvent event = new TaskEvent(
                task.getId(),
                userId,
                task.getName(),
                task.getStatus().name(),
                task.getDeadline(),
                eventType,
                "task-service"
        );

        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(task.getId().toString())
                    .eventType(event.getEventType())
                    .service(event.getService())
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Saved {} event to outbox for task: {}", eventType, task.getId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize {} event for task: {}", eventType, task.getId(), e);
            throw new RuntimeException("Error saving event to outbox", e);
        }
    }
}


