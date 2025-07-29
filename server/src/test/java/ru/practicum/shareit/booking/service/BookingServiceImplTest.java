package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@test.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@test.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
    }

    @Test
    void createBooking_WhenValidData_ShouldReturnBookingResponseDto() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.createBooking(bookingRequestDto, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getBooker().getId()).isEqualTo(2L);
        assertThat(result.getItem().getId()).isEqualTo(1L);

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenUserNotFound_ShouldThrowNotFoundException() {

        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с ID 10 не найден");
    }

    @Test
    void createBooking_WhenItemNotFound_ShouldThrowNotFoundException() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Предмет с ID 1 не найден");
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowValidationException() {

        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 2L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Предмет недоступен для бронирования");
    }

    @Test
    void createBooking_WhenOwnerTriesToBook_ShouldThrowValidationException() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Владелец не может забронировать свою вещь");
    }

    @Test
    void createBooking_WhenStartAfterEnd_ShouldThrowValidationException() {

        bookingRequestDto.setStart(LocalDateTime.now().plusDays(2));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 2L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Дата начала не может быть позже даты окончания");
    }

    @Test
    void updateBookingStatus_WhenValidApproval_ShouldReturnApprovedBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBookingStatus(1L, true, 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateBookingStatus_WhenValidRejection_ShouldReturnRejectedBooking() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBookingStatus(1L, false, 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateBookingStatus_WhenBookingNotFound_ShouldThrowNotFoundException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBookingStatus(1L, true, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Бронирование с ID 1 не найден");
    }

    @Test
    void updateBookingStatus_WhenNotOwner_ShouldThrowValidationException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus(1L, true, 999L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Только владелец может изменить статус бронирования");
    }

    @Test
    void updateBookingStatus_WhenBookingAlreadyProcessed_ShouldThrowValidationException() {

        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus(1L, true, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Нельзя изменить статус уже обработанного бронирования");
    }

    @Test
    void getBookingById_WhenBookerRequests_ShouldReturnBooking() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookingById_WhenOwnerRequests_ShouldReturnBooking() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookingById_WhenUnauthorizedUser_ShouldThrowValidationException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 999L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Нет доступа к данному бронированию");
    }

    @Test
    void getUserBookings_WithStateAll_ShouldReturnAllBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(2L)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "ALL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getUserBookings_WithStateCurrent_ShouldReturnCurrentBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "CURRENT");

        assertThat(result).hasSize(1);
    }

    @Test
    void getUserBookings_WithInvalidState_ShouldThrowValidationException() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThatThrownBy(() -> bookingService.getUserBookings(2L, "INVALID"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Неизвестный параметр state: INVALID");
    }

    @Test
    void getOwnerBookings_WithStateAll_ShouldReturnAllOwnerBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(1L)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "ALL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void findByBookerIdAndItemIdAndStatusAndEndBefore_ShouldReturnBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        LocalDateTime time = LocalDateTime.now();
        when(bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time))
                .thenReturn(bookings);

        List<Booking> result = bookingService.findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
}
