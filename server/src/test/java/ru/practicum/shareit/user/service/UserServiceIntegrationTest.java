package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("UserService Integration Tests")
class UserServiceIntegrationTest {

    private final UserService userService;
    private final UserRepository userRepository;

    private UserDto testUserDto;
    private UserDto secondUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setName("Integration Test User");
        testUserDto.setEmail("integration@test.com");

        secondUserDto = new UserDto();
        secondUserDto.setName("Second User");
        secondUserDto.setEmail("second@test.com");
    }

    @Test
    @DisplayName("Should create user and persist to database")
    void shouldCreateUserAndPersistToDatabase() {
        // When
        UserDto createdUser = userService.createUser(testUserDto);

        // Then
        assertNotNull(createdUser.getId());
        assertEquals(testUserDto.getName(), createdUser.getName());
        assertEquals(testUserDto.getEmail(), createdUser.getEmail());

        // Verify persistence in database
        User persistedUser = userRepository.findById(createdUser.getId()).orElse(null);
        assertNotNull(persistedUser);
        assertEquals(createdUser.getName(), persistedUser.getName());
        assertEquals(createdUser.getEmail(), persistedUser.getEmail());
    }

    @Test
    @DisplayName("Should prevent creating users with duplicate email")
    void shouldPreventCreatingUsersWithDuplicateEmail() {
        // Given
        userService.createUser(testUserDto);

        UserDto duplicateEmailUser = new UserDto();
        duplicateEmailUser.setName("Different Name");
        duplicateEmailUser.setEmail(testUserDto.getEmail()); // Same email

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(duplicateEmailUser));

        assertTrue(exception.getMessage().contains(testUserDto.getEmail()));
        assertTrue(exception.getMessage().contains("уже существует"));

        // Verify only one user exists in database
        List<User> allUsers = userRepository.findAll();
        assertEquals(1, allUsers.size());
    }

    @Test
    @DisplayName("Should update user and persist changes to database")
    @Transactional
    void shouldUpdateUserAndPersistChangesToDatabase() {
        // Given
        UserDto createdUser = userService.createUser(testUserDto);

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated Integration User");
        updateDto.setEmail("updated@integration.com");

        // When
        UserDto updatedUser = userService.updateUser(createdUser.getId(), updateDto);

        // Then
        assertEquals(createdUser.getId(), updatedUser.getId());
        assertEquals(updateDto.getName(), updatedUser.getName());
        assertEquals(updateDto.getEmail(), updatedUser.getEmail());

        // Verify persistence in database
        User persistedUser = userRepository.findById(updatedUser.getId()).orElse(null);
        assertNotNull(persistedUser);
        assertEquals(updateDto.getName(), persistedUser.getName());
        assertEquals(updateDto.getEmail(), persistedUser.getEmail());
    }

    @Test
    @DisplayName("Should prevent updating to existing email")
    @Transactional
    void shouldPreventUpdatingToExistingEmail() {
        // Given
        UserDto firstUser = userService.createUser(testUserDto);
        UserDto secondUser = userService.createUser(secondUserDto);

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail(firstUser.getEmail()); // Try to use first user's email

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(secondUser.getId(), updateDto));

        assertTrue(exception.getMessage().contains(firstUser.getEmail()));
        assertTrue(exception.getMessage().contains("уже используется"));

        // Verify second user's email remains unchanged
        User persistedSecondUser = userRepository.findById(secondUser.getId()).orElse(null);
        assertNotNull(persistedSecondUser);
        assertEquals(secondUser.getEmail(), persistedSecondUser.getEmail());
    }

    @Test
    @DisplayName("Should retrieve user by ID from database")
    void shouldRetrieveUserByIdFromDatabase() {
        // Given
        UserDto createdUser = userService.createUser(testUserDto);

        // When
        UserDto retrievedUser = userService.getUserById(createdUser.getId());

        // Then
        assertNotNull(retrievedUser);
        assertEquals(createdUser.getId(), retrievedUser.getId());
        assertEquals(createdUser.getName(), retrievedUser.getName());
        assertEquals(createdUser.getEmail(), retrievedUser.getEmail());
    }

    @Test
    @DisplayName("Should throw NotFoundException for non-existent user")
    void shouldThrowNotFoundExceptionForNonExistentUser() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(nonExistentId));

        assertTrue(exception.getMessage().contains("Пользователь"));
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
    }

    @Test
    @DisplayName("Should retrieve all users from database")
    void shouldRetrieveAllUsersFromDatabase() {
        // Given
        UserDto firstUser = userService.createUser(testUserDto);
        UserDto secondUser = userService.createUser(secondUserDto);

        // When
        List<UserDto> allUsers = userService.getAllUsers();

        // Then
        assertNotNull(allUsers);
        assertEquals(2, allUsers.size());

        // Verify users are in the list
        assertTrue(allUsers.stream().anyMatch(u -> u.getId().equals(firstUser.getId())));
        assertTrue(allUsers.stream().anyMatch(u -> u.getId().equals(secondUser.getId())));
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void shouldReturnEmptyListWhenNoUsersExist() {
        // When
        List<UserDto> allUsers = userService.getAllUsers();

        // Then
        assertNotNull(allUsers);
        assertTrue(allUsers.isEmpty());
    }

    @Test
    @DisplayName("Should delete user from database")
    void shouldDeleteUserFromDatabase() {
        // Given
        UserDto createdUser = userService.createUser(testUserDto);
        Long userId = createdUser.getId();

        // Verify user exists
        assertTrue(userRepository.existsById(userId));

        // When
        userService.deleteUser(userId);

        // Then
        assertFalse(userRepository.existsById(userId));
        assertEquals(0, userRepository.findAll().size());
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent user")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentUser() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(nonExistentId));

        assertTrue(exception.getMessage().contains("Пользователь"));
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
    }

    @Test
    @DisplayName("Should handle complex user operations in sequence")
    @Transactional
    void shouldHandleComplexUserOperationsInSequence() {
        // Create first user
        UserDto firstUser = userService.createUser(testUserDto);
        assertNotNull(firstUser.getId());

        // Create second user
        UserDto secondUser = userService.createUser(secondUserDto);
        assertNotNull(secondUser.getId());

        // Verify both users exist
        List<UserDto> allUsers = userService.getAllUsers();
        assertEquals(2, allUsers.size());

        // Update first user
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated First User");
        updateDto.setEmail("updated.first@test.com");

        UserDto updatedFirstUser = userService.updateUser(firstUser.getId(), updateDto);
        assertEquals(updateDto.getName(), updatedFirstUser.getName());
        assertEquals(updateDto.getEmail(), updatedFirstUser.getEmail());

        // Delete second user
        userService.deleteUser(secondUser.getId());

        // Verify only first user remains
        allUsers = userService.getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(updatedFirstUser.getId(), allUsers.get(0).getId());

        // Verify persistence
        User persistedUser = userRepository.findById(updatedFirstUser.getId()).orElse(null);
        assertNotNull(persistedUser);
        assertEquals(updateDto.getName(), persistedUser.getName());
        assertEquals(updateDto.getEmail(), persistedUser.getEmail());
    }
}
