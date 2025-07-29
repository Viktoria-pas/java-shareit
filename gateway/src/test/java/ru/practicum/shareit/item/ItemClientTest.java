package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private ItemClient itemClient;

    private static final String SERVER_URL = "http://localhost:9090";
    private static final String API_PREFIX = "/items";

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        itemClient = new ItemClient(SERVER_URL, restTemplateBuilder);
    }

    @Test
    void addItem_ValidInput_ReturnsResponseEntity() {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);
        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.addItem(userId, itemDto);

        assertEquals(expectedResponse, result);
    }

    @Test
    void getItemById_ValidId_ReturnsResponseEntity() {

        Long itemId = 1L;
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX + "/" + itemId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.getItemById(itemId);

        assertEquals(expectedResponse, result);
    }

    @Test
    void getAllItemsByOwner_ValidUserId_ReturnsResponseEntity() {

        Long userId = 1L;
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.getAllItemsByOwner(userId);

        assertEquals(expectedResponse, result);
    }

    @Test
    void addComment_ValidInput_ReturnsResponseEntity() {

        CommentDto commentDto = new CommentDto("Great item!");
        Long userId = 1L;
        Long itemId = 1L;
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(SERVER_URL + API_PREFIX + "/" + itemId + "/comment"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> result = itemClient.addComment(userId, itemId, commentDto);

        assertEquals(expectedResponse, result);
    }
}
