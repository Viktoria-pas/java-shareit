package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final Logger log = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;
    private final UserService userService;

    public ItemController(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto itemDto,
                           @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Добавление новой вещи пользователем {}: {}", ownerId, itemDto);
        userService.getUserById(ownerId);
        ItemDto createdItem = itemService.addItem(itemDto, ownerId);
        log.info("Вещь добавлена: {}", createdItem);
        return createdItem;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @Valid @RequestBody ItemUpdateDto itemUpdateDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Обновление вещи с ID {} пользователем {}: {}", itemId, ownerId, itemUpdateDto);
        ItemDto updatedItem = itemService.updateItem(itemId, itemUpdateDto, ownerId);
        log.info("Вещь обновлена: {}", updatedItem);
        return updatedItem;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("Получение вещи с ID: {}", itemId);
        ItemDto item = itemService.getItemById(itemId);
        log.info("Найдена вещь: {}", item);
        return item;
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получение всех вещей владельца {}", ownerId);
        List<ItemDto> items = itemService.getAllItemsByOwner(ownerId);
        log.info("Найдено {} вещей владельца {}", items.size(), ownerId);
        return items;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        List<ItemDto> foundItems = itemService.searchItems(text);
        log.info("Найдено {} вещей по запросу '{}'", foundItems.size(), text);
        return foundItems;
    }
}
