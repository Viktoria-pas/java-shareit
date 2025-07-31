package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


public class BookingValidator {

    private static final List<String> VALID_STATES = Arrays.asList(
            "ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"
    );

    public void validateBookingDates(BookingRequestDto bookingDto) {
        if (bookingDto.getStart() == null) {
            throw new ValidationException("Дата начала бронирования обязательна");
        }

        if (bookingDto.getEnd() == null) {
            throw new ValidationException("Дата окончания бронирования обязательна");
        }

        LocalDateTime now = LocalDateTime.now();

        if (bookingDto.getStart().isBefore(now)) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }

        if (bookingDto.getEnd().isBefore(now)) {
            throw new ValidationException("Дата окончания бронирования не может быть в прошлом");
        }

        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        if (bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала не может совпадать с датой окончания");
        }
    }

    public void validateBookingState(String state) {
        if (state == null || state.isBlank()) {
            throw new ValidationException("Параметр state не может быть пустым");
        }

        if (!VALID_STATES.contains(state.toUpperCase())) {
            throw new ValidationException("Неизвестный параметр state: " + state +
                    ". Допустимые значения: " + String.join(", ", VALID_STATES));
        }
    }
}
