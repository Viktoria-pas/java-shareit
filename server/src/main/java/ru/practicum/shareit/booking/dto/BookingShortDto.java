package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingShortDto {
    private Long id;

    private Long bookerId;

    private LocalDateTime start;

    private LocalDateTime end;
}
