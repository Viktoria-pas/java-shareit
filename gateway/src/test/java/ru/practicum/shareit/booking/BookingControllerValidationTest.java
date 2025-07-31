package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerValidationTest {

    @InjectMocks
    private BookingValidator bookingValidator;

    private BookingRequestDto createValidBookingRequest() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusDays(1);
        return new BookingRequestDto(1L, start, end);
    }

    @Test
    void validateBookingDates_ShouldAcceptValidDates() {
        BookingRequestDto validBooking = createValidBookingRequest();
        assertDoesNotThrow(() -> bookingValidator.validateBookingDates(validBooking));
    }

    @Test
    void validateBookingDates_ShouldRejectNullStartDate() {
        BookingRequestDto booking = createValidBookingRequest();
        booking.setStart(null);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateBookingDates(booking)
        );
        assertEquals("Дата начала бронирования обязательна", exception.getMessage());
    }

    @Test
    void validateBookingDates_ShouldRejectNullEndDate() {
        BookingRequestDto booking = createValidBookingRequest();
        booking.setEnd(null);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateBookingDates(booking)
        );
        assertEquals("Дата окончания бронирования обязательна", exception.getMessage());
    }

    @Test
    void validateBookingDates_ShouldRejectStartInPast() {
        BookingRequestDto booking = createValidBookingRequest();
        booking.setStart(LocalDateTime.now().minusHours(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateBookingDates(booking)
        );
        assertEquals("Дата начала бронирования не может быть в прошлом", exception.getMessage());
    }

    @Test
    void validateBookingDates_ShouldRejectEndInPast() {
        BookingRequestDto booking = createValidBookingRequest();
        booking.setEnd(LocalDateTime.now().minusHours(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateBookingDates(booking)
        );
        assertEquals("Дата окончания бронирования не может быть в прошлом", exception.getMessage());
    }

    @Test
    void validateBookingDates_ShouldRejectStartAfterEnd() {
        BookingRequestDto booking = createValidBookingRequest();
        booking.setStart(booking.getEnd().plusHours(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateBookingDates(booking)
        );
        assertEquals("Дата начала не может быть позже даты окончания", exception.getMessage());
    }

    @Test
    void validateBookingDates_ShouldRejectStartEqualsEnd() {
        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(1L, now.plusHours(1), now.plusHours(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateBookingDates(booking)
        );
        assertEquals("Дата начала не может совпадать с датой окончания", exception.getMessage());
    }

    @Test
    void validateBookingDates_ShouldRejectCurrentTimeAsEnd() {
        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto booking = new BookingRequestDto(1L, now.plusHours(1), now);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingValidator.validateBookingDates(booking)
        );
        assertTrue(exception.getMessage().contains("не может быть"));
    }
}
