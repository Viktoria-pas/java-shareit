package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> jacksonTester;

    @Test
    void serialize_ValidCommentDto_ReturnsCorrectJson() throws Exception {

        CommentDto commentDto = new CommentDto("Great item, really useful!");

        JsonContent<CommentDto> result = jacksonTester.write(commentDto);

        assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo("Great item, really useful!");
    }

    @Test
    void serialize_CommentDtoWithSpecialCharacters_ReturnsCorrectJson() throws Exception {

        CommentDto commentDto = new CommentDto("Excellent! Works 100% as expected. Rating: 5/5 ⭐⭐⭐⭐⭐");

        JsonContent<CommentDto> result = jacksonTester.write(commentDto);

        assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo("Excellent! Works 100% as expected. Rating: 5/5 ⭐⭐⭐⭐⭐");
    }

    @Test
    void deserialize_ValidJson_ReturnsCorrectCommentDto() throws Exception {

        String jsonContent = "{\"text\": \"Amazing product, highly recommend!\"}";

        CommentDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getText()).isEqualTo("Amazing product, highly recommend!");
    }

    @Test
    void deserialize_JsonWithSpecialCharacters_ReturnsCorrectCommentDto() throws Exception {

        String jsonContent = "{\"text\": \"Price: $50.99 - Quality: excellent! Would buy again 👍\"}";

        CommentDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getText()).isEqualTo("Price: $50.99 - Quality: excellent! Would buy again 👍");
    }

    @Test
    void deserialize_JsonWithEscapedCharacters_ReturnsCorrectCommentDto() throws Exception {

        String jsonContent = "{\"text\": \"Quote: \\\"This is the best item ever!\\\"\"}";

        CommentDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getText()).isEqualTo("Quote: \"This is the best item ever!\"");
    }

    @Test
    void deserialize_JsonWithMultilineText_ReturnsCorrectCommentDto() throws Exception {

        String jsonContent = "{\"text\": \"Line 1\\nLine 2\\nLine 3\"}";

        CommentDto result = jacksonTester.parse(jsonContent).getObject();

        assertThat(result.getText()).isEqualTo("Line 1\nLine 2\nLine 3");
    }
}
