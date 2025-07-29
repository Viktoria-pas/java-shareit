package ru.practicum.shareit.request;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createRequest(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @RequestBody ItemRequestCreateDto requestDto) {
        log.info("POST /requests - создание запроса пользователем {}", userId);
        return itemRequestService.createRequest(requestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /requests - получение запросов пользователя {}", userId);
        return itemRequestService.getUserRequests(userId);
    }

    // В server версии изменить:
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        log.info("GET /requests/all - получение всех запросов, userId: {}, from: {}, size: {}", userId, from, size);
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long requestId) {
        log.info("GET /requests/{} - получение запроса пользователем {}", requestId, userId);
        return itemRequestService.getRequestById(requestId);
    }
}
