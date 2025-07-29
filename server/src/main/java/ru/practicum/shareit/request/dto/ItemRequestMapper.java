package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(String description, Long requestorId) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestorId(requestorId);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        List<ItemResponseDto> items = request.getItems() != null
                ? request.getItems().stream()
                .map(ItemRequestMapper::toItemResponseDto)
                .collect(Collectors.toList())
                : List.of();

        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getRequestorId(),
                request.getCreated(),
                items
        );
    }

    public static ItemRequestDto toItemRequestDtoForAll(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getRequestorId(),
                request.getCreated(),
                null
        );
    }

    private static ItemResponseDto toItemResponseDto(Item item) {
        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getOwner().getId()
        );
    }
}
