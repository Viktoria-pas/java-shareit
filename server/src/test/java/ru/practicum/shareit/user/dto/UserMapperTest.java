package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    @Test
    @DisplayName("Should convert User to UserDto correctly")
    void shouldConvertUserToUserDto() {

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        UserDto result = UserMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Should convert UserDto to User correctly")
    void shouldConvertUserDtoToUser() {

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        User result = UserMapper.toUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Should handle null User when converting to UserDto")
    void shouldHandleNullUserWhenConvertingToUserDto() {

        assertThrows(NullPointerException.class, () -> UserMapper.toUserDto(null));
    }

}
