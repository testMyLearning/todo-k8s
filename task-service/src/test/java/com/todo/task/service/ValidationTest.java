package com.todo.task.service;



import com.todo.common.dto.CreateTaskRequest;
import org.junit.jupiter.api.BeforeAll;
import jakarta.validation.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp(){
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    @Test
    @DisplayName("❌ Должен возвращать ошибки валидации при коротком имени")
    void shouldHaveViolationWhenNameTooShort() {
        // given
        CreateTaskRequest request = new CreateTaskRequest(
                "Но",  // слишком короткое
                "описание",
                LocalDate.now().plusDays(1)
        );

        // when
        Set<ConstraintViolation<CreateTaskRequest>> violations =
                validator.validate(request);
assertThat(violations).hasSize(1);
        // then
        assertThat(violations)
//                .isNotEmpty()
//                .anyMatch(v -> v.getMessage().contains("Длина названия от 2 до 15 символов"));
//                .isEmpty();
                .extracting(ConstraintViolation::getMessage)
                .as("Ожидаемое сообщение об ошибке");
    }


    @ParameterizedTest
    @CsvSource({
            "Но, описание, 2026-03-05, 'Длина названия от 3 до 30 символов'",
            " , описание, 2026-03-05, 'Название задачи обязательно'",
            "Задача, , 2026-03-05, 'Описание обязательно'",
            "Задача, описание, , 'Дедлайн обязателен'"
    })
    void testSpecificValidationErrors(String name, String description, LocalDate deadline, String expectedMessage) {
        CreateTaskRequest request = new CreateTaskRequest(name, description, deadline);
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .contains(expectedMessage);
    }
}

