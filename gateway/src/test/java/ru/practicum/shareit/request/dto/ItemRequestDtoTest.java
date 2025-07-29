package ru.practicum.shareit.request.dto;

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
class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

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
        ItemRequestDto dto = new ItemRequestDto("Нужна дрель");

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.description")
                .isEqualTo("Нужна дрель");
    }

    @Test
    void deserialize() throws Exception {
        String content = "{\"description\":\"Нужна дрель\"}";

        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(new ItemRequestDto("Нужна дрель"));
    }

    @Test
    void validDescription_NoViolations() {
        ItemRequestDto dto = new ItemRequestDto("Нужна дрель");
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void emptyDescription_HasViolation() {
        ItemRequestDto dto = new ItemRequestDto("");
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Описание запроса не может быть пустым");
    }

    @Test
    void nullDescription_HasViolation() {
        ItemRequestDto dto = new ItemRequestDto(null);
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Описание запроса не может быть пустым");
    }

    @Test
    void blankDescription_HasViolation() {
        ItemRequestDto dto = new ItemRequestDto("   ");
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Описание запроса не может быть пустым");
    }
}
