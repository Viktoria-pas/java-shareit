package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long userId = 1L;
    private final Long bookingId = 1L;

    @Test
    void createBooking_ValidRequest_ShouldReturnOk() throws Exception {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingClient.createBooking(eq(userId), any(BookingRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_MissingUserId_ShouldReturnBadRequest() throws Exception {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_InvalidItemId_ShouldReturnBadRequest() throws Exception {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_StartDateInPast_ShouldReturnBadRequest() throws Exception {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_EndDateNotInFuture_ShouldReturnBadRequest() throws Exception {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1) // Past date
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_ValidRequest_ShouldReturnOk() throws Exception {

        when(bookingClient.updateBookingStatus(eq(userId), eq(bookingId), eq(true)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookingStatus_MissingApprovedParam_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_ValidRequest_ShouldReturnOk() throws Exception {

        when(bookingClient.getBookingById(eq(userId), eq(bookingId)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }


    @Test
    void getUserBookings_ValidRequest_ShouldReturnOk() throws Exception {

        when(bookingClient.getUserBookings(eq(userId), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_WithCustomParameters_ShouldReturnOk() throws Exception {

        when(bookingClient.getUserBookings(eq(userId), eq("WAITING"), eq(5), eq(20)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_ValidRequest_ShouldReturnOk() throws Exception {

        when(bookingClient.getOwnerBookings(eq(userId), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_WithCustomParameters_ShouldReturnOk() throws Exception {
        // Given
        when(bookingClient.getOwnerBookings(eq(userId), eq("APPROVED"), eq(10), eq(5)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "APPROVED")
                        .param("from", "10")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }
}
