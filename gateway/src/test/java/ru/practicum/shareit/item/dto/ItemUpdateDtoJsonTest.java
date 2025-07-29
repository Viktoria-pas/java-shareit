package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemUpdateDtoJsonTest {

    @Autowired
    private JacksonTester<ItemUpdateDto> jacksonTester;

    @Test
    void serialize_FullItemUpdateDto_ReturnsCorrectJson() throws Exception {

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Item", "Updated Description", false);

        JsonContent<ItemUpdateDto> result = jacksonTester.write(itemUpdateDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Updated Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Updated Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isFalse();
    }

    @Test
    void serialize_PartialItemUpdateDto_ReturnsCorrectJson() throws Exception {

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto("Updated Name Only", null, null);

        JsonContent<ItemUpdateDto> result = jacksonTester.write(itemUpdateDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Updated Name Only");
        assertThat(result).extractingJsonPathValue("$.description").isNull();
        assertThat(result).extractingJsonPathValue("$.available").isNull();
    }

    @Test
    void serialize_OnlyDescriptionUpdate_ReturnsCorrectJson() throws Exception {

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(null, "Only description updated", null);

        JsonContent<ItemUpdateDto> result = jacksonTester.write(itemUpdateDto);

        assertThat(result).extractingJsonPathValue("$.name").isNull();
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Only description updated");
        assertThat(result).extractingJsonPathValue("$.available").isNull();
    }

    @Test
    void serialize_OnlyAvailabilityUpdate_ReturnsCorrectJson() throws Exception {

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(null, null, true);

        JsonContent<ItemUpdateDto> result = jacksonTester.write(itemUpdateDto);

        assertThat(result).extractingJsonPathValue("$.name").isNull();
        assertThat(result).extractingJsonPathValue("$.description").isNull();
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
    }

    @Test
    void deserialize_FullJson_ReturnsCorrectItemUpdateDto() throws Exception {

        String jsonContent = """
                {
                    "name": "Deserialized Item",
                    "description": "Deserialized Description",
                    "available": true
                }
                """;

        ItemUpdateDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("Deserialized Item");
        assertThat(result.getDescription()).isEqualTo("Deserialized Description");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void deserialize_PartialJson_ReturnsCorrectItemUpdateDto() throws Exception {

        String jsonContent = """
                {
                    "name": "Only Name",
                    "description": null,
                    "available": null
                }
                """;

        ItemUpdateDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("Only Name");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getAvailable()).isNull();
    }

    @Test
    void deserialize_EmptyJson_ReturnsItemUpdateDtoWithNulls() throws Exception {

        String jsonContent = "{}";

        ItemUpdateDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getName()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getAvailable()).isNull();
    }

    @Test
    void deserialize_JsonWithMissingFields_ReturnsCorrectItemUpdateDto() throws Exception {

        String jsonContent = """
                {
                    "available": false
                }
                """;

        ItemUpdateDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getName()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getAvailable()).isFalse();
    }
}