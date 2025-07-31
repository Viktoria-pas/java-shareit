package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_REQUEST_ID = 1L;

    @Test
    void createRequest_WithValidData_ShouldReturnCreatedRequest() throws Exception {

        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("Нужна дрель");

        ItemRequestDto responseDto = new ItemRequestDto();
        responseDto.setId(TEST_REQUEST_ID);
        responseDto.setDescription("Нужна дрель");
        responseDto.setRequestorId(TEST_USER_ID);
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(List.of());

        when(itemRequestService.createRequest(any(ItemRequestCreateDto.class), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_REQUEST_ID))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.requestorId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void createRequest_WithBlankDescription() throws Exception {

        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createRequest_WithTooLongDescription_ShouldReturnBadRequest() throws Exception {

        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("a".repeat(513)); // больше 512 символов

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() throws Exception {

        ItemResponseDto itemResponse = new ItemResponseDto(1L, "Дрель", 2L);

        ItemRequestDto requestDto1 = new ItemRequestDto();
        requestDto1.setId(1L);
        requestDto1.setDescription("Первый запрос");
        requestDto1.setRequestorId(TEST_USER_ID);
        requestDto1.setCreated(LocalDateTime.now());
        requestDto1.setItems(List.of(itemResponse));

        ItemRequestDto requestDto2 = new ItemRequestDto();
        requestDto2.setId(2L);
        requestDto2.setDescription("Второй запрос");
        requestDto2.setRequestorId(TEST_USER_ID);
        requestDto2.setCreated(LocalDateTime.now().minusDays(1));
        requestDto2.setItems(List.of());

        when(itemRequestService.getUserRequests(TEST_USER_ID))
                .thenReturn(List.of(requestDto1, requestDto2));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Первый запрос"))
                .andExpect(jsonPath("$[0].items").isArray())
                .andExpect(jsonPath("$[0].items.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].id").value(1))
                .andExpect(jsonPath("$[0].items[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Второй запрос"))
                .andExpect(jsonPath("$[1].items").isEmpty());
    }

    @Test
    void getAllRequests_WithDefaultParameters_ShouldReturnAllRequests() throws Exception {

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Запрос другого пользователя");
        requestDto.setRequestorId(2L);
        requestDto.setCreated(LocalDateTime.now());
        requestDto.setItems(null);

        when(itemRequestService.getAllRequests(TEST_USER_ID, 0, 20))
                .thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Запрос другого пользователя"))
                .andExpect(jsonPath("$[0].requestorId").value(2))
                .andExpect(jsonPath("$[0].items").doesNotExist());
    }

    @Test
    void getAllRequests_WithCustomParameters_ShouldPassParametersToService() throws Exception {

        when(itemRequestService.getAllRequests(TEST_USER_ID, 10, 5))
                .thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .param("from", "10")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getRequestById_WithExistingRequest_ShouldReturnRequest() throws Exception {

        ItemResponseDto itemResponse = new ItemResponseDto(1L, "Дрель", 2L);

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(TEST_REQUEST_ID);
        requestDto.setDescription("Нужна дрель");
        requestDto.setRequestorId(2L);
        requestDto.setCreated(LocalDateTime.now());
        requestDto.setItems(List.of(itemResponse));

        when(itemRequestService.getRequestById(TEST_REQUEST_ID))
                .thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", TEST_REQUEST_ID)
                        .header(USER_ID_HEADER, TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_REQUEST_ID))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.requestorId").value(2))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Дрель"))
                .andExpect(jsonPath("$.items[0].ownerId").value(2));
    }
}