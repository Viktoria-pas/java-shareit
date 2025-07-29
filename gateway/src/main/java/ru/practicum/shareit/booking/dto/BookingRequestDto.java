package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    @NotNull(message = "ID вещи должен быть указан")
    private Long itemId;

    @NotNull(message = "Дата начала должна быть указана")
    @FutureOrPresent(message = "Дата начала не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания должна быть указана")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;
}
