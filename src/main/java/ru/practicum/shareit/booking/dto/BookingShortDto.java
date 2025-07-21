package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
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
