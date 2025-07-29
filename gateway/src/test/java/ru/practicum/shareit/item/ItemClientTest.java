package ru.practicum.shareit.item;

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
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> entityCaptor;

    private ItemClient itemClient;

    private static final String SERVER_URL = "http://localhost:9090";
    private static final String API_PREFIX = "/items";

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        itemClient = new ItemClient(SERVER_URL, restTemplateBuilder);
    }

    @Test
    void addItem_ShouldCallPostWithCorrectParameters() {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);
        long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.addItem(userId, itemDto);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Object.class)
        );

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertEquals(itemDto, capturedEntity.getBody());
        assertEquals(userId, Long.parseLong(capturedEntity.getHeaders().getFirst("X-Sharer-User-Id")));
        assertEquals(expectedResponse, result);
    }

    @Test
    void updateItem_ShouldCallPatchWithCorrectParameters() {
        ItemUpdateDto itemDto = new ItemUpdateDto("Updated Item", "Updated Desc", true);
        long userId = 1L;
        long itemId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.updateItem(userId, itemId, itemDto);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.PATCH),
                entityCaptor.capture(),
                eq(Object.class)
        );

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertEquals(itemDto, capturedEntity.getBody());
        assertEquals(userId, Long.parseLong(capturedEntity.getHeaders().getFirst("X-Sharer-User-Id")));
        assertEquals(expectedResponse, result);
    }

    @Test
    void getItemById_ShouldCallGetWithCorrectUrl() {
        long itemId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX + "/" + itemId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.getItemById(itemId);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX + "/" + itemId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        );

        assertEquals(expectedResponse, result);
    }

    @Test
    void getAllItemsByOwner_ShouldCallGetWithUserIdHeader() {
        long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.getAllItemsByOwner(userId);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(Object.class)
        );

        assertEquals(userId, Long.parseLong(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id")));
        assertEquals(expectedResponse, result);
    }

    @Test
    void searchItems_ShouldCallGetWithTextParameter() {
        String searchText = "test";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("text", searchText))
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.searchItems(searchText);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("text", searchText))
        );

        assertEquals(expectedResponse, result);
    }

    @Test
    void addComment_ShouldCallPostWithCorrectParameters() {
        CommentDto commentDto = new CommentDto("Great item!");
        long userId = 1L;
        long itemId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX + "/" + itemId + "/comment"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.addComment(userId, itemId, commentDto);

        verify(restTemplate).exchange(
                eq(SERVER_URL + API_PREFIX + "/" + itemId + "/comment"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Object.class)
        );

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertEquals(commentDto, capturedEntity.getBody());
        assertEquals(userId, Long.parseLong(capturedEntity.getHeaders().getFirst("X-Sharer-User-Id")));
        assertEquals(expectedResponse, result);
    }

    @Test
    void constructor_ShouldInitializeWithCorrectBaseUrl() {

        verify(restTemplateBuilder, times(1)).build();

        try {
            Field baseUrlField = BaseClient.class.getDeclaredField("baseUrl");
            baseUrlField.setAccessible(true);
            String actualBaseUrl = (String) baseUrlField.get(itemClient);

            assertEquals(SERVER_URL + API_PREFIX, actualBaseUrl);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
