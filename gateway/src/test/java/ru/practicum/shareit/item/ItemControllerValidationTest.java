package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemControllerValidationTest {
    private final ItemValidator validator = new ItemValidator();

    @Test
    void validateItemData_shouldPassForValidItem() {
        ItemDto item = new ItemDto("Valid Item", "Valid Description", true, null);
        assertDoesNotThrow(() -> validator.validateItemData(item));
    }

    @Test
    void validateItemData_shouldThrowWhenNameIsNull() {
        ItemDto item = new ItemDto(null, "Description", true, null);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateItemData(item)
        );
        assertEquals("Название предмета не может быть пустым", exception.getMessage());
    }

    @Test
    void validateItemData_shouldThrowWhenNameIsEmpty() {
        ItemDto item = new ItemDto("", "Description", true, null);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateItemData(item)
        );
        assertEquals("Название предмета не может быть пустым", exception.getMessage());
    }

    @Test
    void validateItemData_shouldThrowWhenDescriptionIsNull() {
        ItemDto item = new ItemDto("Name", null, true, null);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateItemData(item)
        );
        assertEquals("Описание предмета не может быть пустым", exception.getMessage());
    }

    @Test
    void validateItemData_shouldThrowWhenAvailableIsNull() {
        ItemDto item = new ItemDto("Name", "Description", null, null);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateItemData(item)
        );
        assertEquals("Статус доступности предмета обязателен", exception.getMessage());
    }

    @Test
    void validateItemData_shouldThrowWhenNameTooLong() {
        String longName = "a".repeat(256);
        ItemDto item = new ItemDto(longName, "Description", true, null);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateItemData(item)
        );
        assertEquals("Название предмета не может быть длиннее 255 символов", exception.getMessage());
    }

    @Test
    void validateItemData_shouldThrowWhenDescriptionTooLong() {
        String longDesc = "a".repeat(1001);
        ItemDto item = new ItemDto("Name", longDesc, true, null);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateItemData(item)
        );
        assertEquals("Описание предмета не может быть длиннее 1000 символов", exception.getMessage());
    }

    @Test
    void validateItemUpdateData_shouldPassForValidUpdate() {
        ItemUpdateDto update = new ItemUpdateDto("New Name", "New Description", true);
        assertDoesNotThrow(() -> validator.validateItemUpdateData(update));
    }

    @Test
    void validateItemUpdateData_shouldThrowWhenEmptyName() {
        ItemUpdateDto update = new ItemUpdateDto("", "Description", true);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateItemUpdateData(update)
        );
        assertEquals("Название предмета не может быть пустым", exception.getMessage());
    }

    @Test
    void validateItemUpdateData_shouldThrowWhenNameTooLong() {
        String longName = "a".repeat(256);
        ItemUpdateDto update = new ItemUpdateDto(longName, "Description", true);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateItemUpdateData(update)
        );
        assertEquals("Название предмета не может быть длиннее 255 символов", exception.getMessage());
    }

    @Test
    void validateComment_shouldPassForValidComment() {
        CommentDto comment = new CommentDto("Valid comment text");
        assertDoesNotThrow(() -> validator.validateComment(comment));
    }

    @Test
    void validateComment_shouldThrowWhenTextIsNull() {
        CommentDto comment = new CommentDto(null);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateComment(comment)
        );
        assertEquals("Текст комментария не может быть пустым", exception.getMessage());
    }

    @Test
    void validateComment_shouldThrowWhenTextIsEmpty() {
        CommentDto comment = new CommentDto("");
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateComment(comment)
        );
        assertEquals("Текст комментария не может быть пустым", exception.getMessage());
    }

    @Test
    void validateComment_shouldThrowWhenTextTooLong() {
        String longText = "a".repeat(1001);
        CommentDto comment = new CommentDto(longText);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateComment(comment)
        );
        assertEquals("Комментарий не может быть длиннее 1000 символов", exception.getMessage());
    }
}
