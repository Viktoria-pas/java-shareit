package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Test
    void serialize_ShouldCorrectlySerializeBookingRequestDto() throws Exception {

        LocalDateTime start = LocalDateTime.of(2025, 12, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 2, 12, 0, 0);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, start, end);

        JsonContent<BookingRequestDto> result = json.write(bookingRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void deserialize_ShouldCorrectlyDeserializeBookingRequestDto() throws Exception {

        String jsonContent = "{\"itemId\": 1, \"start\": \"2025-12-01T10:00:00\", \"end\": \"2025-12-02T12:00:00\"}";

        BookingRequestDto result = json.parse(jsonContent).getObject();

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2025, 12, 1, 10, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2025, 12, 2, 12, 0, 0));
    }

    @Test
    void deserialize_WithNullItemId_ShouldDeserializeSuccessfully() throws Exception {

        String jsonContent = "{\"itemId\": null, \"start\": \"2025-12-01T10:00:00\", \"end\": \"2025-12-02T12:00:00\"}";

        BookingRequestDto result = json.parse(jsonContent).getObject();

        assertThat(result.getItemId()).isNull();
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2025, 12, 1, 10, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2025, 12, 2, 12, 0, 0));
    }

    @Test
    void deserialize_WithNullDates_ShouldDeserializeSuccessfully() throws Exception {

        String jsonContent = "{\"itemId\": 1, \"start\": null, \"end\": null}";

        BookingRequestDto result = json.parse(jsonContent).getObject();

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isNull();
        assertThat(result.getEnd()).isNull();
    }

    @Test
    void deserialize_WithMissingFields_ShouldDeserializeWithNullValues() throws Exception {

        String jsonContent = "{}";

        BookingRequestDto result = json.parse(jsonContent).getObject();

        assertThat(result.getItemId()).isNull();
        assertThat(result.getStart()).isNull();
        assertThat(result.getEnd()).isNull();
    }

    @Test
    void serialize_WithNullValues_ShouldIncludeNullFields() throws Exception {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(null, null, null);

        JsonContent<BookingRequestDto> result = json.write(bookingRequestDto);

        assertThat(result).extractingJsonPathValue("$.itemId").isNull();
        assertThat(result).extractingJsonPathValue("$.start").isNull();
        assertThat(result).extractingJsonPathValue("$.end").isNull();
    }

    @Test
    void deserialize_WithCustomDateTimeFormat_ShouldHandleCorrectly() throws Exception {

        String jsonContent = "{\"itemId\": 1, \"start\": \"2025-12-01T10:00:00.123456\", \"end\": \"2025-12-02T12:00:00.987654\"}";

        BookingRequestDto result = json.parse(jsonContent).getObject();

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2025, 12, 1, 10, 0, 0, 123456000));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2025, 12, 2, 12, 0, 0, 987654000));
    }

    @Test
    void roundTrip_ShouldPreserveAllData() throws Exception {

        LocalDateTime start = LocalDateTime.of(2025, 12, 1, 10, 30, 45);
        LocalDateTime end = LocalDateTime.of(2025, 12, 2, 14, 15, 30);
        BookingRequestDto original = new BookingRequestDto(42L, start, end);

        String jsonString = json.write(original).getJson();
        BookingRequestDto deserialized = json.parse(jsonString).getObject();

        assertThat(deserialized.getItemId()).isEqualTo(original.getItemId());
        assertThat(deserialized.getStart()).isEqualTo(original.getStart());
        assertThat(deserialized.getEnd()).isEqualTo(original.getEnd());
    }
}