package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestBody @Valid BookingRequestDto bookingRequestDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Gateway: создание бронирования пользователем {}: {}", userId, bookingRequestDto);
        return bookingClient.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(
            @PathVariable @Positive Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Gateway: обновление статуса бронирования {} пользователем {}: {}",
                bookingId, userId, approved ? "APPROVED" : "REJECTED");
        return bookingClient.updateBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @PathVariable @Positive Long bookingId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Gateway: получение бронирования {} пользователем {}", bookingId, userId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Gateway: получение бронирований пользователя {} с состоянием {}", userId, state);
        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Gateway: получение бронирований владельца {} с состоянием {}", userId, state);
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }
}