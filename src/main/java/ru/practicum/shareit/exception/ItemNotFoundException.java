package ru.practicum.shareit.exception;

public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(Long itemId) {
        super("Вещь с ID " + itemId + " не найдена");
    }
}
