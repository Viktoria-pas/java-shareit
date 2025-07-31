package ru.practicum.shareit.item;

import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

public class ItemValidator {

    public void validateItemData(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
            throw new ValidationException("Название предмета не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().trim().isEmpty()) {
            throw new ValidationException("Описание предмета не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности предмета обязателен");
        }


        if (itemDto.getName().length() > 255) {
            throw new ValidationException("Название предмета не может быть длиннее 255 символов");
        }

        if (itemDto.getDescription().length() > 1000) {
            throw new ValidationException("Описание предмета не может быть длиннее 1000 символов");
        }
    }

    public void validateItemUpdateData(ItemUpdateDto itemDto) {

        if (itemDto.getName() != null && itemDto.getName().trim().isEmpty()) {
            throw new ValidationException("Название предмета не может быть пустым");
        }

        if (itemDto.getDescription() != null && itemDto.getDescription().trim().isEmpty()) {
            throw new ValidationException("Описание предмета не может быть пустым");
        }

        if (itemDto.getName() != null && itemDto.getName().length() > 255) {
            throw new ValidationException("Название предмета не может быть длиннее 255 символов");
        }

        if (itemDto.getDescription() != null && itemDto.getDescription().length() > 1000) {
            throw new ValidationException("Описание предмета не может быть длиннее 1000 символов");
        }
    }

    public void validateComment(CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().trim().isEmpty()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        if (commentDto.getText().length() > 1000) {
            throw new ValidationException("Комментарий не может быть длиннее 1000 символов");
        }
    }
}
