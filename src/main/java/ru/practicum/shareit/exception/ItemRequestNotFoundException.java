package ru.practicum.shareit.exception;

public class ItemRequestNotFoundException extends NotFoundException {
    public ItemRequestNotFoundException(Long requestId) {
        super("Запрос с ID " + requestId + " не найден");
    }
}
