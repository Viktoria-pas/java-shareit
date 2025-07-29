package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void serialize() throws Exception {
        UserDto dto = new UserDto(1L, "John Doe", "john@example.com");

        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.email").isEqualTo("john@example.com");
    }

    @Test
    void deserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@example.com\"}";

        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(new UserDto(1L, "John Doe", "john@example.com"));
    }

    @Test
    void validUser_NoViolations() {
        UserDto dto = new UserDto(1L, "John Doe", "john@example.com");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void emptyName_HasViolation() {
        UserDto dto = new UserDto(1L, "", "john@example.com");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Имя не может быть пустым");
    }

    @Test
    void nullName_HasViolation() {
        UserDto dto = new UserDto(1L, null, "john@example.com");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Имя не может быть пустым");
    }

    @Test
    void blankName_HasViolation() {
        UserDto dto = new UserDto(1L, "   ", "john@example.com");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Имя не может быть пустым");
    }

    @Test
    void emptyEmail_HasViolation() {
        UserDto dto = new UserDto(1L, "John Doe", "");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
    }

    @Test
    void nullEmail_HasViolation() {
        UserDto dto = new UserDto(1L, "John Doe", null);
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email не может быть пустым");
    }

    @Test
    void invalidEmailFormat_HasViolation() {
        UserDto dto = new UserDto(1L, "John Doe", "invalid-email");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Некорректный формат email");
    }
}
