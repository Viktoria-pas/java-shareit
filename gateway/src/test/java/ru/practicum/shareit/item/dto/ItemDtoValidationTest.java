package ru.practicum.shareit.item.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_ValidItemDto_NoViolations() {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_BlankName_HasViolation() {

        ItemDto itemDto = new ItemDto("", "Test Description", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertEquals(1, violations.size());
        ConstraintViolation<ItemDto> violation = violations.iterator().next();
        assertEquals("Название не может быть пустым", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void validate_NullName_HasViolation() {

        ItemDto itemDto = new ItemDto(null, "Test Description", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertEquals(1, violations.size());
        ConstraintViolation<ItemDto> violation = violations.iterator().next();
        assertEquals("Название не может быть пустым", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void validate_WhitespaceName_HasViolation() {

        ItemDto itemDto = new ItemDto("   ", "Test Description", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertEquals(1, violations.size());
        ConstraintViolation<ItemDto> violation = violations.iterator().next();
        assertEquals("Название не может быть пустым", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void validate_BlankDescription_HasViolation() {

        ItemDto itemDto = new ItemDto("Test Item", "", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertEquals(1, violations.size());
        ConstraintViolation<ItemDto> violation = violations.iterator().next();
        assertEquals("Описание не может быть пустым", violation.getMessage());
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    void validate_NullDescription_HasViolation() {

        ItemDto itemDto = new ItemDto("Test Item", null, true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertEquals(1, violations.size());
        ConstraintViolation<ItemDto> violation = violations.iterator().next();
        assertEquals("Описание не может быть пустым", violation.getMessage());
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    void validate_WhitespaceDescription_HasViolation() {

        ItemDto itemDto = new ItemDto("Test Item", "   ", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertEquals(1, violations.size());
        ConstraintViolation<ItemDto> violation = violations.iterator().next();
        assertEquals("Описание не может быть пустым", violation.getMessage());
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    void validate_NullAvailable_HasViolation() {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", null, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertEquals(1, violations.size());
        ConstraintViolation<ItemDto> violation = violations.iterator().next();
        assertEquals("Статус доступности должен быть указан", violation.getMessage());
        assertEquals("available", violation.getPropertyPath().toString());
    }

    @Test
    void validate_MultipleViolations_ReturnsAllViolations() {

        ItemDto itemDto = new ItemDto("", "", null, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertEquals(3, violations.size());

        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(violationMessages.contains("Название не может быть пустым"));
        assertTrue(violationMessages.contains("Описание не может быть пустым"));
        assertTrue(violationMessages.contains("Статус доступности должен быть указан"));
    }

    @Test
    void validate_ValidItemDtoWithRequestId_NoViolations() {

        ItemDto itemDto = new ItemDto("Test Item", "Test Description", false, 123L);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertTrue(violations.isEmpty());
    }
}
