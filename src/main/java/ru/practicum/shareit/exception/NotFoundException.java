package ru.practicum.shareit.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String entityType, Long id) {
        super(String.format("%s с ID %d не найден", entityType, id));
    }

    public NotFoundException(String message) {
        super(message);
    }
}
