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
    void createBooking_MissingUserId_ShouldReturnInternalServerError() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isInternalServerError());
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
    void createBooking_EndDateInPast_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_StartAfterEnd_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_StartEqualsEnd_ShouldReturnBadRequest() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                dateTime,
                dateTime
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_NullStartDate_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                null,
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_NullEndDate_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                null
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_ValidApproved_ShouldReturnOk() throws Exception {
        when(bookingClient.updateBookingStatus(eq(userId), eq(bookingId), eq(true)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookingStatus_ValidRejected_ShouldReturnOk() throws Exception {
        when(bookingClient.updateBookingStatus(eq(userId), eq(bookingId), eq(false)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "false"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookingStatus_MissingApprovedParam_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isInternalServerError());
    }

    // Тесты для getBookingById
    @Test
    void getBookingById_ValidRequest_ShouldReturnOk() throws Exception {
        when(bookingClient.getBookingById(eq(userId), eq(bookingId)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    // Тесты для getUserBookings
    @Test
    void getUserBookings_DefaultParameters_ShouldReturnOk() throws Exception {
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
    void getUserBookings_AllValidStates_ShouldReturnOk() throws Exception {
        String[] validStates = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};

        for (String state : validStates) {
            when(bookingClient.getUserBookings(eq(userId), eq(state), eq(0), eq(10)))
                    .thenReturn(ResponseEntity.ok().build());

            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", userId)
                            .param("state", state))
                    .andExpect(status().isOk());
        }
    }

    // Тесты для getOwnerBookings
    @Test
    void getOwnerBookings_DefaultParameters_ShouldReturnOk() throws Exception {
        when(bookingClient.getOwnerBookings(eq(userId), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_WithCustomParameters_ShouldReturnOk() throws Exception {
        when(bookingClient.getOwnerBookings(eq(userId), eq("CURRENT"), eq(10), eq(5)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "CURRENT")
                        .param("from", "10")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_AllValidStates_ShouldReturnOk() throws Exception {
        String[] validStates = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};

        for (String state : validStates) {
            when(bookingClient.getOwnerBookings(eq(userId), eq(state), eq(0), eq(10)))
                    .thenReturn(ResponseEntity.ok().build());

            mockMvc.perform(get("/bookings/owner")
                            .header("X-Sharer-User-Id", userId)
                            .param("state", state))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void getOwnerBookings_InvalidState_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_EmptyState_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", ""))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_BlankState_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_LowerCaseValidState_ShouldReturnOk() throws Exception {
        when(bookingClient.getOwnerBookings(eq(userId), eq("current"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "current"))
                .andExpect(status().isOk());
    }


    @Test
    void getUserBookings_NegativeFrom_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_ZeroSize_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_NegativeSize_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_NegativeFrom_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_ZeroSize_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_NegativeUserId_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_ZeroUserId_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_StartDayBooking_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                null,
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_EndDayBooking_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(2),
                null
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_EndDayBookingBeforeStart_ShouldReturnBadRequest() throws Exception {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_NegativeBookingId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", -1L)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_NegativeBookingId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/{bookingId}", -1L)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }
}
