package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final EntityManager entityManager;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.com");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @AfterEach
    void tearDown() {

        entityManager.createQuery("DELETE FROM bookings").executeUpdate();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createBooking_IntegrationTest_ShouldCreateAndSaveBooking() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto result = bookingService.createBooking(bookingRequestDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());

        // Проверяем что бронирование сохранилось в БД
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM bookings b WHERE b.id = :id", Booking.class);
        query.setParameter("id", result.getId());
        Booking savedBooking = query.getSingleResult();

        assertThat(savedBooking).isNotNull();
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void updateBookingStatus_IntegrationTest_ShouldUpdateStatusInDatabase() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequestDto, booker.getId());

        BookingResponseDto result = bookingService.updateBookingStatus(
                createdBooking.getId(), true, owner.getId());

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);

        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM bookings b WHERE b.id = :id", Booking.class);
        query.setParameter("id", result.getId());
        Booking updatedBooking = query.getSingleResult();

        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getUserBookings_IntegrationTest_ShouldReturnUserBookingsFromDatabase() {

        BookingRequestDto bookingRequestDto1 = new BookingRequestDto();
        bookingRequestDto1.setItemId(item.getId());
        bookingRequestDto1.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto1.setEnd(LocalDateTime.now().plusDays(2));

        BookingRequestDto bookingRequestDto2 = new BookingRequestDto();
        bookingRequestDto2.setItemId(item.getId());
        bookingRequestDto2.setStart(LocalDateTime.now().plusDays(3));
        bookingRequestDto2.setEnd(LocalDateTime.now().plusDays(4));

        bookingService.createBooking(bookingRequestDto1, booker.getId());
        bookingService.createBooking(bookingRequestDto2, booker.getId());

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "ALL");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(booking -> booking.getBooker().getId().equals(booker.getId()));

        assertThat(result.get(0).getStart()).isAfter(result.get(1).getStart());
    }

    @Test
    void getUserBookings_WithStateFuture_IntegrationTest_ShouldReturnOnlyFutureBookings() {

        BookingRequestDto pastBooking = new BookingRequestDto();
        pastBooking.setItemId(item.getId());
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));

        BookingRequestDto futureBooking = new BookingRequestDto();
        futureBooking.setItemId(item.getId());
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));

        Booking pastBookingEntity = new Booking();
        pastBookingEntity.setStart(pastBooking.getStart());
        pastBookingEntity.setEnd(pastBooking.getEnd());
        pastBookingEntity.setItem(item);
        pastBookingEntity.setBooker(booker);
        pastBookingEntity.setStatus(BookingStatus.APPROVED);
        entityManager.persist(pastBookingEntity);

        bookingService.createBooking(futureBooking, booker.getId());

        List<BookingResponseDto> result = bookingService.getUserBookings(booker.getId(), "FUTURE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStart()).isAfter(LocalDateTime.now());
    }

    @Test
    void getOwnerBookings_IntegrationTest_ShouldReturnOwnerBookingsFromDatabase() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingService.createBooking(bookingRequestDto, booker.getId());

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), "ALL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void findByBookerIdAndItemIdAndStatusAndEndBefore_IntegrationTest_ShouldReturnMatchingBookings() {

        Booking completedBooking = new Booking();
        completedBooking.setStart(LocalDateTime.now().minusDays(3));
        completedBooking.setEnd(LocalDateTime.now().minusDays(1));
        completedBooking.setItem(item);
        completedBooking.setBooker(booker);
        completedBooking.setStatus(BookingStatus.APPROVED);
        entityManager.persist(completedBooking);
        entityManager.flush();

        List<Booking> result = bookingService.findByBookerIdAndItemIdAndStatusAndEndBefore(
                booker.getId(), item.getId(), BookingStatus.APPROVED, LocalDateTime.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.get(0).getItem().getId()).isEqualTo(item.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(result.get(0).getEnd()).isBefore(LocalDateTime.now());
    }

//    @Test
//    void createBooking_WhenItemNotAvailable_IntegrationTest_ShouldThrowValidationException() {
//
//        item.setAvailable(false);
//        item = itemRepository.save(item);
//
//        BookingRequestDto bookingRequestDto = new BookingRequestDto();
//        bookingRequestDto.setItemId(item.getId());
//        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
//        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));
//
//        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
//                .isInstanceOf(ValidationException.class)
//                .hasMessage("Предмет недоступен для бронирования");
//    }

    @Test
    void updateBookingStatus_WhenBookingNotExists_IntegrationTest_ShouldThrowNotFoundException() {

        assertThatThrownBy(() -> bookingService.updateBookingStatus(999L, true, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Бронирование с ID 999 не найден");
    }

    @Test
    void getBookingById_WhenBookingExists_IntegrationTest_ShouldReturnBookingFromDatabase() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequestDto, booker.getId());

        BookingResponseDto result = bookingService.getBookingById(createdBooking.getId(), booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(createdBooking.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
    }
}
