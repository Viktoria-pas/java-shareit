package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestDtoJson;

    @Autowired
    private JacksonTester<ItemRequestCreateDto> itemRequestCreateDtoJson;

    @Autowired
    private JacksonTester<ItemResponseDto> itemResponseDtoJson;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        ItemResponseDto itemResponse = new ItemResponseDto(1L, "Дрель", 2L);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Нужна дрель для ремонта");
        itemRequestDto.setRequestorId(1L);
        itemRequestDto.setCreated(created);
        itemRequestDto.setItems(List.of(itemResponse));

        JsonContent<ItemRequestDto> result = itemRequestDtoJson.write(itemRequestDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Нужна дрель для ремонта");

        assertThat(result).hasJsonPath("$.requestorId");
        assertThat(result).extractingJsonPathNumberValue("$.requestorId").isEqualTo(1);

        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        assertThat(result).hasJsonPath("$.items");
        assertThat(result).hasJsonPath("$.items[0].id");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).hasJsonPath("$.items[0].name");
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).hasJsonPath("$.items[0].ownerId");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
    }

    @Test
    void testItemRequestDtoDeserialization() throws Exception {

        String json = "{\"id\": 1, \"description\": \"Нужна дрель для ремонта\", \"requestorId\": 1, " +
                "\"created\": \"2024-01-15T10:30:00\", \"items\": [{\"id\": 1, \"name\": \"Дрель\", \"ownerId\": 2}]}";

        ItemRequestDto result = itemRequestDtoJson.parseObject(json);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(result.getRequestorId()).isEqualTo(1L);
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(1L);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(result.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }

    @Test
    void testItemRequestDtoWithNullItems() throws Exception {

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Нужна дрель");
        itemRequestDto.setRequestorId(1L);
        itemRequestDto.setCreated(LocalDateTime.now());
        itemRequestDto.setItems(null);

        JsonContent<ItemRequestDto> result = itemRequestDtoJson.write(itemRequestDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.requestorId");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).hasJsonPath("$.items");
        assertThat(result).extractingJsonPathValue("$.items").isNull();
    }

}