package ru.practicum.shareit.user;

import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserUpdateDto;

public class UserValidator {

    public void validateUserData(String name, String email, boolean isRequired) {
        if (isRequired) {
            if (name == null || name.trim().isEmpty()) {
                throw new ValidationException("Имя пользователя не может быть пустым");
            }

            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email пользователя не может быть пустым");
            }
        }

        if (name != null) {
            if (name.trim().isEmpty()) {
                throw new ValidationException("Имя пользователя не может быть пустым");
            }

            if (name.length() > 255) {
                throw new ValidationException("Имя пользователя не может быть длиннее 255 символов");
            }
        }

        if (email != null) {
            if (email.trim().isEmpty()) {
                throw new ValidationException("Email не может быть пустым");
            }

            if (!email.contains("@")) {
                throw new ValidationException("Email должен содержать символ @");
            }

            if (email.length() > 255) {
                throw new ValidationException("Email не может быть длиннее 255 символов");
            }
        }
    }

    public void validateUserUpdateData(UserUpdateDto userDto) {
        validateUserData(userDto.getName(), userDto.getEmail(), false);
    }
}
