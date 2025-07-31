package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserControllerValidationTest {

    private final UserValidator validator = new UserValidator();

    @Test
    void validateUserData_shouldPassForValidData() {
        assertDoesNotThrow(() -> validator.validateUserData("Valid Name", "email@test.com", true));
    }

    @Test
    void validateUserData_shouldThrowWhenNameIsNullAndRequired() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserData(null, "email@test.com", true)
        );
        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
    }

    @Test
    void validateUserData_shouldThrowWhenNameIsEmptyAndRequired() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserData("", "email@test.com", true)
        );
        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
    }

    @Test
    void validateUserData_shouldThrowWhenEmailIsNullAndRequired() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserData("Name", null, true)
        );
        assertEquals("Email пользователя не может быть пустым", exception.getMessage());
    }

    @Test
    void validateUserData_shouldThrowWhenEmailIsEmptyAndRequired() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserData("Name", "", true)
        );
        assertEquals("Email пользователя не может быть пустым", exception.getMessage());
    }

    @Test
    void validateUserData_shouldThrowWhenNameTooLong() {
        String longName = "a".repeat(256);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserData(longName, "email@test.com", true)
        );
        assertEquals("Имя пользователя не может быть длиннее 255 символов", exception.getMessage());
    }

    @Test
    void validateUserData_shouldThrowWhenEmailInvalid() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserData("Name", "invalid-email", true)
        );
        assertEquals("Email должен содержать символ @", exception.getMessage());
    }

    @Test
    void validateUserData_shouldThrowWhenEmailTooLong() {
        String longEmail = "a".repeat(250) + "@test.com"; // 256 символов
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserData("Name", longEmail, true)
        );
        assertEquals("Email не может быть длиннее 255 символов", exception.getMessage());
    }

    @Test
    void validateUserUpdateData_shouldPassForValidData() {
        UserUpdateDto dto = new UserUpdateDto("New Name", "new@email.com");
        assertDoesNotThrow(() -> validator.validateUserUpdateData(dto));
    }

    @Test
    void validateUserUpdateData_shouldThrowWhenEmptyName() {
        UserUpdateDto dto = new UserUpdateDto("", "email@test.com");
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUserUpdateData(dto)
        );
        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
    }

}
