package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;
    private final Logger log = LoggerFactory.getLogger(BookingController.class);

    @PostMapping
    public BookingResponseDto createBooking(
            @RequestBody @Valid BookingRequestDto bookingRequestDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Создание бронирования пользователем {}: {}", userId, bookingRequestDto);
        BookingResponseDto booking = bookingService.createBooking(bookingRequestDto, userId);
        log.info("Бронирование создано: {}", booking);
        return booking;
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBookingStatus(
            @PathVariable @Positive Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Обновление статуса бронирования {} пользователем {}: {}",
                bookingId, userId, approved ? "APPROVED" : "REJECTED");
        BookingResponseDto booking = bookingService.updateBookingStatus(bookingId, approved, userId);
        log.info("Статус бронирования обновлен: {}", booking);
        return booking;
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(
            @PathVariable @Positive Long bookingId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Получение бронирования {} пользователем {}", bookingId, userId);
        BookingResponseDto booking = bookingService.getBookingById(bookingId, userId);
        log.info("Найдено бронирование: {}", booking);
        return booking;
    }

    @GetMapping
    public List<BookingResponseDto> getUserBookings(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Получение бронирований пользователя {} с состоянием {}", userId, state);
        List<BookingResponseDto> bookings = bookingService.getUserBookings(userId, state);
        log.info("Найдено {} бронирований", bookings.size());
        return bookings;
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Получение бронирований владельца {} с состоянием {}", userId, state);
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(userId, state);
        log.info("Найдено {} бронирований для владельца", bookings.size());
        return bookings;
    }
}
