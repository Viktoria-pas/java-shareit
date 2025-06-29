package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toUser(userDto);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new NotFoundException("Пользователь", userId);
        }

        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(existingUser.getEmail())) {
            if (users.values().stream().anyMatch(u -> u.getEmail().equals(userUpdateDto.getEmail()))) {
                throw new ConflictException("Email " + userUpdateDto.getEmail() + " уже используется");
            }
            existingUser.setEmail(userUpdateDto.getEmail());
        }

        if (userUpdateDto.getName() != null) {
            existingUser.setName(userUpdateDto.getName());
        }

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь", userId);
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь", userId);
        }
        users.remove(userId);
    }
}
