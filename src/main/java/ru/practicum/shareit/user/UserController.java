package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Validated
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);
        UserDto createdUser = userService.createUser(userDto);
        log.info("Пользователь создан: {}", createdUser);
        return createdUser;
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(
            @PathVariable @Positive Long userId,
            @RequestBody @Valid UserUpdateDto userUpdateDto) {
        log.info("Обновление пользователя с ID {}: {}", userId, userUpdateDto);
        UserDto updatedUser = userService.updateUser(userId, userUpdateDto);
        log.info("Пользователь обновлен: {}", updatedUser);
        return updatedUser;
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable @Positive Long userId) {
        log.info("Получение пользователя с ID: {}", userId);
        UserDto user = userService.getUserById(userId);
        log.info("Найден пользователь: {}", user);
        return user;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        List<UserDto> users = userService.getAllUsers();
        log.info("Найдено {} пользователей", users.size());
        return users;
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable @Positive Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        userService.deleteUser(userId);
        log.info("Пользователь с ID {} удален", userId);
    }
}
