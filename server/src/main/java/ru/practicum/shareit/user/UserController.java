package ru.practicum.shareit.user;

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
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("=== Server: ПОЛУЧЕН ЗАПРОС createUser ===");
        log.info("Server: входящие данные: {}", userDto);
        try {
            UserDto createdUser = userService.createUser(userDto);
            log.info("Server: пользователь создан успешно: {}", createdUser);
            log.info("Server: возвращаем ответ: {}", createdUser);
            return createdUser;
        } catch (Exception e) {
            log.error("Server: ОШИБКА при создании пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDto userUpdateDto) {
        log.info("=== Server: ПОЛУЧЕН ЗАПРОС updateUser ===");
        log.info("Server: обновление пользователя с ID {}: {}", userId, userUpdateDto);
        try {
            UserDto updatedUser = userService.updateUser(userId, userUpdateDto);
            log.info("Server: пользователь обновлен успешно: {}", updatedUser);
            return updatedUser;
        } catch (Exception e) {
            log.error("Server: ОШИБКА при обновлении пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("=== Server: ПОЛУЧЕН ЗАПРОС getUserById ===");
        log.info("Server: получение пользователя с ID: {}", userId);
        try {
            UserDto user = userService.getUserById(userId);
            log.info("Server: найден пользователь: {}", user);
            return user;
        } catch (Exception e) {
            log.error("Server: ОШИБКА при получении пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("=== Server: ПОЛУЧЕН ЗАПРОС getAllUsers ===");
        try {
            List<UserDto> users = userService.getAllUsers();
            log.info("Server: найдено {} пользователей", users.size());
            return users;
        } catch (Exception e) {
            log.error("Server: ОШИБКА при получении всех пользователей: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("=== Server: ПОЛУЧЕН ЗАПРОС deleteUser ===");
        log.info("Server: удаление пользователя с ID: {}", userId);
        try {
            userService.deleteUser(userId);
            log.info("Server: пользователь с ID {} удален успешно", userId);
        } catch (Exception e) {
            log.error("Server: ОШИБКА при удалении пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
}
