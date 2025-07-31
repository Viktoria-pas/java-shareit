package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    @Test
    void toItemRequest_ShouldMapFieldsCorrectly() {

        String description = "Need item";
        Long requestorId = 1L;

        ItemRequest request = ItemRequestMapper.toItemRequest(description, requestorId);

        assertThat(request.getDescription()).isEqualTo(description);
        assertThat(request.getRequestorId()).isEqualTo(requestorId);
        assertThat(request.getCreated()).isNotNull();
    }

    @Test
    void toItemRequestDto_ShouldMapAllFieldsWithItems() {

        User owner = new User(2L, "Owner", "owner@email.com");
        Item item1 = new Item(1L, "Item 1", "Description 1", true, owner, null, null);
        Item item2 = new Item(2L, "Item 2", "Description 2", true, owner, null, null);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need items");
        request.setRequestorId(1L);
        request.setCreated(LocalDateTime.now());
        request.setItems(List.of(item1, item2));

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getRequestorId()).isEqualTo(request.getRequestorId());
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());
        assertThat(dto.getItems()).hasSize(2);
    }

    @Test
    void toItemRequestDto_ShouldHandleNullItems() {

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need items");
        request.setRequestorId(1L);
        request.setCreated(LocalDateTime.now());
        request.setItems(null);

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);

        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void toItemRequestDtoForAll_ShouldMapFieldsWithoutItems() {

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need items");
        request.setRequestorId(1L);
        request.setCreated(LocalDateTime.now());
        request.setItems(List.of(new Item()));

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDtoForAll(request);

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getRequestorId()).isEqualTo(request.getRequestorId());
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());
        assertThat(dto.getItems()).isNull();
    }
}
