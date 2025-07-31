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
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
    private Item unavailableItem;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;
    private Booking approvedBooking;
    private Booking rejectedBooking;

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

        unavailableItem = new Item();
        unavailableItem.setId(2L);
        unavailableItem.setName("Unavailable Item");
        unavailableItem.setDescription("Unavailable Description");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);

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

        approvedBooking = new Booking();
        approvedBooking.setId(2L);
        approvedBooking.setStart(LocalDateTime.now().minusDays(2));
        approvedBooking.setEnd(LocalDateTime.now().minusDays(1));
        approvedBooking.setItem(item);
        approvedBooking.setBooker(booker);
        approvedBooking.setStatus(BookingStatus.APPROVED);

        rejectedBooking = new Booking();
        rejectedBooking.setId(3L);
        rejectedBooking.setStart(LocalDateTime.now().plusDays(3));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(4));
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(booker);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
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
        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
    }

    @Test
    void createBooking_WhenUserNotFound_ShouldThrowNotFoundException() {

        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с ID 10 не найден");

        verify(userRepository).findById(10L);
        verifyNoInteractions(itemRepository, bookingRepository);
    }

    @Test
    void createBooking_WhenItemNotFound_ShouldThrowNotFoundException() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Предмет с ID 1 не найден");

        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowBadRequestException() {

        bookingRequestDto.setItemId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(unavailableItem));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Предмет недоступен для бронирования");

        verify(userRepository).findById(2L);
        verify(itemRepository).findById(2L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_WhenOwnerTriesToBook_ShouldThrowBadRequestException() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Владелец не может забронировать свою вещь");

        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_WhenStartAfterEnd_ShouldThrowBadRequestException() {

        bookingRequestDto.setStart(LocalDateTime.now().plusDays(2));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Дата начала не может быть позже даты окончания");

        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void updateBookingStatus_WhenValidApproval_ShouldReturnApprovedBooking() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBookingStatus(1L, true, 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateBookingStatus_WhenValidRejection_ShouldReturnRejectedBooking() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBookingStatus(1L, false, 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateBookingStatus_WhenBookingNotFound_ShouldThrowNotFoundException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBookingStatus(1L, true, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Бронирование с ID 1 не найден");

        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBookingStatus_WhenNotOwner_ShouldThrowBadRequestException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus(1L, true, 999L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Только владелец может изменить статус бронирования");

        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBookingStatus_WhenBookingAlreadyApproved_ShouldThrowBadRequestException() {

        approvedBooking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(approvedBooking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus(2L, true, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Нельзя изменить статус уже обработанного бронирования");

        verify(bookingRepository).findById(2L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBookingStatus_WhenBookingAlreadyRejected_ShouldThrowBadRequestException() {

        rejectedBooking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(rejectedBooking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus(3L, false, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Нельзя изменить статус уже обработанного бронирования");

        verify(bookingRepository).findById(3L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBookingById_WhenBookerRequests_ShouldReturnBooking() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void getBookingById_WhenOwnerRequests_ShouldReturnBooking() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void getBookingById_WhenBookingNotFound_ShouldThrowNotFoundException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Бронирование с ID 1 не найден");

        verify(bookingRepository).findById(1L);
    }

    @Test
    void getBookingById_WhenUnauthorizedUser_ShouldThrowConflictException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 999L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Нет доступа к данному бронированию");

        verify(bookingRepository).findById(1L);
    }

    @Test
    void getUserBookings_WithStateAll_ShouldReturnAllBookings() {

        List<Booking> bookings = Arrays.asList(booking, approvedBooking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(2L)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "ALL");

        assertThat(result).hasSize(2);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdOrderByStartDesc(2L);
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
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUserBookings_WithStatePast_ShouldReturnPastBookings() {

        List<Booking> bookings = Arrays.asList(approvedBooking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(eq(2L), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "PAST");

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdAndEndBeforeOrderByStartDesc(eq(2L), any(LocalDateTime.class));
    }

    @Test
    void getUserBookings_WithStateFuture_ShouldReturnFutureBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(eq(2L), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "FUTURE");

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdAndStartAfterOrderByStartDesc(eq(2L), any(LocalDateTime.class));
    }

    @Test
    void getUserBookings_WithStateWaiting_ShouldReturnWaitingBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(2L, BookingStatus.WAITING))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "WAITING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(2L, BookingStatus.WAITING);
    }

    @Test
    void getUserBookings_WithStateRejected_ShouldReturnRejectedBookings() {

        List<Booking> bookings = Arrays.asList(rejectedBooking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(2L, BookingStatus.REJECTED))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "REJECTED");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(2L, BookingStatus.REJECTED);
    }

    @Test
    void getUserBookings_WithInvalidState_ShouldThrowConflictException() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThatThrownBy(() -> bookingService.getUserBookings(2L, "INVALID"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Неизвестный параметр state: INVALID");

        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getUserBookings_WhenUserNotFound_ShouldThrowNotFoundException() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getUserBookings(999L, "ALL"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с ID 999 не найден");

        verify(userRepository).findById(999L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getUserBookings_WithEmptyResult_ShouldReturnEmptyList() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(2L)).thenReturn(Collections.emptyList());

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "ALL");

        assertThat(result).isEmpty();
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdOrderByStartDesc(2L);
    }

    @Test
    void getOwnerBookings_WithStateAll_ShouldReturnAllOwnerBookings() {

        List<Booking> bookings = Arrays.asList(booking, approvedBooking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(1L)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "ALL");

        assertThat(result).hasSize(2);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(1L);
    }

    @Test
    void getOwnerBookings_WithStateCurrent_ShouldReturnCurrentOwnerBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "CURRENT");

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getOwnerBookings_WithStatePast_ShouldReturnPastOwnerBookings() {

        List<Booking> bookings = Arrays.asList(approvedBooking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "PAST");

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getOwnerBookings_WithStateFuture_ShouldReturnFutureOwnerBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "FUTURE");

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdAndStartAfterOrderByStartDesc(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getOwnerBookings_WithStateWaiting_ShouldReturnWaitingOwnerBookings() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.WAITING))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "WAITING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.WAITING);
    }

    @Test
    void getOwnerBookings_WithStateRejected_ShouldReturnRejectedOwnerBookings() {

        List<Booking> bookings = Arrays.asList(rejectedBooking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.REJECTED))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "REJECTED");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.REJECTED);
    }

    @Test
    void getOwnerBookings_WithInvalidState_ShouldThrowConflictException() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> bookingService.getOwnerBookings(1L, "INVALID"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Неизвестный параметр state: INVALID");

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getOwnerBookings_WhenUserNotFound_ShouldThrowNotFoundException() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getOwnerBookings(999L, "ALL"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с ID 999 не найден");

        verify(userRepository).findById(999L);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getOwnerBookings_WithEmptyResult_ShouldReturnEmptyList() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(1L)).thenReturn(Collections.emptyList());

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "ALL");

        assertThat(result).isEmpty();
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(1L);
    }

    @Test
    void findByBookerIdAndItemIdAndStatusAndEndBefore_ShouldReturnMatchingBookings() {

        List<Booking> bookings = Arrays.asList(approvedBooking);
        LocalDateTime time = LocalDateTime.now();
        when(bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time))
                .thenReturn(bookings);

        List<Booking> result = bookingService.findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time);
    }

    @Test
    void findByBookerIdAndItemIdAndStatusAndEndBefore_WithNoMatches_ShouldReturnEmptyList() {

        LocalDateTime time = LocalDateTime.now();
        when(bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time))
                .thenReturn(Collections.emptyList());

        List<Booking> result = bookingService.findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time);

        assertThat(result).isEmpty();
        verify(bookingRepository).findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time);
    }

    @Test
    void findByBookerIdAndItemIdAndStatusAndEndBefore_WithMultipleBookings_ShouldReturnAllMatching() {

        Booking anotherApprovedBooking = new Booking();
        anotherApprovedBooking.setId(4L);
        anotherApprovedBooking.setStart(LocalDateTime.now().minusDays(5));
        anotherApprovedBooking.setEnd(LocalDateTime.now().minusDays(3));
        anotherApprovedBooking.setItem(item);
        anotherApprovedBooking.setBooker(booker);
        anotherApprovedBooking.setStatus(BookingStatus.APPROVED);

        List<Booking> bookings = Arrays.asList(approvedBooking, anotherApprovedBooking);
        LocalDateTime time = LocalDateTime.now();
        when(bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time))
                .thenReturn(bookings);

        List<Booking> result = bookingService.findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(approvedBooking, anotherApprovedBooking);
        verify(bookingRepository).findByBookerIdAndItemIdAndStatusAndEndBefore(
                2L, 1L, BookingStatus.APPROVED, time);
    }

    @Test
    void getUserBookings_WithLowerCaseState_ShouldWorkCorrectly() {

        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(2L)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "all");

        assertThat(result).hasSize(1);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdOrderByStartDesc(2L);
    }

    @Test
    void getOwnerBookings_WithMixedCaseState_ShouldWorkCorrectly() {
        List<Booking> bookings = Arrays.asList(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.WAITING))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "Waiting");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.WAITING);
    }

    @Test
    void updateBookingStatus_WhenApprovalIsNull_ShouldHandleCorrectly() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBookingStatus(1L, false, 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBooking_ShouldSetCorrectBookingFields() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            savedBooking.setId(1L);
            return savedBooking;
        });

        BookingResponseDto result = bookingService.createBooking(bookingRequestDto, 2L);

        verify(bookingRepository).save(argThat(savedBooking ->
                savedBooking.getStart().equals(bookingRequestDto.getStart()) &&
                        savedBooking.getEnd().equals(bookingRequestDto.getEnd()) &&
                        savedBooking.getItem().equals(item) &&
                        savedBooking.getBooker().equals(booker) &&
                        savedBooking.getStatus().equals(BookingStatus.WAITING)
        ));
    }

    @Test
    void getUserBookings_ShouldReturnBookingsInCorrectOrder() {

        Booking olderBooking = new Booking();
        olderBooking.setId(5L);
        olderBooking.setStart(LocalDateTime.now().plusDays(5));
        olderBooking.setEnd(LocalDateTime.now().plusDays(6));
        olderBooking.setItem(item);
        olderBooking.setBooker(booker);
        olderBooking.setStatus(BookingStatus.WAITING);

        List<Booking> bookings = Arrays.asList(booking, olderBooking); // порядок по убыванию даты начала
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(2L)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "ALL");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L); // первое бронирование
        assertThat(result.get(1).getId()).isEqualTo(5L); // второе бронирование
        verify(bookingRepository).findByBookerIdOrderByStartDesc(2L);
    }
}
