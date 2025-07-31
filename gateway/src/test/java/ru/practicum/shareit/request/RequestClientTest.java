package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> entityCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> parametersCaptor;

    private RequestClient requestClient;

    private static final String SERVER_URL = "http://localhost:9090";
    private static final String API_PREFIX = "/requests";

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        requestClient = new RequestClient(SERVER_URL, restTemplateBuilder);
    }

    @Test
    void createRequest_ShouldCallPostWithCorrectParameters() {
        ItemRequestDto requestDto = new ItemRequestDto("Need item");
        long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = requestClient.createRequest(userId, requestDto);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Object.class)
        );

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getBody()).isEqualTo(requestDto);
        assertThat(capturedEntity.getHeaders().getFirst("X-Sharer-User-Id")).isEqualTo(String.valueOf(userId));
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getUserRequests_ShouldCallGetWithUserIdHeader() {
        long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = requestClient.getUserRequests(userId);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"))
                .isEqualTo(String.valueOf(userId));
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getAllRequests_ShouldCallGetWithParameters() {
        long userId = 1L;
        int from = 0;
        int size = 10;
        Map<String, Object> expectedParams = Map.of("from", from, "size", size);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(expectedParams)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = requestClient.getAllRequests(userId, from, size);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(expectedParams)
        );

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getRequestById_ShouldCallGetWithCorrectPath() {
        long userId = 1L;
        long requestId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX + "/" + requestId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = requestClient.getRequestById(userId, requestId);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX + "/" + requestId),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"))
                .isEqualTo(String.valueOf(userId));
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void constructor_ShouldInitializeWithCorrectBaseUrl() {
        verify(restTemplateBuilder).build();
    }
}
