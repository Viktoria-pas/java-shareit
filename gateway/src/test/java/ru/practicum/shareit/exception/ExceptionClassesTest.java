package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionClassesTest {

    @Test
    void validationException_WithMessage_CreatesExceptionWithCorrectMessage() {

        String message = "Ошибка валидации данных";

        ValidationException exception = new ValidationException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }
}
