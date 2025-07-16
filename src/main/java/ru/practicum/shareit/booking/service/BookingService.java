package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long userId);

    BookingResponseDto updateBookingStatus(Long bookingId, Boolean approved, Long userId);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getUserBookings(Long userId, String state);

    List<BookingResponseDto> getOwnerBookings(Long userId, String state);

    List<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(Long userId, Long itemId, BookingStatus bookingStatus, LocalDateTime time);

}
