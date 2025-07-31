package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> entityCaptor;

    private UserClient userClient;

    private static final String SERVER_URL = "http://localhost:9090";
    private static final String API_PREFIX = "/users";

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        userClient = new UserClient(SERVER_URL, restTemplateBuilder);
    }

    @Test
    void createUser_ShouldCallPostWithCorrectParameters() {
        UserDto userDto = new UserDto(1L,"User Name", "user@email.com");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = userClient.createUser(userDto);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Object.class)
        );

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getBody()).isEqualTo(userDto);
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getUserById_ShouldCallGetWithCorrectPath() {
        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX + "/" + userId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = userClient.getUserById(userId);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX + "/" + userId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        );

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getAllUsers_ShouldCallGetWithCorrectPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = userClient.getAllUsers();

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        );

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void updateUser_ShouldCallPatchWithCorrectParameters() {
        Long userId = 1L;
        UserUpdateDto userDto = new UserUpdateDto("updated@email.com", "Updated Name");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX + "/" + userId),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = userClient.updateUser(userId, userDto);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX + "/" + userId),
                eq(HttpMethod.PATCH),
                entityCaptor.capture(),
                eq(Object.class)
        );

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getBody()).isEqualTo(userDto);
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void deleteUser_ShouldCallDeleteWithCorrectPath() {
        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX + "/" + userId),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = userClient.deleteUser(userId);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX + "/" + userId),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
        );

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void constructor_ShouldInitializeWithCorrectBaseUrl() {
        verify(restTemplateBuilder).build();
        // Можно добавить проверку через reflection при необходимости
    }
}
