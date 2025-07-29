package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Gateway: добавление новой вещи пользователем {}: {}", userId, itemDto);
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody ItemUpdateDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Gateway: обновление вещи с ID {} пользователем {}: {}", itemId, userId, itemDto);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable @Positive Long itemId) {
        log.info("Gateway: получение вещи с ID {}", itemId);
        return itemClient.getItemById(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Gateway: получение всех вещей владельца {}", userId);
        return itemClient.getAllItemsByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        log.info("Gateway: поиск вещей по тексту '{}'", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody CommentDto commentDto) {
        log.info("Gateway: добавление комментария к вещи с ID {} пользователем с ID {}", itemId, userId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}

