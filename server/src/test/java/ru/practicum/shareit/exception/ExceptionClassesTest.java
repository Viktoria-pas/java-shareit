package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionClassesTest {

    @Test
    void conflictException_WithMessage_CreatesExceptionWithCorrectMessage() {

        String message = "Конфликт данных";

        ConflictException exception = new ConflictException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void notFoundExceptionWithEntityTypeAndId_CreatesFormattedMessage() {

        String entityType = "Пользователь";
        Long id = 123L;

        NotFoundException exception = new NotFoundException(entityType, id);

        assertEquals("Пользователь с ID 123 не найден", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void notFoundExceptionWithCustomMessage_CreatesExceptionWithCorrectMessage() {

        String message = "Кастомное сообщение об ошибке";

        NotFoundException exception = new NotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void errorResponse_WithErrorAndMessage_CreatesObjectWithCorrectFields() {

        String error = "Ошибка валидации";
        String message = "Поле не может быть пустым";

        ErrorResponse errorResponse = new ErrorResponse(error, message);

        assertNotNull(errorResponse);
    }
}