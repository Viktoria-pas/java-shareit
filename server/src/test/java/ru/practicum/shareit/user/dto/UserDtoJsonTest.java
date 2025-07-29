package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("UserDto JSON Serialization Tests")
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    @DisplayName("Should serialize UserDto to JSON correctly")
    void shouldSerializeUserDtoToJson() throws IOException {

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test User");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("test@example.com");

        assertThat(result).isEqualToJson("{\"id\": 1, \"name\": \"Test User\", \"email\": \"test@example.com\"}");
    }

    @Test
    @DisplayName("Should deserialize JSON to UserDto correctly")
    void shouldDeserializeJsonToUserDto() throws IOException {

        String jsonContent = "{\"id\": 1, \"name\": \"Test User\", \"email\": \"test@example.com\"}";

        UserDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should handle null values in serialization")
    void shouldHandleNullValuesInSerialization() throws IOException {

        UserDto userDto = new UserDto();
        userDto.setId(null);
        userDto.setName(null);
        userDto.setEmail(null);

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathValue("$.id").isNull();
        assertThat(result).extractingJsonPathValue("$.name").isNull();
        assertThat(result).extractingJsonPathValue("$.email").isNull();
    }

    @Test
    @DisplayName("Should handle null values in deserialization")
    void shouldHandleNullValuesInDeserialization() throws IOException {

        String jsonContent = "{\"id\": null, \"name\": null, \"email\": null}";

        UserDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isNull();
        assertThat(result.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should handle missing fields in deserialization")
    void shouldHandleMissingFieldsInDeserialization() throws IOException {

        String jsonContent = "{\"name\": \"Test User\"}";

        UserDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should handle empty JSON object")
    void shouldHandleEmptyJsonObject() throws IOException {

        String jsonContent = "{}";

        UserDto result = json.parse(jsonContent).getObject();
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isNull();
        assertThat(result.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should serialize UserDto with only ID")
    void shouldSerializeUserDtoWithOnlyId() throws IOException {

        UserDto userDto = new UserDto();
        userDto.setId(42L);

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(42);
        assertThat(result).extractingJsonPathValue("$.name").isNull();
        assertThat(result).extractingJsonPathValue("$.email").isNull();
    }

    @Test
    @DisplayName("Should handle special characters in name and email")
    void shouldHandleSpecialCharactersInNameAndEmail() throws IOException {

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Тест Пользователь with émojis 😊");
        userDto.setEmail("test+tag@example-domain.co.uk");

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo("Тест Пользователь with émojis 😊");
        assertThat(result).extractingJsonPathStringValue("$.email")
                .isEqualTo("test+tag@example-domain.co.uk");
    }

    @Test
    @DisplayName("Should deserialize special characters correctly")
    void shouldDeserializeSpecialCharactersCorrectly() throws IOException {

        String jsonContent = "{\"id\": 1, \"name\": \"Тест Пользователь with émojis 😊\", \"email\": \"test+tag@example-domain.co.uk\"}";

        UserDto result = json.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("Тест Пользователь with émojis 😊");
        assertThat(result.getEmail()).isEqualTo("test+tag@example-domain.co.uk");
    }
}
