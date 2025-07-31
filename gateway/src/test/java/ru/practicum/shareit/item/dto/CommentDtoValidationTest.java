package ru.practicum.shareit.item.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_ValidCommentDto_NoViolations() {

        CommentDto commentDto = new CommentDto("Great item, really useful!");

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_BlankText_HasViolation() {

        CommentDto commentDto = new CommentDto("");

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertEquals(1, violations.size());
        ConstraintViolation<CommentDto> violation = violations.iterator().next();
        assertEquals("Текст комментария не может быть пустым", violation.getMessage());
        assertEquals("text", violation.getPropertyPath().toString());
    }

    @Test
    void validate_NullText_HasViolation() {

        CommentDto commentDto = new CommentDto(null);

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertEquals(1, violations.size());
        ConstraintViolation<CommentDto> violation = violations.iterator().next();
        assertEquals("Текст комментария не может быть пустым", violation.getMessage());
        assertEquals("text", violation.getPropertyPath().toString());
    }

    @Test
    void validate_WhitespaceText_HasViolation() {

        CommentDto commentDto = new CommentDto("   ");

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertEquals(1, violations.size());
        ConstraintViolation<CommentDto> violation = violations.iterator().next();
        assertEquals("Текст комментария не может быть пустым", violation.getMessage());
        assertEquals("text", violation.getPropertyPath().toString());
    }

    @Test
    void validate_CommentWithSpecialCharacters_NoViolations() {

        CommentDto commentDto = new CommentDto("Excellent! Works 100% as expected. Rating: 5/5 ⭐⭐⭐⭐⭐");

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_LongComment_NoViolations() {

        String longText = "This is a very long comment that contains a lot of text to test if the validation " +
                "works correctly with longer strings. The comment should be valid as long as it's not blank. " +
                "Here we test that validation doesn't impose any length restrictions on the comment text.";
        CommentDto commentDto = new CommentDto(longText);

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_CommentWithNewlines_NoViolations() {

        CommentDto commentDto = new CommentDto("Line 1\nLine 2\nLine 3");

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_SingleCharacterComment_NoViolations() {

        CommentDto commentDto = new CommentDto("!");

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertTrue(violations.isEmpty());
    }
}
