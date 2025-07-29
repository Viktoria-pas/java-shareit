package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setName("Test User 1");
        testUser1.setEmail("test1@example.com");
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User();
        testUser2.setName("Test User 2");
        testUser2.setEmail("test2@example.com");
        testUser2 = userRepository.save(testUser2);
    }

    @Test
    void createRequest_ShouldCreateAndReturnRequest() {
        // Given
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("Нужна дрель");

        // When
        ItemRequestDto result = itemRequestService.createRequest(createDto, testUser1.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getRequestorId()).isEqualTo(testUser1.getId());
        assertThat(result.getCreated()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(result.getItems()).isEmpty();

        // Verify in database
        ItemRequest savedRequest = itemRequestRepository.findById(result.getId()).orElse(null);
        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getDescription()).isEqualTo("Нужна дрель");
        assertThat(savedRequest.getRequestorId()).isEqualTo(testUser1.getId());
    }

    @Test
    void getUserRequests_WithExistingRequests_ShouldReturnUserRequestsOrderedByCreatedDesc() {
        // Given
        ItemRequest oldRequest = createTestRequest("Старый запрос", testUser1.getId(), LocalDateTime.now().minusDays(2));
        ItemRequest newRequest = createTestRequest("Новый запрос", testUser1.getId(), LocalDateTime.now().minusDays(1));
        ItemRequest otherUserRequest = createTestRequest("Запрос другого пользователя", testUser2.getId(), LocalDateTime.now());

        itemRequestRepository.save(oldRequest);
        itemRequestRepository.save(newRequest);
        itemRequestRepository.save(otherUserRequest);

        // When
        List<ItemRequestDto> result = itemRequestService.getUserRequests(testUser1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Новый запрос");
        assertThat(result.get(1).getDescription()).isEqualTo("Старый запрос");
        assertThat(result).allMatch(dto -> dto.getRequestorId().equals(testUser1.getId()));
    }

    @Test
    void getUserRequests_WithNoRequests_ShouldReturnEmptyList() {
        // When
        List<ItemRequestDto> result = itemRequestService.getUserRequests(testUser1.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequestsWithPagination() {
        // Given
        ItemRequest user1Request = createTestRequest("Запрос пользователя 1", testUser1.getId(), LocalDateTime.now().minusDays(3));
        ItemRequest user2Request1 = createTestRequest("Запрос пользователя 2 - 1", testUser2.getId(), LocalDateTime.now().minusDays(2));
        ItemRequest user2Request2 = createTestRequest("Запрос пользователя 2 - 2", testUser2.getId(), LocalDateTime.now().minusDays(1));

        itemRequestRepository.save(user1Request);
        itemRequestRepository.save(user2Request1);
        itemRequestRepository.save(user2Request2);

        // When - получаем запросы от имени user1 (должны вернуться запросы user2)
        List<ItemRequestDto> result = itemRequestService.getAllRequests(testUser1.getId(), 0, 10);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Запрос пользователя 2 - 2"); // новый первым
        assertThat(result.get(1).getDescription()).isEqualTo("Запрос пользователя 2 - 1");
        assertThat(result).allMatch(dto -> dto.getRequestorId().equals(testUser2.getId()));
        assertThat(result).allMatch(dto -> dto.getItems() == null); // для getAllRequests items должны быть null
    }

    @Test
    void getAllRequests_WithPagination_ShouldRespectFromAndSize() {
        // Given
        for (int i = 0; i < 5; i++) {
            ItemRequest request = createTestRequest("Запрос " + i, testUser2.getId(), LocalDateTime.now().minusDays(i));
            itemRequestRepository.save(request);
        }

        // When
        List<ItemRequestDto> result = itemRequestService.getAllRequests(testUser1.getId(), 2, 2);

        // Then
        assertThat(result).hasSize(2);
        // Проверяем, что пропустили первые 2 записи и взяли следующие 2
    }

    @Test
    void getRequestById_WithExistingRequest_ShouldReturnRequest() {
        // Given
        ItemRequest request = createTestRequest("Тестовый запрос", testUser1.getId(), LocalDateTime.now());
        ItemRequest savedRequest = itemRequestRepository.save(request);

        // When
        ItemRequestDto result = itemRequestService.getRequestById(savedRequest.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedRequest.getId());
        assertThat(result.getDescription()).isEqualTo("Тестовый запрос");
        assertThat(result.getRequestorId()).isEqualTo(testUser1.getId());
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getRequestById_WithNonExistingRequest_ShouldThrowNotFoundException() {
        // Given
        Long nonExistingId = 999L;

        // When & Then
        assertThatThrownBy(() -> itemRequestService.getRequestById(nonExistingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Запрос с id 999 не найден");
    }

    private ItemRequest createTestRequest(String description, Long requestorId, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestorId(requestorId);
        request.setCreated(created);
        return request;
    }
}