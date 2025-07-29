package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolation;
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

    @Test
    void handleValidationExceptions_WithConstraintViolationException_ReturnsErrorResponse() {
        // Создаем mock ConstraintViolationException
        jakarta.validation.ConstraintViolationException constraintException =
                mock(jakarta.validation.ConstraintViolationException.class);

        // Создаем mock ConstraintViolation
        jakarta.validation.ConstraintViolation<Object> violation = mock(jakarta.validation.ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("Некорректный email");
        when(constraintException.getConstraintViolations()).thenReturn(java.util.Set.of(violation));

        ru.practicum.shareit.exception.ErrorResponse result = globalExceptionHandler.handleValidationExceptions(constraintException);

        assertNotNull(result);
        assertEquals("Ошибка валидации", result.getError());
        assertEquals("email: Некорректный email", result.getMessage());
    }

    @Test
    void handleValidationExceptions_WithMultipleFieldErrors_ReturnsErrorResponse() {
        FieldError fieldError1 = new FieldError("user", "name", "Имя не может быть пустым");
        FieldError fieldError2 = new FieldError("user", "email", "Некорректный email");

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ru.practicum.shareit.exception.ErrorResponse result = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        assertNotNull(result);
        assertEquals("Ошибка валидации", result.getError());
        assertTrue(result.getMessage().contains("name: Имя не может быть пустым"));
        assertTrue(result.getMessage().contains("email: Некорректный email"));
        assertTrue(result.getMessage().contains(";"));
    }

    @Test
    void handleValidationExceptions_WithConstraintViolationException_MultipleViolations_ReturnsErrorResponse() {
        jakarta.validation.ConstraintViolationException constraintException =
                mock(jakarta.validation.ConstraintViolationException.class);

        jakarta.validation.ConstraintViolation<Object> violation1 = mock(jakarta.validation.ConstraintViolation.class);
        jakarta.validation.ConstraintViolation<Object> violation2 = mock(jakarta.validation.ConstraintViolation.class);

        Path propertyPath1 = mock(Path.class);
        Path propertyPath2 = mock(Path.class);

        when(propertyPath1.toString()).thenReturn("name");
        when(propertyPath2.toString()).thenReturn("email");
        when(violation1.getPropertyPath()).thenReturn(propertyPath1);
        when(violation2.getPropertyPath()).thenReturn(propertyPath2);
        when(violation1.getMessage()).thenReturn("Имя не может быть пустым");
        when(violation2.getMessage()).thenReturn("Некорректный email");
        when(constraintException.getConstraintViolations()).thenReturn(java.util.Set.of(violation1, violation2));

        ru.practicum.shareit.exception.ErrorResponse result = globalExceptionHandler.handleValidationExceptions(constraintException);

        assertNotNull(result);
        assertEquals("Ошибка валидации", result.getError());
        assertNotNull(result.getMessage());
    }

    @Test
    void handleValidationExceptions_WithEmptyFieldErrors_ReturnsErrorResponse() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ru.practicum.shareit.exception.ErrorResponse result = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        assertNotNull(result);
        assertEquals("Ошибка валидации", result.getError());
        assertEquals("", result.getMessage());
    }
}

