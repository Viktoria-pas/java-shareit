package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toItem_ShouldMapAllFieldsCorrectly() {

        User owner = new User(1L, "Owner Name", "owner@email.com");
        ItemRequest request = new ItemRequest(1L, "Need item", null, null, null);
        CommentDto commentDto = new CommentDto(1L, "Great item!", "Author Name", null);

        ItemDto itemDto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                1L,
                List.of(commentDto),
                null,
                null
        );

        Item item = ItemMapper.toItem(itemDto, owner, request);

        assertThat(item.getId()).isEqualTo(itemDto.getId());
        assertThat(item.getName()).isEqualTo(itemDto.getName());
        assertThat(item.getDescription()).isEqualTo(itemDto.getDescription());
        assertThat(item.getAvailable()).isEqualTo(itemDto.getAvailable());
        assertThat(item.getOwner()).isEqualTo(owner);
        assertThat(item.getRequest()).isEqualTo(request);
        assertThat(item.getComments()).hasSize(1);
        assertThat(item.getComments().get(0).getId());
    }

    @Test
    void toItem_ShouldHandleNullComments() {

        User owner = new User(1L, "Owner Name", "owner@email.com");
        ItemRequest request = new ItemRequest(1L, "Need item", null, null, null);

        ItemDto itemDto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                1L,
                null,  // null comments
                null,
                null
        );

        Item item = ItemMapper.toItem(itemDto, owner, request);

        assertThat(item.getComments()).isEmpty();
    }

    @Test
    void toItem_ShouldHandleNullRequest() {

        User owner = new User(1L, "Owner Name", "owner@email.com");

        ItemDto itemDto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                null,
                null,
                null,
                null
        );

        Item item = ItemMapper.toItem(itemDto, owner, null);

        assertThat(item.getRequest()).isNull();
    }
}
