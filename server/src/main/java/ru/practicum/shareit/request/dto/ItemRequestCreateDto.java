package ru.practicum.shareit.request.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestCreateDto {
    @NotBlank(message = "Описание запроса не может быть пустым")
    @Size(max = 512, message = "Описание не может быть длиннее 512 символов")
    private String description;
}
