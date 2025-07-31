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
class UserUpdateDtoTest {

    @Autowired
    private JacksonTester<UserUpdateDto> json;

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
        UserUpdateDto dto = new UserUpdateDto("Jane Doe", "jane@example.com");

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.name").isEqualTo("Jane Doe");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.email").isEqualTo("jane@example.com");
    }

    @Test
    void deserialize() throws Exception {
        String content = "{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}";

        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(new UserUpdateDto("Jane Doe", "jane@example.com"));
    }

    @Test
    void validUpdate_NoViolations() {
        UserUpdateDto dto = new UserUpdateDto("Jane Doe", "jane@example.com");
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void onlyName_NoViolations() {
        UserUpdateDto dto = new UserUpdateDto("Jane Doe", null);
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void onlyValidEmail_NoViolations() {
        UserUpdateDto dto = new UserUpdateDto(null, "jane@example.com");
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void invalidEmailFormat_HasViolation() {
        UserUpdateDto dto = new UserUpdateDto("Jane Doe", "invalid-email");
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Некорректный формат email");
    }

    @Test
    void bothFieldsNull_NoViolations() {
        UserUpdateDto dto = new UserUpdateDto(null, null);
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
