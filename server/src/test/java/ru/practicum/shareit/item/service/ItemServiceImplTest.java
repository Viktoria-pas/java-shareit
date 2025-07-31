package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private UserDto ownerDto;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("John Doe");
        owner.setEmail("john@email.com");

        ownerDto = new UserDto();
        ownerDto.setId(1L);
        ownerDto.setName("John Doe");
        ownerDto.setEmail("john@email.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setComments(Collections.emptyList());

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setComments(Collections.emptyList());

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthor(owner);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setAuthorName("John Doe");
        commentDto.setCreated(LocalDateTime.now());
    }

    @Test
    void addItem_ShouldReturnItemDto_WhenValidInput() {

        when(userService.getUserById(1L)).thenReturn(ownerDto);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.addItem(itemDto, 1L);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());

        verify(userService).getUserById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void addItem_ShouldSetRequest_WhenRequestIdProvided() {

        itemDto.setRequestId(2L);
        when(userService.getUserById(1L)).thenReturn(ownerDto);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.addItem(itemDto, 1L);

        assertNotNull(result);
        verify(itemRepository).save(argThat(savedItem ->
                savedItem.getRequest() != null && savedItem.getRequest().getId().equals(2L)));
    }

    @Test
    void updateItem_ShouldUpdateItem_WhenOwnerIsCorrect() {

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(1L, updateDto, 1L);

        assertNotNull(result);
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_ShouldThrowNotFoundException_WhenItemNotFound() {

        ItemUpdateDto updateDto = new ItemUpdateDto();
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, updateDto, 1L));

        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_ShouldThrowNotFoundException_WhenUserIsNotOwner() {

        ItemUpdateDto updateDto = new ItemUpdateDto();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, updateDto, 999L));

        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_ShouldUpdateOnlyProvidedFields() {

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Name");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        itemService.updateItem(1L, updateDto, 1L);

        verify(itemRepository).save(argThat(savedItem ->
                savedItem.getName().equals("Updated Name") &&
                        savedItem.getDescription().equals("Test Description") && // остался прежним
                        savedItem.getAvailable().equals(true) // остался прежним
        ));
    }

    @Test
    void getItemById_ShouldReturnItemDto_WhenItemExists() {

        List<Comment> comments = List.of(comment);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(comments);

        ItemDto result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());

        verify(itemRepository).findById(1L);
        verify(commentRepository).findByItemIdOrderByCreatedDesc(1L);
    }

    @Test
    void getItemById_ShouldThrowNotFoundException_WhenItemNotFound() {

        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.getItemById(1L));

        verify(itemRepository).findById(1L);
        verify(commentRepository, never()).findByItemIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllItemsByOwner_ShouldReturnItemsList() {

        List<Item> items = List.of(item);
        when(itemRepository.findByOwnerId(1L)).thenReturn(items);

        List<ItemDto> result = itemService.getAllItemsByOwner(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getName(), result.get(0).getName());

        verify(itemRepository).findByOwnerId(1L);
    }

    @Test
    void getAllItemsByOwner_ShouldReturnEmptyList_WhenNoItems() {

        when(itemRepository.findByOwnerId(1L)).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getAllItemsByOwner(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(itemRepository).findByOwnerId(1L);
    }

    @Test
    void searchItems_ShouldReturnItemsList_WhenTextProvided() {

        String searchText = "test";
        List<Item> items = List.of(item);
        when(itemRepository.findAvailableItemsWithText(searchText.toLowerCase())).thenReturn(items);

        List<ItemDto> result = itemService.searchItems(searchText);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getName(), result.get(0).getName());

        verify(itemRepository).findAvailableItemsWithText(searchText.toLowerCase());
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsNull() {

        List<ItemDto> result = itemService.searchItems(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(itemRepository, never()).findAvailableItemsWithText(anyString());
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsBlank() {

        List<ItemDto> result = itemService.searchItems("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(itemRepository, never()).findAvailableItemsWithText(anyString());
    }

    @Test
    void addComment_ShouldReturnCommentDto_WhenValidInput() {

        Long userId = 1L;
        Long itemId = 1L;

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(userService.getUserById(userId)).thenReturn(ownerDto);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingService.findByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(userId, itemId, commentDto);

        assertNotNull(result);
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getAuthor().getName(), result.getAuthorName());

        verify(userService).getUserById(userId);
        verify(itemRepository).findById(itemId);
        verify(bookingService).findByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_ShouldThrowNotFoundException_WhenItemNotFound() {

        Long userId = 1L;
        Long itemId = 1L;

        when(userService.getUserById(userId)).thenReturn(ownerDto);
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(userId, itemId, commentDto));

        verify(userService).getUserById(userId);
        verify(itemRepository).findById(itemId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

//    @Test
//    void addComment_ShouldThrowValidationException_WhenUserDidntBookItem() {
//
//        Long userId = 1L;
//        Long itemId = 1L;
//
//        when(userService.getUserById(userId)).thenReturn(ownerDto);
//        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
//        when(bookingService.findByBookerIdAndItemIdAndStatusAndEndBefore(
//                eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
//                .thenReturn(Collections.emptyList());
//
//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> itemService.addComment(userId, itemId, commentDto));
//
//        assertEquals("Нельзя оставить комментарий к вещи, которую не брали в аренду",
//                exception.getMessage());
//
//        verify(userService).getUserById(userId);
//        verify(itemRepository).findById(itemId);
//        verify(bookingService).findByBookerIdAndItemIdAndStatusAndEndBefore(
//                eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
//        verify(commentRepository, never()).save(any(Comment.class));
//    }

    @Test
    void addComment_ShouldSetCreationTime() {

        Long userId = 1L;
        Long itemId = 1L;

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(userService.getUserById(userId)).thenReturn(ownerDto);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingService.findByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        itemService.addComment(userId, itemId, commentDto);

        verify(commentRepository).save(argThat(savedComment ->
                savedComment.getCreated() != null &&
                        savedComment.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)) &&
                        savedComment.getCreated().isAfter(LocalDateTime.now().minusSeconds(1))
        ));
    }
}
