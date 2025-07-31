package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRequestDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_ValidBookingRequestDto_ShouldPassValidation() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_NullItemId_ShouldFailValidation() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("ID вещи должен быть указан");
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("itemId");
    }

    @Test
    void validate_NullStartDate_ShouldFailValidation() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                null,
                LocalDateTime.now().plusDays(2)
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Дата начала должна быть указана");
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("start");
    }

    @Test
    void validate_NullEndDate_ShouldFailValidation() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                null
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Дата окончания должна быть указана");
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("end");
    }

    @Test
    void validate_StartDateInPast_ShouldFailValidation() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().minusDays(1), // Past date
                LocalDateTime.now().plusDays(2)
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Дата начала не может быть в прошлом");
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("start");
    }

    @Test
    void validate_EndDateNotInFuture_ShouldFailValidation() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1) // Past date
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Дата окончания должна быть в будущем");
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("end");
    }

    @Test
    void validate_EndDateIsExactlyNow_ShouldFailValidation() {

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                now.plusDays(1),
                now
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Дата окончания должна быть в будущем");
    }

    @Test
    void validate_MultipleViolations_ShouldReturnAllViolations() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                null, // Invalid itemId
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(2)
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).hasSize(3);

        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(violationMessages).containsExactlyInAnyOrder(
                "ID вещи должен быть указан",
                "Дата начала не может быть в прошлом",
                "Дата окончания должна быть в будущем"
        );
    }

    @Test
    void validate_AllFieldsNull_ShouldReturnAllViolations() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(null, null, null);

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).hasSize(3);

        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(violationMessages).containsExactlyInAnyOrder(
                "ID вещи должен быть указан",
                "Дата начала должна быть указана",
                "Дата окончания должна быть указана"
        );
    }

    @Test
    void validate_StartDateInFuture_EndDateFarInFuture_ShouldPassValidation() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(20)
        );

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(bookingRequestDto);

        assertThat(violations).isEmpty();
    }
}