package com.todo.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.todo.common.dto.CreateTaskRequest;
import com.todo.common.dto.TaskDto;
import com.todo.common.dtoAsync.PageResponse;
import com.todo.task.controller.TaskController;
import com.todo.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/tasks должен возвращать список задач с пагинацией")
    void shouldReturnTaskList() throws Exception {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        String sortBy = "createdAt";
        String sortDirection = "DESC";

        List<TaskDto> tasks = List.of(
                new TaskDto(UUID.randomUUID(), "Задача 1", "Описание 1", "ACTIVE", LocalDate.now(), null, userId),
                new TaskDto(UUID.randomUUID(), "Задача 2", "Описание 2", "ACTIVE", LocalDate.now(), null, userId)
        );

        PageResponse<TaskDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(tasks);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);
        pageResponse.setFirstPage(true);
        pageResponse.setLastPage(true);

        when(taskService.findAll(eq(userId), eq(page), eq(size), eq(sortBy), eq(sortDirection)))
                .thenReturn(pageResponse);

        // when/then
        mockMvc.perform(get("/api/tasks")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sortBy", sortBy)
                        .param("sortDirection", sortDirection)
                        .header("X-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.firstPage").value(true))
                .andExpect(jsonPath("$.lastPage").value(true));

        verify(taskService, times(1))
                .findAll(eq(userId), eq(page), eq(size), eq(sortBy), eq(sortDirection));
    }

@Test
        @DisplayName("POST api/tasks - создание задачи")
        void createTaskTest() throws Exception {
Long userId =1L;
    CreateTaskRequest request = new CreateTaskRequest(
            "name",
            "description",
            LocalDate.of(2026,3,5)
    );
    TaskDto expected = new TaskDto(
            UUID.randomUUID(),
            "name",
            "description",
            "ACTIVE",
            LocalDate.of(2026,3,5),
            null,
            userId
    );
    when(taskService.create(any(CreateTaskRequest.class), eq(userId)))
            .thenReturn(expected);

    mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)  // ← указываем тип
                    .content(objectMapper.writeValueAsString(request))           // ← JSON в body
                    .header("X-User-Id", String.valueOf(userId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())      // ← проверяем поля
            .andExpect(jsonPath("$.name").value("name"))
            .andExpect(jsonPath("$.description").value("description"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.userId").value(userId));

    // Проверяем, что сервис вызвали
    verify(taskService, times(1)).create(any(CreateTaskRequest.class), eq(userId));

}


}
