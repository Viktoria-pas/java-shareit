package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        List<CommentDto> commentDtos = item.getComments() != null ?
                item.getComments().stream()
                        .map(CommentMapper::toCommentDto)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                commentDtos,
                null,
                null
        );
    }

    public static Item toItem(ItemDto itemDto, User owner, ItemRequest request) {
        List<Comment> comments = itemDto.getComments() != null ?
                itemDto.getComments().stream()
                        .map(CommentMapper::toComment)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request,
                comments
        );
    }
}
