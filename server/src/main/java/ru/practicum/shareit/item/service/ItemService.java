package ru.practicum.shareit.item.service;



import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemUpdateDto itemUpdateDto, Long ownerId);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getAllItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
