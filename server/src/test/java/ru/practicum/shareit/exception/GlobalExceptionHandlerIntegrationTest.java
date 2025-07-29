package ru.practicum.shareit.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({GlobalExceptionHandler.class, GlobalExceptionHandlerIntegrationTest.TestController.class})
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void handleValidationException_Returns400() throws Exception {
        mockMvc.perform(get("/test/custom-validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Кастомная ошибка валидации"));
    }

    @Test
    void handleNotFoundException_Returns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Объект не найден"))
                .andExpect(jsonPath("$.message").value("Тестовый объект не найден"));
    }

    @Test
    void handleConflictException_Returns409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Конфликт данных"));
    }

    @Test
    void handleGenericException_Returns500() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.message").value("Произошла непредвиденная ошибка"));
    }

    @Test
    void handleMethodArgumentNotValidException_Returns400() throws Exception {
        TestDto invalidDto = new TestDto("", "invalid-email");

        mockMvc.perform(post("/test/validation")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void handleMethodArgumentNotValidException_WithBlankName_Returns400() throws Exception {
        TestDto invalidDto = new TestDto("", "test@example.com");

        mockMvc.perform(post("/test/validation")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: Имя не может быть пустым"));
    }

    @Test
    void handleMethodArgumentNotValidException_WithInvalidEmail_Returns400() throws Exception {
        TestDto invalidDto = new TestDto("John", "invalid-email");

        mockMvc.perform(post("/test/validation")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: Некорректный формат email"));
    }

    @Test
    void handleMethodArgumentNotValidException_WithMultipleErrors_Returns400() throws Exception {
        TestDto invalidDto = new TestDto("", "invalid-email");

        mockMvc.perform(post("/test/validation")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
    @Test
    void handleConstraintViolationException_Returns400() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest());

    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/validation")
        public void testValidation(@Valid @RequestBody TestDto dto) {

        }

        @GetMapping("/custom-validation")
        public void testCustomValidation() {
            throw new ValidationException("Кастомная ошибка валидации");
        }

        @GetMapping("/not-found")
        public void testNotFound() {
            throw new NotFoundException("Тестовый объект не найден");
        }

        @GetMapping("/conflict")
        public void testConflict() {
            throw new ConflictException("Конфликт данных");
        }

        @GetMapping("/generic-error")
        public void testGenericError() {
            throw new RuntimeException("Неожиданная ошибка");
        }

        @GetMapping("/constraint-violation")
        public void testConstraintViolation() {
            throw new jakarta.validation.ConstraintViolationException("Constraint violation",
                    java.util.Set.of());
        }

    }

    static class TestDto {
        @NotBlank(message = "Имя не может быть пустым")
        private String name;

        @Email(message = "Некорректный формат email")
        private String email;

        public TestDto() {

        }

        public TestDto(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
