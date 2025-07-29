package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> jacksonTester;

    @Test
    void serialize_ValidItemDto_ReturnsCorrectJson() throws Exception {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, 123L);

        JsonContent<ItemDto> result = jacksonTester.write(itemDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(123);
    }

    @Test
    void serialize_ItemDtoWithNullRequestId_ReturnsCorrectJson() throws Exception {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", false, null);

        JsonContent<ItemDto> result = jacksonTester.write(itemDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isFalse();
        assertThat(result).extractingJsonPathValue("$.requestId").isNull();
    }

    @Test
    void deserialize_ValidJson_ReturnsCorrectItemDto() throws Exception {

        String jsonContent = """
                {
                    "name": "Test Item",
                    "description": "Test Description",
                    "available": true,
                    "requestId": 456
                }
                """;

        ItemDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("Test Item");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isEqualTo(456L);
    }

    @Test
    void deserialize_JsonWithNullRequestId_ReturnsCorrectItemDto() throws Exception {

        String jsonContent = """
                {
                    "name": "Test Item",
                    "description": "Test Description",
                    "available": false,
                    "requestId": null
                }
                """;

        ItemDto result = jacksonTester.parse(jsonContent).getObject();

        // Assert
        assertThat(result.getName()).isEqualTo("Test Item");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void deserialize_JsonWithMissingRequestId_ReturnsCorrectItemDto() throws Exception {

        String jsonContent = """
                {
                    "name": "Test Item",
                    "description": "Test Description",
                    "available": true
                }
                """;

        ItemDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("Test Item");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isNull();
    }
}
