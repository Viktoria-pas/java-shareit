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

    @Test
    void addItem_ValidInput_ReturnsOk() throws Exception {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);
        Long userId = 1L;

        when(itemClient.addItem(eq(userId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_BlankName_ReturnsBadRequest() throws Exception {

        ItemDto itemDto = new ItemDto("", "Test Description", true, null);
        Long userId = 1L;

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_BlankDescription_ReturnsBadRequest() throws Exception {

        ItemDto itemDto = new ItemDto("Test Item", "", true, null);
        Long userId = 1L;

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_NullAvailable_ReturnsBadRequest() throws Exception {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", null, null);
        Long userId = 1L;

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_MissingUserIdHeader_ReturnsBadRequest() throws Exception {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_ValidInput_ReturnsOk() throws Exception {

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", "Updated Description", false);
        Long userId = 1L;
        Long itemId = 1L;

        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_ValidId_ReturnsOk() throws Exception {

        Long itemId = 1L;

        when(itemClient.getItemById(eq(itemId)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsByOwner_ValidUserId_ReturnsOk() throws Exception {

        Long userId = 1L;

        when(itemClient.getAllItemsByOwner(eq(userId)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk());
    }

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
    void addComment_ValidInput_ReturnsOk() throws Exception {

        CommentDto commentDto = new CommentDto("Great item!");
        Long userId = 1L;
        Long itemId = 1L;

        when(itemClient.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_BlankText_ReturnsBadRequest() throws Exception {

        CommentDto commentDto = new CommentDto("");
        Long userId = 1L;
        Long itemId = 1L;

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }
}
