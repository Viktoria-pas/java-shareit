package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@TestPropertySource(properties = {
        "shareit-server.url=http://localhost:8080"
})
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void addItem_FullWorkflow_Success() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, 123L);
        Long userId = 1L;
        Map<String, Object> createdItem = Map.of(
                "id", 1L,
                "name", "Test Item",
                "description", "Test Description",
                "available", true,
                "requestId", 123L
        );

        when(itemClient.addItem(eq(userId), any(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(createdItem, HttpStatus.CREATED));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.requestId").value(123L));
    }

    @Test
    void updateItem_FullWorkflow_Success() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", "Updated Description", false);
        Long userId = 1L;
        Long itemId = 1L;
        Map<String, Object> updatedItem = Map.of(
                "id", itemId,
                "name", "Updated Item",
                "description", "Updated Description",
                "available", false
        );

        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenReturn(new ResponseEntity<>(updatedItem, HttpStatus.OK));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getItemById_ExistingItem_Success() throws Exception {
        Long itemId = 1L;
        Map<String, Object> item = Map.of(
                "id", itemId,
                "name", "Test Item",
                "description", "Test Description",
                "available", true,
                "comments", Collections.emptyList()
        );

        when(itemClient.getItemById(eq(itemId)))
                .thenReturn(new ResponseEntity<>(item, HttpStatus.OK));

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments").isEmpty());
    }

    @Test
    void getAllItemsByOwner_WithItems_Success() throws Exception {
        Long userId = 1L;
        List<Map<String, Object>> items = List.of(
                Map.of("id", 1L, "name", "Item 1", "description", "Description 1", "available", true),
                Map.of("id", 2L, "name", "Item 2", "description", "Description 2", "available", false)
        );

        when(itemClient.getAllItemsByOwner(eq(userId)))
                .thenReturn(new ResponseEntity<>(items, HttpStatus.OK));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Item 2"));
    }

    @Test
    void getAllItemsByOwner_EmptyList_Success() throws Exception {
        Long userId = 1L;
        List<Map<String, Object>> items = Collections.emptyList();

        when(itemClient.getAllItemsByOwner(eq(userId)))
                .thenReturn(new ResponseEntity<>(items, HttpStatus.OK));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchItems_WithResults_Success() throws Exception {
        String searchText = "test";
        List<Map<String, Object>> searchResults = List.of(
                Map.of("id", 1L, "name", "Test Item", "description", "Test Description", "available", true)
        );

        when(itemClient.searchItems(eq(searchText)))
                .thenReturn(new ResponseEntity<>(searchResults, HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItems_EmptyResults_Success() throws Exception {
        String searchText = "nonexistent";
        List<Map<String, Object>> searchResults = Collections.emptyList();

        when(itemClient.searchItems(eq(searchText)))
                .thenReturn(new ResponseEntity<>(searchResults, HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addComment_FullWorkflow_Success() throws Exception {
        CommentDto commentDto = new CommentDto("Great item!");
        Long userId = 1L;
        Long itemId = 1L;
        Map<String, Object> createdComment = Map.of(
                "id", 1L,
                "text", "Great item!",
                "authorName", "User Name",
                "created", "2024-01-01T10:00:00"
        );

        when(itemClient.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(createdComment, HttpStatus.OK));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("User Name"));
    }

    @Test
    void errorHandling_ValidationErrors_ReturnsBadRequest() throws Exception {
        ItemDto invalidItemDto = new ItemDto("", "", null, null);
        Long userId = 1L;

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void errorHandling_MissingHeaders_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addItem_InvalidData_ReturnsBadRequest() throws Exception {
        ItemDto invalidItemDto = new ItemDto(null, null, null, null);
        Long userId = 1L;

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_InvalidData_ReturnsBadRequest() throws Exception {
        CommentDto invalidCommentDto = new CommentDto("");
        Long userId = 1L;
        Long itemId = 1L;

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_MissingUserId_ReturnsBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto("Great item!");
        Long itemId = 1L;

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isInternalServerError());
    }
}
