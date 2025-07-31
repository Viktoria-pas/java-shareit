package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> parametersCaptor;

    private BookingClient bookingClient;
    private final String serverUrl = "http://localhost:8080";
    private final Long userId = 1L;
    private final Long bookingId = 1L;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        bookingClient = new BookingClient(serverUrl, restTemplateBuilder);
    }

    @Test
    void createBooking_ShouldMakeCorrectPostRequest() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = bookingClient.createBooking(userId, bookingRequestDto);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(urlCaptor.getValue()).isEqualTo(serverUrl + "/bookings");
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(httpEntityCaptor.getValue().getBody()).isEqualTo(bookingRequestDto);
        assertThat(httpEntityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void updateBookingStatus_WithApprovedTrue_ShouldMakeCorrectPatchRequest() {

        Boolean approved = true;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = bookingClient.updateBookingStatus(userId, bookingId, approved);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                eq(Object.class),
                parametersCaptor.capture()
        );

        assertThat(urlCaptor.getValue()).contains(serverUrl + "/bookings");
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.PATCH);
        assertThat(httpEntityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(parametersCaptor.getValue()).containsEntry("approved", true);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void updateBookingStatus_WithApprovedFalse_ShouldMakeCorrectPatchRequest() {

        Boolean approved = false;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = bookingClient.updateBookingStatus(userId, bookingId, approved);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class), parametersCaptor.capture());
        assertThat(parametersCaptor.getValue()).containsEntry("approved", false);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void getBookingById_ShouldMakeCorrectGetRequest() {

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = bookingClient.getBookingById(userId, bookingId);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(urlCaptor.getValue()).contains(serverUrl + "/bookings");
        assertThat(urlCaptor.getValue()).contains("/" + bookingId);
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(httpEntityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void getUserBookings_WithDefaultParameters_ShouldMakeCorrectGetRequest() {

        String state = "ALL";
        Integer from = 0;
        Integer size = 10;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = bookingClient.getUserBookings(userId, state, from, size);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                eq(Object.class),
                parametersCaptor.capture()
        );

        assertThat(urlCaptor.getValue()).contains(serverUrl + "/bookings");
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(httpEntityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(parametersCaptor.getValue())
                .containsEntry("state", "ALL")
                .containsEntry("from", 0)
                .containsEntry("size", 10);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void getUserBookings_WithCustomParameters_ShouldMakeCorrectGetRequest() {

        String state = "WAITING";
        Integer from = 5;
        Integer size = 20;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = bookingClient.getUserBookings(userId, state, from, size);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), parametersCaptor.capture());
        assertThat(parametersCaptor.getValue())
                .containsEntry("state", "WAITING")
                .containsEntry("from", 5)
                .containsEntry("size", 20);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void getOwnerBookings_WithDefaultParameters_ShouldMakeCorrectGetRequest() {

        String state = "ALL";
        Integer from = 0;
        Integer size = 10;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = bookingClient.getOwnerBookings(userId, state, from, size);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                eq(Object.class),
                parametersCaptor.capture()
        );

        assertThat(urlCaptor.getValue()).contains(serverUrl + "/bookingsowner");
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(httpEntityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(parametersCaptor.getValue())
                .containsEntry("state", "ALL")
                .containsEntry("from", 0)
                .containsEntry("size", 10);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void getOwnerBookings_WithCustomParameters_ShouldMakeCorrectGetRequest() {

        String state = "APPROVED";
        Integer from = 10;
        Integer size = 5;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = bookingClient.getOwnerBookings(userId, state, from, size);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), parametersCaptor.capture());
        assertThat(parametersCaptor.getValue())
                .containsEntry("state", "APPROVED")
                .containsEntry("from", 10)
                .containsEntry("size", 5);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void createBooking_ShouldIncludeCorrectHeaders() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        bookingClient.createBooking(userId, bookingRequestDto);

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), httpEntityCaptor.capture(), eq(Object.class));
        HttpHeaders headers = httpEntityCaptor.getValue().getHeaders();

        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.getAccept()).containsExactly(MediaType.APPLICATION_JSON);
        assertThat(headers.get("X-Sharer-User-Id")).containsExactly("1");
    }
}
