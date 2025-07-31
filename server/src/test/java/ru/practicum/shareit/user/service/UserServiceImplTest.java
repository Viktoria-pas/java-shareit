package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private UserUpdateDto testUserUpdateDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setName("Test User");
        testUserDto.setEmail("test@example.com");

        testUserUpdateDto = new UserUpdateDto();
        testUserUpdateDto.setName("Updated User");
        testUserUpdateDto.setEmail("updated@example.com");
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when email is unique")
        void shouldCreateUser_WhenEmailIsUnique() {

            when(userRepository.existsByEmail(testUserDto.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserDto result = userService.createUser(testUserDto);

            assertNotNull(result);
            assertEquals(testUserDto.getName(), result.getName());
            assertEquals(testUserDto.getEmail(), result.getEmail());
            assertEquals(testUser.getId(), result.getId());

            verify(userRepository).existsByEmail(testUserDto.getEmail());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw ConflictException when email already exists")
        void shouldThrowConflictException_WhenEmailAlreadyExists() {

            when(userRepository.existsByEmail(testUserDto.getEmail())).thenReturn(true);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> userService.createUser(testUserDto));

            assertEquals("Пользователь с email " + testUserDto.getEmail() + " уже существует",
                    exception.getMessage());

            verify(userRepository).existsByEmail(testUserDto.getEmail());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user name and email successfully")
        void shouldUpdateUserNameAndEmail_WhenDataIsValid() {

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);

            UserDto result = userService.updateUser(1L, testUserUpdateDto);

            assertNotNull(result);
            assertEquals("Updated User", result.getName());
            assertEquals("updated@example.com", result.getEmail());
            assertEquals(1L, result.getId());

            verify(userRepository).findById(1L);
            verify(userRepository).existsByEmail("updated@example.com");
        }

        @Test
        @DisplayName("Should update only name when email is null")
        void shouldUpdateOnlyName_WhenEmailIsNull() {

            testUserUpdateDto.setEmail(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserDto result = userService.updateUser(1L, testUserUpdateDto);

            assertNotNull(result);
            assertEquals("Updated User", result.getName());
            assertEquals("test@example.com", result.getEmail()); // Original email preserved

            verify(userRepository).findById(1L);
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("Should update only email when name is null")
        void shouldUpdateOnlyEmail_WhenNameIsNull() {

            testUserUpdateDto.setName(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);

            UserDto result = userService.updateUser(1L, testUserUpdateDto);

            assertNotNull(result);
            assertEquals("Test User", result.getName()); // Original name preserved
            assertEquals("updated@example.com", result.getEmail());

            verify(userRepository).findById(1L);
            verify(userRepository).existsByEmail("updated@example.com");
        }

        @Test
        @DisplayName("Should not check email existence when email is unchanged")
        void shouldNotCheckEmailExistence_WhenEmailIsUnchanged() {

            testUserUpdateDto.setEmail("test@example.com"); // Same as existing
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserDto result = userService.updateUser(1L, testUserUpdateDto);

            assertNotNull(result);
            assertEquals("Updated User", result.getName());
            assertEquals("test@example.com", result.getEmail());

            verify(userRepository).findById(1L);
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowNotFoundException_WhenUserDoesNotExist() {

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.updateUser(999L, testUserUpdateDto));

            assertTrue(exception.getMessage().contains("Пользователь"));
            assertTrue(exception.getMessage().contains("999"));

            verify(userRepository).findById(999L);
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw ConflictException when new email already exists")
        void shouldThrowConflictException_WhenNewEmailAlreadyExists() {

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> userService.updateUser(1L, testUserUpdateDto));

            assertEquals("Email updated@example.com уже используется", exception.getMessage());

            verify(userRepository).findById(1L);
            verify(userRepository).existsByEmail("updated@example.com");
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should return user by ID when user exists")
        void shouldReturnUser_WhenUserExists() {

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserDto result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(testUser.getId(), result.getId());
            assertEquals(testUser.getName(), result.getName());
            assertEquals(testUser.getEmail(), result.getEmail());

            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowNotFoundException_WhenUserDoesNotExist() {

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.getUserById(999L));

            assertTrue(exception.getMessage().contains("Пользователь"));
            assertTrue(exception.getMessage().contains("999"));

            verify(userRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users when users exist")
        void shouldReturnAllUsers_WhenUsersExist() {

            User user2 = new User();
            user2.setId(2L);
            user2.setName("User 2");
            user2.setEmail("user2@example.com");

            List<User> users = Arrays.asList(testUser, user2);
            when(userRepository.findAll()).thenReturn(users);

            List<UserDto> result = userService.getAllUsers();

            assertNotNull(result);
            assertEquals(2, result.size());

            UserDto firstUser = result.get(0);
            assertEquals(testUser.getId(), firstUser.getId());
            assertEquals(testUser.getName(), firstUser.getName());
            assertEquals(testUser.getEmail(), firstUser.getEmail());

            UserDto secondUser = result.get(1);
            assertEquals(user2.getId(), secondUser.getId());
            assertEquals(user2.getName(), secondUser.getName());
            assertEquals(user2.getEmail(), secondUser.getEmail());

            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyList_WhenNoUsersExist() {

            when(userRepository.findAll()).thenReturn(Arrays.asList());

            List<UserDto> result = userService.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully when user exists")
        void shouldDeleteUser_WhenUserExists() {

            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            assertDoesNotThrow(() -> userService.deleteUser(1L));

            verify(userRepository).existsById(1L);
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user does not exist")
        void shouldThrowNotFoundException_WhenUserDoesNotExist() {

            when(userRepository.existsById(999L)).thenReturn(false);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.deleteUser(999L));

            assertTrue(exception.getMessage().contains("Пользователь"));
            assertTrue(exception.getMessage().contains("999"));

            verify(userRepository).existsById(999L);
            verify(userRepository, never()).deleteById(anyLong());
        }
    }
}
