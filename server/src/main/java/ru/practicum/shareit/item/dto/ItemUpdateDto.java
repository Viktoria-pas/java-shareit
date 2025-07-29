package ru.practicum.shareit.item.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemUpdateDto {

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;
}
