package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(ItemRequestCreateDto requestDto, Long userId) {
        log.info("Создание запроса вещи пользователем с id: {}", userId);

        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto.getDescription(), userId);
        ItemRequest savedRequest = itemRequestRepository.save(request);

        log.info("Запрос вещи создан с id: {}", savedRequest.getId());
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя с id: {}", userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        log.info("Получение всех запросов кроме пользователя с id: {}, from: {}, size: {}", userId, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        return itemRequestRepository.findAllExceptUserRequests(userId, pageable)
                .stream()
                .map(ItemRequestMapper::toItemRequestDtoForAll)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getRequestById(Long requestId) {
        log.info("Получение запроса с id: {}", requestId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        return ItemRequestMapper.toItemRequestDto(request);
    }
}
