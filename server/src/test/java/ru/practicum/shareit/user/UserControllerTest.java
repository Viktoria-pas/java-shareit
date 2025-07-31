package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto testUserDto;
    private UserUpdateDto testUserUpdateDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setName("Test User");
        testUserDto.setEmail("test@example.com");

        testUserUpdateDto = new UserUpdateDto();
        testUserUpdateDto.setName("Updated User");
        testUserUpdateDto.setEmail("updated@example.com");
    }

    @Nested
    @DisplayName("Create User Endpoint Tests")
    class CreateUserEndpointTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() throws Exception {

            UserDto inputDto = new UserDto();
            inputDto.setName("Test User");
            inputDto.setEmail("test@example.com");

            when(userService.createUser(any(UserDto.class))).thenReturn(testUserDto);

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Test User")))
                    .andExpect(jsonPath("$.email", is("test@example.com")));
        }

        @Test
        @DisplayName("Should return conflict when email already exists")
        void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

            UserDto inputDto = new UserDto();
            inputDto.setName("Test User");
            inputDto.setEmail("test@example.com");

            when(userService.createUser(any(UserDto.class)))
                    .thenThrow(new ConflictException("Пользователь с email test@example.com уже существует"));

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error", is("Пользователь с email test@example.com уже существует")));
        }

        @Test
        @DisplayName("Should handle internal server error")
        void shouldHandleInternalServerError() throws Exception {

            UserDto inputDto = new UserDto();
            inputDto.setName("Test User");
            inputDto.setEmail("test@example.com");

            when(userService.createUser(any(UserDto.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error", is("Внутренняя ошибка сервера")))
                    .andExpect(jsonPath("$.message", is("Произошла непредвиденная ошибка")));
        }
    }

    @Nested
    @DisplayName("Update User Endpoint Tests")
    class UpdateUserEndpointTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() throws Exception {

            UserDto updatedUserDto = new UserDto();
            updatedUserDto.setId(1L);
            updatedUserDto.setName("Updated User");
            updatedUserDto.setEmail("updated@example.com");

            when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedUserDto);

            mockMvc.perform(patch("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Updated User")))
                    .andExpect(jsonPath("$.email", is("updated@example.com")));
        }

        @Test
        @DisplayName("Should return not found when user does not exist")
        void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

            when(userService.updateUser(eq(999L), any(UserUpdateDto.class)))
                    .thenThrow(new NotFoundException("Пользователь", 999L));

            mockMvc.perform(patch("/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Объект не найден")));
        }

        @Test
        @DisplayName("Should return conflict when email already exists")
        void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

            when(userService.updateUser(eq(1L), any(UserUpdateDto.class)))
                    .thenThrow(new ConflictException("Email updated@example.com уже используется"));

            mockMvc.perform(patch("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error", is("Email updated@example.com уже используется")));
        }

        @Test
        @DisplayName("Should update user with partial data")
        void shouldUpdateUserWithPartialData() throws Exception {

            UserUpdateDto partialUpdate = new UserUpdateDto();
            partialUpdate.setName("Only Name Updated");

            UserDto updatedUserDto = new UserDto();
            updatedUserDto.setId(1L);
            updatedUserDto.setName("Only Name Updated");
            updatedUserDto.setEmail("test@example.com"); // Email unchanged

            when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedUserDto);

            mockMvc.perform(patch("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(partialUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Only Name Updated")))
                    .andExpect(jsonPath("$.email", is("test@example.com")));
        }
    }

    @Nested
    @DisplayName("Get User By ID Endpoint Tests")
    class GetUserByIdEndpointTests {

        @Test
        @DisplayName("Should return user when user exists")
        void shouldReturnUserWhenUserExists() throws Exception {

            when(userService.getUserById(1L)).thenReturn(testUserDto);

            mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Test User")))
                    .andExpect(jsonPath("$.email", is("test@example.com")));
        }

        @Test
        @DisplayName("Should return not found when user does not exist")
        void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

            when(userService.getUserById(999L))
                    .thenThrow(new NotFoundException("Пользователь", 999L));

            mockMvc.perform(get("/users/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Объект не найден")));
        }
    }

    @Nested
    @DisplayName("Get All Users Endpoint Tests")
    class GetAllUsersEndpointTests {

        @Test
        @DisplayName("Should return all users when users exist")
        void shouldReturnAllUsersWhenUsersExist() throws Exception {

            UserDto secondUser = new UserDto();
            secondUser.setId(2L);
            secondUser.setName("Second User");
            secondUser.setEmail("second@example.com");

            List<UserDto> users = Arrays.asList(testUserDto, secondUser);
            when(userService.getAllUsers()).thenReturn(users);

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("Test User")))
                    .andExpect(jsonPath("$[0].email", is("test@example.com")))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].name", is("Second User")))
                    .andExpect(jsonPath("$[1].email", is("second@example.com")));
        }

        @Test
        @DisplayName("Should return empty array when no users exist")
        void shouldReturnEmptyArrayWhenNoUsersExist() throws Exception {

            when(userService.getAllUsers()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {

            when(userService.getAllUsers())
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/users"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error", is("Внутренняя ошибка сервера")))
                    .andExpect(jsonPath("$.message", is("Произошла непредвиденная ошибка")));
        }
    }

    @Nested
    @DisplayName("Delete User Endpoint Tests")
    class DeleteUserEndpointTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {

            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return not found when user does not exist")
        void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

            doThrow(new NotFoundException("Пользователь", 999L))
                    .when(userService).deleteUser(999L);

            mockMvc.perform(delete("/users/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Объект не найден")));
        }

        @Test
        @DisplayName("Should handle service exception during deletion")
        void shouldHandleServiceExceptionDuringDeletion() throws Exception {

            doThrow(new RuntimeException("Database connection failed"))
                    .when(userService).deleteUser(1L);

            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error", is("Внутренняя ошибка сервера")))
                    .andExpect(jsonPath("$.message", is("Произошла непредвиденная ошибка")));
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should handle malformed JSON")
        void shouldHandleMalformedJson() throws Exception {

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws Exception {

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {

            UserDto inputDto = new UserDto();
            inputDto.setName("Test User");
            inputDto.setEmail("test@example.com");

            mockMvc.perform(post("/users")
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Path Variable Tests")
    class PathVariableTests {

        @Test
        @DisplayName("Should handle negative user ID")
        void shouldHandleNegativeUserId() throws Exception {

            when(userService.getUserById(-1L))
                    .thenThrow(new NotFoundException("Пользователь", -1L));

            mockMvc.perform(get("/users/-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle zero user ID")
        void shouldHandleZeroUserId() throws Exception {

            when(userService.getUserById(0L))
                    .thenThrow(new NotFoundException("Пользователь", 0L));

            mockMvc.perform(get("/users/0"))
                    .andExpect(status().isNotFound());
        }
    }
}