package ru.practicum.shareit.user.dto;

import lombok.Data;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;

@Data
public class UserUpdateDto {

    @Nullable
    private String name;

    @Nullable
    @Email(message = "Неверный формат email")
    private String email;
}