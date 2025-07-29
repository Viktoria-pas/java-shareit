package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ConstraintViolation<Object> constraintViolation;

    @Mock
    private Path path;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationExceptions_WithMethodArgumentNotValidException_ReturnsErrorResponse() {

        FieldError fieldError = new FieldError("user", "email", "Некорректный email");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ErrorResponse result = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        assertNotNull(result);

        verify(methodArgumentNotValidException).getBindingResult();
        verify(bindingResult).getFieldErrors();
    }

    @Test
    void handleCustomValidationExceptions_ReturnsErrorMap() {

        ValidationException exception = new ValidationException("Некорректные данные");

        Map<String, String> result = globalExceptionHandler.handleCustomValidationExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Некорректные данные", result.get("error"));
    }

    @Test
    void handleNotFoundExceptions_ReturnsErrorMap() {

        NotFoundException exception = new NotFoundException("Пользователь", 1L);

        Map<String, String> result = globalExceptionHandler.handleNotFoundExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Объект не найден", result.get("error"));
        assertEquals("Пользователь с ID 1 не найден", result.get("message"));
    }

    @Test
    void handleNotFoundExceptions_WithCustomMessage_ReturnsErrorMap() {

        NotFoundException exception = new NotFoundException("Кастомное сообщение об ошибке");

        Map<String, String> result = globalExceptionHandler.handleNotFoundExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Объект не найден", result.get("error"));
        assertEquals("Кастомное сообщение об ошибке", result.get("message"));
    }

    @Test
    void handleConflictExceptions_ReturnsErrorMap() {

        ConflictException exception = new ConflictException("Email уже существует");

        Map<String, String> result = globalExceptionHandler.handleConflictExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Email уже существует", result.get("error"));
    }

    @Test
    void handleAllExceptions_ReturnsGenericErrorMap() {

        RuntimeException exception = new RuntimeException("Неожиданная ошибка");

        Map<String, String> result = globalExceptionHandler.handleAllExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
        assertEquals("Произошла непредвиденная ошибка", result.get("message"));
    }
}
