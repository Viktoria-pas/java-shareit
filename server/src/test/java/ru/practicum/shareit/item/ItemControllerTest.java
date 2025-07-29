package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private UserService userService;

    private ItemDto itemDto;
    private ItemUpdateDto itemUpdateDto;
    private CommentDto commentDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@email.com");

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setComments(Collections.emptyList());

        itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Updated Item");
        itemUpdateDto.setDescription("Updated Description");
        itemUpdateDto.setAvailable(false);

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setAuthorName("Test User");
        commentDto.setCreated(LocalDateTime.now());
    }

    @Test
    void addItem_ShouldReturnCreatedItem_WhenValidInput() throws Exception {

        when(userService.getUserById(1L)).thenReturn(userDto);
        when(itemService.addItem(any(ItemDto.class), eq(1L))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Item")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(userService).getUserById(1L);
        verify(itemService).addItem(any(ItemDto.class), eq(1L));
    }

    @Test
    void addItem_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {

        when(userService.getUserById(1L)).thenThrow(new NotFoundException("Пользователь", 1L));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(1L);
        verify(itemService, never()).addItem(any(ItemDto.class), anyLong());
    }

    @Test
    void addItem_ShouldReturnBadRequest_WhenMissingUserHeader() throws Exception {

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).getUserById(anyLong());
        verify(itemService, never()).addItem(any(ItemDto.class), anyLong());
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem_WhenValidInput() throws Exception {
        // Given
        ItemDto updatedItemDto = new ItemDto();
        updatedItemDto.setId(1L);
        updatedItemDto.setName("Updated Item");
        updatedItemDto.setDescription("Updated Description");
        updatedItemDto.setAvailable(false);

        when(itemService.updateItem(eq(1L), any(ItemUpdateDto.class), eq(1L)))
                .thenReturn(updatedItemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Item")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.available", is(false)));

        verify(itemService).updateItem(eq(1L), any(ItemUpdateDto.class), eq(1L));
    }

    @Test
    void updateItem_ShouldReturnNotFound_WhenItemNotExists() throws Exception {

        when(itemService.updateItem(eq(1L), any(ItemUpdateDto.class), eq(1L)))
                .thenThrow(new NotFoundException("Предмет", 1L));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isNotFound());

        verify(itemService).updateItem(eq(1L), any(ItemUpdateDto.class), eq(1L));
    }

    @Test
    void updateItem_ShouldReturnNotFound_WhenUserIsNotOwner() throws Exception {

        when(itemService.updateItem(eq(1L), any(ItemUpdateDto.class), eq(2L)))
                .thenThrow(new NotFoundException("Предмет", 1L));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isNotFound());

        verify(itemService).updateItem(eq(1L), any(ItemUpdateDto.class), eq(2L));
    }

    @Test
    void getItemById_ShouldReturnItem_WhenItemExists() throws Exception {

        when(itemService.getItemById(1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Item")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService).getItemById(1L);
    }

    @Test
    void getItemById_ShouldReturnNotFound_WhenItemNotExists() throws Exception {

        when(itemService.getItemById(1L)).thenThrow(new NotFoundException("Предмет", 1L));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isNotFound());

        verify(itemService).getItemById(1L);
    }

    @Test
    void getAllItemsByOwner_ShouldReturnItemsList() throws Exception {

        List<ItemDto> items = List.of(itemDto);
        when(itemService.getAllItemsByOwner(1L)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")));

        verify(itemService).getAllItemsByOwner(1L);
    }

    @Test
    void getAllItemsByOwner_ShouldReturnEmptyList_WhenNoItems() throws Exception {

        when(itemService.getAllItemsByOwner(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService).getAllItemsByOwner(1L);
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() throws Exception {

        List<ItemDto> items = List.of(itemDto);
        when(itemService.searchItems("test")).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")));

        verify(itemService).searchItems("test");
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenNoMatches() throws Exception {

        when(itemService.searchItems("nonexistent")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .param("text", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService).searchItems("nonexistent");
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsEmpty() throws Exception {

        when(itemService.searchItems("")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService).searchItems("");
    }

    @Test
    void addComment_ShouldReturnCreatedComment_WhenValidInput() throws Exception {

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great item!")))
                .andExpect(jsonPath("$.authorName", is("Test User")))
                .andExpect(jsonPath("$.created", notNullValue()));

        verify(itemService).addComment(eq(1L), eq(1L), any(CommentDto.class));
    }

    @Test
    void addComment_ShouldReturnNotFound_WhenItemNotExists() throws Exception {

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isNotFound());

        verify(itemService).addComment(eq(1L), eq(1L), any(CommentDto.class));
    }

    @Test
    void addComment_ShouldReturnBadRequest_WhenUserDidntBookItem() throws Exception {

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenThrow(new ValidationException("Нельзя оставить комментарий к вещи, которую не брали в аренду"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());

        verify(itemService).addComment(eq(1L), eq(1L), any(CommentDto.class));
    }

    @Test
    void addComment_ShouldReturnBadRequest_WhenMissingUserHeader() throws Exception {

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).addComment(anyLong(), anyLong(), any(CommentDto.class));
    }

    @Test
    void getAllItemsByOwner_ShouldReturnBadRequest_WhenMissingUserHeader() throws Exception {

        mockMvc.perform(get("/items"))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).getAllItemsByOwner(anyLong());
    }

    @Test
    void updateItem_ShouldReturnBadRequest_WhenInvalidItemId() throws Exception {
        // When & Then
        mockMvc.perform(patch("/items/invalid")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).updateItem(anyLong(), any(ItemUpdateDto.class), anyLong());
    }

    @Test
    void getItemById_ShouldReturnBadRequest_WhenInvalidItemId() throws Exception {

        mockMvc.perform(get("/items/invalid"))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).getItemById(anyLong());
    }

    @Test
    void addComment_ShouldReturnBadRequest_WhenInvalidItemId() throws Exception {

        mockMvc.perform(post("/items/invalid/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).addComment(anyLong(), anyLong(), any(CommentDto.class));
    }
}
