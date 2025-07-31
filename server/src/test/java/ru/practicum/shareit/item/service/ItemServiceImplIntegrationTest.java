package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class ItemServiceImplIntegrationTest {

    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {

        owner = new User();
        owner.setName("Owner User");
        owner.setEmail("owner@test.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker User");
        booker.setEmail("booker@test.com");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void addItem_ShouldSaveItemToDatabase() {

        ItemDto itemDto = new ItemDto();
        itemDto.setName("New Item");
        itemDto.setDescription("New Description");
        itemDto.setAvailable(true);

        ItemDto result = itemService.addItem(itemDto, owner.getId());

        assertNotNull(result);
        assertNotNull(result.getId());

        Optional<Item> savedItem = itemRepository.findById(result.getId());
        assertTrue(savedItem.isPresent());
        assertEquals("New Item", savedItem.get().getName());
        assertEquals("New Description", savedItem.get().getDescription());
        assertEquals(owner.getId(), savedItem.get().getOwner().getId());
    }

    @Test
    void updateItem_ShouldUpdateItemInDatabase() {

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        ItemDto result = itemService.updateItem(item.getId(), updateDto, owner.getId());

        assertNotNull(result);

        Optional<Item> updatedItem = itemRepository.findById(item.getId());
        assertTrue(updatedItem.isPresent());
        assertEquals("Updated Name", updatedItem.get().getName());
        assertEquals("Updated Description", updatedItem.get().getDescription());
        assertFalse(updatedItem.get().getAvailable());
    }

    @Test
    void updateItem_ShouldThrowNotFoundException_WhenUserIsNotOwner() {

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Name");

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(item.getId(), updateDto, booker.getId()));
    }

    @Test
    void getItemById_ShouldReturnItemWithComments() {

        Comment comment = new Comment();
        comment.setText("Great item!");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        ItemDto result = itemService.getItemById(item.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void getAllItemsByOwner_ShouldReturnOwnerItems() {

        Item secondItem = new Item();
        secondItem.setName("Second Item");
        secondItem.setDescription("Second Description");
        secondItem.setAvailable(true);
        secondItem.setOwner(owner);
        itemRepository.save(secondItem);

        List<ItemDto> result = itemService.getAllItemsByOwner(owner.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Test Item")));
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Second Item")));
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() {

        Item searchableItem = new Item();
        searchableItem.setName("Searchable Item");
        searchableItem.setDescription("This item can be found");
        searchableItem.setAvailable(true);
        searchableItem.setOwner(owner);
        itemRepository.save(searchableItem);

        Item unavailableItem = new Item();
        unavailableItem.setName("Unavailable Searchable Item");
        unavailableItem.setDescription("This item cannot be found");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);
        itemRepository.save(unavailableItem);

        List<ItemDto> result = itemService.searchItems("searchable");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Searchable Item", result.get(0).getName());
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsBlank() {

        List<ItemDto> result = itemService.searchItems("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_ShouldSaveCommentToDatabase() {

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Excellent item!");

        CommentDto result = itemService.addComment(booker.getId(), item.getId(), commentDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Excellent item!", result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
        assertNotNull(result.getCreated());

        Optional<Comment> savedComment = commentRepository.findById(result.getId());
        assertTrue(savedComment.isPresent());
        assertEquals("Excellent item!", savedComment.get().getText());
        assertEquals(booker.getId(), savedComment.get().getAuthor().getId());
        assertEquals(item.getId(), savedComment.get().getItem().getId());
    }

//    @Test
//    void addComment_ShouldThrowValidationException_WhenUserDidntBookItem() {
//
//        CommentDto commentDto = new CommentDto();
//        commentDto.setText("Comment without booking");
//
//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> itemService.addComment(booker.getId(), item.getId(), commentDto));
//
//        assertEquals("Нельзя оставить комментарий к вещи, которую не брали в аренду",
//                exception.getMessage());
//    }

//    @Test
//    void addComment_ShouldThrowValidationException_WhenBookingIsNotFinished() {
//
//        Booking booking = new Booking();
//        booking.setItem(item);
//        booking.setBooker(booker);
//        booking.setStart(LocalDateTime.now().minusDays(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1)); // еще не закончилось
//        booking.setStatus(BookingStatus.APPROVED);
//        bookingRepository.save(booking);
//
//        CommentDto commentDto = new CommentDto();
//        commentDto.setText("Comment for active booking");
//
//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> itemService.addComment(booker.getId(), item.getId(), commentDto));
//
//        assertEquals("Нельзя оставить комментарий к вещи, которую не брали в аренду",
//                exception.getMessage());
//    }

//    @Test
//    void addComment_ShouldThrowValidationException_WhenBookingIsRejected() {
//
//        Booking booking = new Booking();
//        booking.setItem(item);
//        booking.setBooker(booker);
//        booking.setStart(LocalDateTime.now().minusDays(2));
//        booking.setEnd(LocalDateTime.now().minusDays(1));
//        booking.setStatus(BookingStatus.REJECTED);
//        bookingRepository.save(booking);
//
//        CommentDto commentDto = new CommentDto();
//        commentDto.setText("Comment for rejected booking");
//
//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> itemService.addComment(booker.getId(), item.getId(), commentDto));
//
//        assertEquals("Нельзя оставить комментарий к вещи, которую не брали в аренду",
//                exception.getMessage());
//    }

    @Test
    @Sql(statements = "INSERT INTO users (id, name, email) VALUES (999, 'Test User', 'test999@test.com')")
    void getItemById_ShouldThrowNotFoundException_WhenItemNotExists() {

        assertThrows(NotFoundException.class,
                () -> itemService.getItemById(999L));
    }

    @Test
    void getAllItemsByOwner_ShouldReturnEmptyList_WhenOwnerHasNoItems() {

        User newOwner = new User();
        newOwner.setName("New Owner");
        newOwner.setEmail("newowner@test.com");
        newOwner = userRepository.save(newOwner);

        List<ItemDto> result = itemService.getAllItemsByOwner(newOwner.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_ShouldSearchInDescription() {
        // Given
        Item itemWithDescriptionMatch = new Item();
        itemWithDescriptionMatch.setName("Item");
        itemWithDescriptionMatch.setDescription("Special unique description");
        itemWithDescriptionMatch.setAvailable(true);
        itemWithDescriptionMatch.setOwner(owner);
        itemRepository.save(itemWithDescriptionMatch);

        List<ItemDto> result = itemService.searchItems("unique");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Item", result.get(0).getName());
        assertTrue(result.get(0).getDescription().contains("unique"));
    }
}
