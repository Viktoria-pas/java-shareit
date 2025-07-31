package ru.practicum.shareit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        mockLogger = mock(Logger.class);

        ReflectionTestUtils.setField(globalExceptionHandler, "log", mockLogger);
    }

    @Test
    void handleNotFoundExceptions_WithEntityAndId_ShouldReturnCorrectErrorMap() {

        NotFoundException exception = new NotFoundException("Пользователь", 1L);

        Map<String, String> result = globalExceptionHandler.handleNotFoundExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Объект не найден", result.get("error"));
        assertEquals("Пользователь с ID 1 не найден", result.get("message"));

        verify(mockLogger).warn("Объект не найден: {}", "Пользователь с ID 1 не найден");
    }

    @Test
    void handleNotFoundExceptions_WithCustomMessage_ShouldReturnCorrectErrorMap() {

        NotFoundException exception = new NotFoundException("Запрос с id 123 не найден");

        Map<String, String> result = globalExceptionHandler.handleNotFoundExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Объект не найден", result.get("error"));
        assertEquals("Запрос с id 123 не найден", result.get("message"));

        verify(mockLogger).warn("Объект не найден: {}", "Запрос с id 123 не найден");
    }

    @Test
    void handleNotFoundExceptions_WithItemNotFound_ShouldReturnCorrectErrorMap() {

        NotFoundException exception = new NotFoundException("Предмет", 5L);

        Map<String, String> result = globalExceptionHandler.handleNotFoundExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Объект не найден", result.get("error"));
        assertEquals("Предмет с ID 5 не найден", result.get("message"));

        verify(mockLogger).warn("Объект не найден: {}", "Предмет с ID 5 не найден");
    }

    @Test
    void handleNotFoundExceptions_WithBookingNotFound_ShouldReturnCorrectErrorMap() {

        NotFoundException exception = new NotFoundException("Бронирование", 10L);

        Map<String, String> result = globalExceptionHandler.handleNotFoundExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Объект не найден", result.get("error"));
        assertEquals("Бронирование с ID 10 не найден", result.get("message"));

        verify(mockLogger).warn("Объект не найден: {}", "Бронирование с ID 10 не найден");
    }

    @Test
    void handleNotFoundExceptions_WithEmptyMessage_ShouldHandleCorrectly() {

        NotFoundException exception = new NotFoundException("");

        Map<String, String> result = globalExceptionHandler.handleNotFoundExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Объект не найден", result.get("error"));
        assertEquals("", result.get("message"));

        verify(mockLogger).warn("Объект не найден: {}", "");
    }

    @Test
    void handleConflictExceptions_WithEmailDuplicate_ShouldReturnCorrectErrorMap() {

        ConflictException exception = new ConflictException("Пользователь с email test@example.com уже существует");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Пользователь с email test@example.com уже существует", result.get("error"));

        verify(mockLogger).warn("Конфликт данных: {}", "Пользователь с email test@example.com уже существует");
    }

    @Test
    void handleConflictExceptions_WithItemUnavailable_ShouldReturnCorrectErrorMap() {

        ConflictException exception = new ConflictException("Предмет недоступен для бронирования");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Предмет недоступен для бронирования", result.get("error"));

        verify(mockLogger).warn("Конфликт данных: {}", "Предмет недоступен для бронирования");
    }

    @Test
    void handleConflictExceptions_WithOwnerBooking_ShouldReturnCorrectErrorMap() {

        ConflictException exception = new ConflictException("Владелец не может забронировать свою вещь");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Владелец не может забронировать свою вещь", result.get("error"));

        verify(mockLogger).warn("Конфликт данных: {}", "Владелец не может забронировать свою вещь");
    }

    @Test
    void handleConflictExceptions_WithBookingAlreadyProcessed_ShouldReturnCorrectErrorMap() {

        ConflictException exception = new ConflictException("Нельзя изменить статус уже обработанного бронирования");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Нельзя изменить статус уже обработанного бронирования", result.get("error"));

        verify(mockLogger).warn("Конфликт данных: {}", "Нельзя изменить статус уже обработанного бронирования");
    }

    @Test
    void handleConflictExceptions_WithAccessDenied_ShouldReturnCorrectErrorMap() {

        ConflictException exception = new ConflictException("Нет доступа к данному бронированию");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Нет доступа к данному бронированию", result.get("error"));

        verify(mockLogger).warn("Конфликт данных: {}", "Нет доступа к данному бронированию");
    }

    @Test
    void handleConflictExceptions_WithCommentRestriction_ShouldReturnCorrectErrorMap() {

        ConflictException exception = new ConflictException("Нельзя оставить комментарий к вещи, которую не брали в аренду");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Нельзя оставить комментарий к вещи, которую не брали в аренду", result.get("error"));

        verify(mockLogger).warn("Конфликт данных: {}", "Нельзя оставить комментарий к вещи, которую не брали в аренду");
    }

    @Test
    void handleConflictExceptions_WithInvalidState_ShouldReturnCorrectErrorMap() {

        ConflictException exception = new ConflictException("Неизвестный параметр state: INVALID");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Неизвестный параметр state: INVALID", result.get("error"));

        verify(mockLogger).warn("Конфликт данных: {}", "Неизвестный параметр state: INVALID");
    }

    @Test
    void handleConflictExceptions_WithEmptyMessage_ShouldHandleCorrectly() {

        ConflictException exception = new ConflictException("");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get("error"));

        verify(mockLogger).warn("Конфликт данных: {}", "");
    }

    @Test
    void handleBadRequest_WithValidationError_ShouldReturnCorrectErrorMap() {

        BadRequestException exception = new BadRequestException("Предмет недоступен для бронирования");

        Map<String, String> result = globalExceptionHandler.handleBadRequest(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Неверный запрос", result.get("error"));
        assertEquals("Предмет недоступен для бронирования", result.get("message"));

        verify(mockLogger).error("{Запрос неверный: {}", "Предмет недоступен для бронирования", exception);
    }

    @Test
    void handleBadRequest_WithOwnerBookingError_ShouldReturnCorrectErrorMap() {

        BadRequestException exception = new BadRequestException("Владелец не может забронировать свою вещь");

        Map<String, String> result = globalExceptionHandler.handleBadRequest(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Неверный запрос", result.get("error"));
        assertEquals("Владелец не может забронировать свою вещь", result.get("message"));

        verify(mockLogger).error("{Запрос неверный: {}", "Владелец не может забронировать свою вещь", exception);
    }

    @Test
    void handleBadRequest_WithDateValidationError_ShouldReturnCorrectErrorMap() {

        BadRequestException exception = new BadRequestException("Дата начала не может быть позже даты окончания");

        Map<String, String> result = globalExceptionHandler.handleBadRequest(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Неверный запрос", result.get("error"));
        assertEquals("Дата начала не может быть позже даты окончания", result.get("message"));

        verify(mockLogger).error("{Запрос неверный: {}", "Дата начала не может быть позже даты окончания", exception);
    }

    @Test
    void handleBadRequest_WithStatusChangeError_ShouldReturnCorrectErrorMap() {

        BadRequestException exception = new BadRequestException("Только владелец может изменить статус бронирования");

        Map<String, String> result = globalExceptionHandler.handleBadRequest(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Неверный запрос", result.get("error"));
        assertEquals("Только владелец может изменить статус бронирования", result.get("message"));

        verify(mockLogger).error("{Запрос неверный: {}", "Только владелец может изменить статус бронирования", exception);
    }

    @Test
    void handleBadRequest_WithProcessedBookingError_ShouldReturnCorrectErrorMap() {

        BadRequestException exception = new BadRequestException("Нельзя изменить статус уже обработанного бронирования");

        Map<String, String> result = globalExceptionHandler.handleBadRequest(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Неверный запрос", result.get("error"));
        assertEquals("Нельзя изменить статус уже обработанного бронирования", result.get("message"));

        verify(mockLogger).error("{Запрос неверный: {}", "Нельзя изменить статус уже обработанного бронирования", exception);
    }

    @Test
    void handleBadRequest_WithEmptyMessage_ShouldHandleCorrectly() {

        BadRequestException exception = new BadRequestException("");

        Map<String, String> result = globalExceptionHandler.handleBadRequest(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Неверный запрос", result.get("error"));
        assertEquals("", result.get("message"));

        verify(mockLogger).error("{Запрос неверный: {}", "", exception);
    }

    @Test
    void handleAllExceptions_WithRuntimeException_ShouldReturnGenericErrorMap() {

        RuntimeException exception = new RuntimeException("Ошибка базы данных");

        Map<String, String> result = globalExceptionHandler.handleAllExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
        assertEquals("Произошла непредвиденная ошибка", result.get("message"));

        verify(mockLogger).error("Внутренняя ошибка сервера: {}", "Ошибка базы данных", exception);
    }

    @Test
    void handleAllExceptions_WithNullPointerException_ShouldReturnGenericErrorMap() {

        NullPointerException exception = new NullPointerException("Объект равен null");

        Map<String, String> result = globalExceptionHandler.handleAllExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
        assertEquals("Произошла непредвиденная ошибка", result.get("message"));

        verify(mockLogger).error("Внутренняя ошибка сервера: {}", "Объект равен null", exception);
    }

    @Test
    void handleAllExceptions_WithSQLException_ShouldReturnGenericErrorMap() {

        RuntimeException exception = new RuntimeException("could not execute statement");

        Map<String, String> result = globalExceptionHandler.handleAllExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
        assertEquals("Произошла непредвиденная ошибка", result.get("message"));

        verify(mockLogger).error("Внутренняя ошибка сервера: {}", "could not execute statement", exception);
    }

    @Test
    void handleAllExceptions_WithIllegalArgumentException_ShouldReturnGenericErrorMap() {

        IllegalArgumentException exception = new IllegalArgumentException("Недопустимый аргумент");

        Map<String, String> result = globalExceptionHandler.handleAllExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
        assertEquals("Произошла непредвиденная ошибка", result.get("message"));

        verify(mockLogger).error("Внутренняя ошибка сервера: {}", "Недопустимый аргумент", exception);
    }

    @Test
    void handleAllExceptions_WithIllegalStateException_ShouldReturnGenericErrorMap() {

        IllegalStateException exception = new IllegalStateException("Недопустимое состояние");

        Map<String, String> result = globalExceptionHandler.handleAllExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
        assertEquals("Произошла непредвиденная ошибка", result.get("message"));

        verify(mockLogger).error("Внутренняя ошибка сервера: {}", "Недопустимое состояние", exception);
    }

    @Test
    void handleAllExceptions_WithCustomException_ShouldReturnGenericErrorMap() {

        Exception exception = new Exception("Кастомная ошибка");

        Map<String, String> result = globalExceptionHandler.handleAllExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
    }
}
