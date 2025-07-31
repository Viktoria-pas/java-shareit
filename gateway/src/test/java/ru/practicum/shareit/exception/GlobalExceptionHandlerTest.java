package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
        assertEquals("Ошибка валидации", result.getError());
        assertEquals("email: Некорректный email", result.getMessage());

        verify(methodArgumentNotValidException).getBindingResult();
        verify(bindingResult).getFieldErrors();
    }

    @Test
    void handleValidationExceptions_WithConstraintViolationException_ReturnsErrorResponse() {

        ConstraintViolationException constraintException = mock(ConstraintViolationException.class);
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("userId");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("должно быть больше 0");
        when(constraintException.getConstraintViolations()).thenReturn(Set.of(violation));

        ErrorResponse result = globalExceptionHandler.handleValidationExceptions(constraintException);

        assertNotNull(result);
        assertEquals("Ошибка валидации", result.getError());
        assertEquals("userId: должно быть больше 0", result.getMessage());
    }

    @Test
    void handleValidationExceptions_WithMultipleFieldErrors_ReturnsErrorResponse() {

        FieldError fieldError1 = new FieldError("user", "name", "Имя не может быть пустым");
        FieldError fieldError2 = new FieldError("user", "email", "Email должен содержать @");

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ErrorResponse result = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        assertNotNull(result);
        assertEquals("Ошибка валидации", result.getError());
        assertTrue(result.getMessage().contains("name: Имя не может быть пустым"));
        assertTrue(result.getMessage().contains("email: Email должен содержать @"));
        assertTrue(result.getMessage().contains(";"));
    }

    @Test
    void handleCustomValidationExceptions_ReturnsErrorMap() {

        ValidationException exception = new ValidationException("Дата начала не может быть позже даты окончания");

        Map<String, String> result = globalExceptionHandler.handleCustomValidationExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Дата начала не может быть позже даты окончания", result.get("error"));
    }

    @Test
    void handleCustomValidationExceptions_WithBookingState_ReturnsErrorMap() {

        ValidationException exception = new ValidationException("Неизвестный параметр state: INVALID");

        Map<String, String> result = globalExceptionHandler.handleCustomValidationExceptions(exception);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Неизвестный параметр state: INVALID", result.get("error"));
    }

    @Test
    void handleAllExceptions_ReturnsGenericErrorMap() {

        RuntimeException exception = new RuntimeException("Ошибка соединения с сервером");

        Map<String, String> result = globalExceptionHandler.handleAllExceptions(exception);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
        assertEquals("Произошла непредвиденная ошибка", result.get("message"));
    }

    @Test
    void handleValidationExceptions_WithEmptyFieldErrors_ReturnsErrorResponse() {

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ErrorResponse result = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        assertNotNull(result);
        assertEquals("Ошибка валидации", result.getError());
        assertEquals("", result.getMessage());
    }
}
