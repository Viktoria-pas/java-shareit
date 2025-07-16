package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingShortDto {
    private Long id;

    @NotNull
    @Positive
    private Long bookerId;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;
}
