package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final Long userId = 1L;
    private final Long itemId = 1L;

    @Test
    void addItem_ValidInput_ReturnsOk() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);

        when(itemClient.addItem(eq(userId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_WithRequestId_ReturnsOk() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, 123L);

        when(itemClient.addItem(eq(userId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_NullName_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_EmptyName_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("", "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_BlankName_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("   ", "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_NullDescription_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", null, true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_EmptyDescription_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_BlankDescription_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "   ", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_NullAvailable_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", null, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_TooLongName_ReturnsBadRequest() throws Exception {
        String longName = "a".repeat(256); // 256 символов
        ItemDto itemDto = new ItemDto(longName, "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_TooLongDescription_ReturnsBadRequest() throws Exception {
        String longDescription = "a".repeat(1001); // 1001 символ
        ItemDto itemDto = new ItemDto("Test Item", longDescription, true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_MaxLengthName_ReturnsOk() throws Exception {
        String maxLengthName = "a".repeat(255); // 255 символов
        ItemDto itemDto = new ItemDto(maxLengthName, "Test Description", true, null);

        when(itemClient.addItem(eq(userId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_MaxLengthDescription_ReturnsOk() throws Exception {
        String maxLengthDescription = "a".repeat(1000); // 1000 символов
        ItemDto itemDto = new ItemDto("Test Item", maxLengthDescription, true, null);

        when(itemClient.addItem(eq(userId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_MissingUserIdHeader_ReturnsInternalServerError() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addItem_NegativeUserId_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_ZeroUserId_ReturnsBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    // Тесты для updateItem
    @Test
    void updateItem_ValidInput_ReturnsOk() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", "Updated Description", false);

        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_OnlyName_ReturnsOk() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", null, null);

        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_OnlyDescription_ReturnsOk() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(null, "Updated Description", null);

        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_OnlyAvailable_ReturnsOk() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(null, null, false);

        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_EmptyName_ReturnsBadRequest() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("", "Updated Description", true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_BlankName_ReturnsBadRequest() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("   ", "Updated Description", true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_EmptyDescription_ReturnsBadRequest() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", "", true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_BlankDescription_ReturnsBadRequest() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", "   ", true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_TooLongName_ReturnsBadRequest() throws Exception {
        String longName = "a".repeat(256);
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(longName, "Updated Description", true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_TooLongDescription_ReturnsBadRequest() throws Exception {
        String longDescription = "a".repeat(1001);
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", longDescription, true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_NegativeItemId_ReturnsBadRequest() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", "Updated Description", true);

        mockMvc.perform(patch("/items/{itemId}", -1L)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_ZeroItemId_ReturnsBadRequest() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", "Updated Description", true);

        mockMvc.perform(patch("/items/{itemId}", 0L)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    // Тесты для getItemById
    @Test
    void getItemById_ValidId_ReturnsOk() throws Exception {
        when(itemClient.getItemById(eq(itemId)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_NegativeId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/{itemId}", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_ZeroId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/{itemId}", 0L))
                .andExpect(status().isBadRequest());
    }

    // Тесты для getAllItemsByOwner
    @Test
    void getAllItemsByOwner_ValidUserId_ReturnsOk() throws Exception {
        when(itemClient.getAllItemsByOwner(eq(userId)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsByOwner_NegativeUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllItemsByOwner_ZeroUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 0L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllItemsByOwner_MissingUserIdHeader_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isInternalServerError());
    }

    // Тесты для searchItems
    @Test
    void searchItems_ValidText_ReturnsOk() throws Exception {
        String searchText = "test";

        when(itemClient.searchItems(eq(searchText)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_EmptyText_ReturnsOk() throws Exception {
        String searchText = "";

        when(itemClient.searchItems(eq(searchText)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_BlankText_ReturnsOk() throws Exception {
        String searchText = "   ";

        when(itemClient.searchItems(eq(searchText)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_NullText_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void searchItems_LongText_ReturnsOk() throws Exception {
        String longText = "a".repeat(1000);

        when(itemClient.searchItems(eq(longText)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", longText))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_ValidInput_ReturnsOk() throws Exception {
        CommentDto commentDto = new CommentDto("Great item!");

        when(itemClient.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_NullText_ReturnsBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto(null);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_EmptyText_ReturnsBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto("");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_BlankText_ReturnsBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto("   ");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_TooLongText_ReturnsBadRequest() throws Exception {
        String longText = "a".repeat(1001);
        CommentDto commentDto = new CommentDto(longText);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_MaxLengthText_ReturnsOk() throws Exception {
        String maxLengthText = "a".repeat(1000);
        CommentDto commentDto = new CommentDto(maxLengthText);

        when(itemClient.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_NegativeItemId_ReturnsBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto("Great item!");

        mockMvc.perform(post("/items/{itemId}/comment", -1L)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_ZeroItemId_ReturnsBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto("Great item!");

        mockMvc.perform(post("/items/{itemId}/comment", 0L)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_NegativeUserId_ReturnsBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto("Great item!");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_ZeroUserId_ReturnsBadRequest() throws Exception {
        CommentDto commentDto = new CommentDto("Great item!");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_MissingUserIdHeader_ReturnsInternalServerError() throws Exception {
        CommentDto commentDto = new CommentDto("Great item!");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isInternalServerError());
    }
}
