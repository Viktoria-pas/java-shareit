package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> methodCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> entityCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> parametersCaptor;

    private BaseClient baseClient;
    private final String baseUrl = "http://localhost:8080/api";

    // Concrete implementation for testing
    private static class TestBaseClient extends BaseClient {
        public TestBaseClient(RestTemplate rest, String baseUrl) {
            super(rest, baseUrl);
        }

        // Expose protected methods for testing
        public ResponseEntity<Object> testGet(String path) {
            return get(path);
        }

        public ResponseEntity<Object> testGet(String path, long userId) {
            return get(path, userId);
        }

        public ResponseEntity<Object> testGet(String path, Long userId, Map<String, Object> parameters) {
            return get(path, userId, parameters);
        }

        public <T> ResponseEntity<Object> testPost(String path, T body) {
            return post(path, body);
        }

        public <T> ResponseEntity<Object> testPost(String path, long userId, T body) {
            return post(path, userId, body);
        }

        public <T> ResponseEntity<Object> testPost(String path, Long userId, Map<String, Object> parameters, T body) {
            return post(path, userId, parameters, body);
        }

        public <T> ResponseEntity<Object> testPut(String path, long userId, T body) {
            return put(path, userId, body);
        }

        public <T> ResponseEntity<Object> testPut(String path, long userId, Map<String, Object> parameters, T body) {
            return put(path, userId, parameters, body);
        }

        public <T> ResponseEntity<Object> testPatch(String path, T body) {
            return patch(path, body);
        }

        public <T> ResponseEntity<Object> testPatch(String path, long userId) {
            return patch(path, userId);
        }

        public <T> ResponseEntity<Object> testPatch(String path, long userId, T body) {
            return patch(path, userId, body);
        }

        public <T> ResponseEntity<Object> testPatch(String path, Long userId, Map<String, Object> parameters, T body) {
            return patch(path, userId, parameters, body);
        }

        public ResponseEntity<Object> testDelete(String path) {
            return delete(path);
        }

        public ResponseEntity<Object> testDelete(String path, long userId) {
            return delete(path, userId);
        }

        public ResponseEntity<Object> testDelete(String path, Long userId, Map<String, Object> parameters) {
            return delete(path, userId, parameters);
        }
    }

    @BeforeEach
    void setUp() {
        baseClient = new TestBaseClient(restTemplate, baseUrl);
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {

        BaseClient client = new TestBaseClient(restTemplate, baseUrl);

        assertThat(client).isNotNull();
    }

    @Test
    void get_WithPathOnly_ShouldMakeCorrectRequest() {

        String path = "/test";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testGet(path);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                entityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl + "test");
        assertThat(methodCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).isNull();
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void get_WithPathAndUserId_ShouldIncludeUserIdHeader() {

        String path = "/test";
        long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testGet(path, userId);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class));
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void get_WithParameters_ShouldPassParametersToRestTemplate() {

        String path = "/test";
        Long userId = 1L;
        Map<String, Object> parameters = Map.of("param1", "value1", "param2", 2);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testGet(path, userId, parameters);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                parametersCaptor.capture()
        );
        assertThat(parametersCaptor.getValue()).isEqualTo(parameters);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void post_WithBodyOnly_ShouldMakeCorrectRequest() {

        String path = "/test";
        String body = "test body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testPost(path, body);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                entityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl + "test");
        assertThat(methodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(entityCaptor.getValue().getBody()).isEqualTo(body);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void post_WithUserIdAndBody_ShouldIncludeUserIdHeader() {

        String path = "/test";
        long userId = 1L;
        String body = "test body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testPost(path, userId, body);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class));
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(entityCaptor.getValue().getBody()).isEqualTo(body);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void put_ShouldMakeCorrectRequest() {

        String path = "/test";
        long userId = 1L;
        String body = "test body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testPut(path, userId, body);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                entityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl + "test");
        assertThat(methodCaptor.getValue()).isEqualTo(HttpMethod.PUT);
        assertThat(entityCaptor.getValue().getBody()).isEqualTo(body);
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void patch_WithBodyOnly_ShouldMakeCorrectRequest() {

        String path = "/test";
        String body = "test body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testPatch(path, body);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                entityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl + "test");
        assertThat(methodCaptor.getValue()).isEqualTo(HttpMethod.PATCH);
        assertThat(entityCaptor.getValue().getBody()).isEqualTo(body);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void patch_WithUserIdOnly_ShouldMakeCorrectRequest() {

        String path = "/test";
        long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testPatch(path, userId);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.PATCH), entityCaptor.capture(), eq(Object.class));
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(entityCaptor.getValue().getBody()).isNull();
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void delete_WithPathOnly_ShouldMakeCorrectRequest() {

        String path = "/test";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testDelete(path);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                entityCaptor.capture(),
                eq(Object.class)
        );

        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl + "test");
        assertThat(methodCaptor.getValue()).isEqualTo(HttpMethod.DELETE);
        assertThat(entityCaptor.getValue().getBody()).isNull();
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void makeAndSendRequest_WithLeadingSlashInPath_ShouldRemoveSlash() {

        String path = "/test/path";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ((TestBaseClient) baseClient).testGet(path);

        verify(restTemplate).exchange(urlCaptor.capture(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl + "test/path");
    }

    @Test
    void makeAndSendRequest_WithoutLeadingSlashInPath_ShouldKeepPathAsIs() {

        String path = "test/path";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ((TestBaseClient) baseClient).testGet(path);

        verify(restTemplate).exchange(urlCaptor.capture(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl + "test/path");
    }

    @Test
    void makeAndSendRequest_ShouldSetCorrectHeaders() {

        String path = "/test";
        Long userId = 42L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ((TestBaseClient) baseClient).testGet(path, userId, null);

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), entityCaptor.capture(), eq(Object.class));
        HttpHeaders headers = entityCaptor.getValue().getHeaders();

        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.getAccept()).containsExactly(MediaType.APPLICATION_JSON);
        assertThat(headers.get("X-Sharer-User-Id")).containsExactly("42");
    }

    @Test
    void makeAndSendRequest_WithHttpClientErrorException_ShouldReturnErrorResponse() {

        String path = "/test";
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = ((TestBaseClient) baseClient).testGet(path);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(exception.getResponseBodyAsByteArray());
    }

    @Test
    void makeAndSendRequest_WithNullParameters_ShouldCallExchangeWithoutParameters() {

        String path = "/test";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ((TestBaseClient) baseClient).testGet(path, null, null);

        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }
    @Test
    void patch_WithUserIdAndParametersAndBody_ShouldMakeCorrectRequest() {
        String path = "/test";
        long userId = 1L;
        Map<String, Object> parameters = Map.of("param1", "value1");
        String body = "test body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testPatch(path, userId, parameters, body);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.PATCH),
                entityCaptor.capture(),
                eq(Object.class),
                parametersCaptor.capture()
        );
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(entityCaptor.getValue().getBody()).isEqualTo(body);
        assertThat(parametersCaptor.getValue()).isEqualTo(parameters);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void put_WithParameters_ShouldPassParametersToRestTemplate() {
        String path = "/test";
        long userId = 1L;
        Map<String, Object> parameters = Map.of("param1", "value1");
        String body = "test body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testPut(path, userId, parameters, body);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Object.class),
                parametersCaptor.capture()
        );
        assertThat(parametersCaptor.getValue()).isEqualTo(parameters);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void delete_WithUserIdAndParameters_ShouldMakeCorrectRequest() {
        String path = "/test";
        long userId = 1L;
        Map<String, Object> parameters = Map.of("param1", "value1");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testDelete(path, userId, parameters);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                entityCaptor.capture(),
                eq(Object.class),
                parametersCaptor.capture()
        );
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(parametersCaptor.getValue()).isEqualTo(parameters);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void makeAndSendRequest_WithEmptyPath_ShouldHandleCorrectly() {
        String path = "";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testGet(path);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        );
        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }
    @Test
    void delete_WithUserId_ShouldCallDeleteWithParameters() {
        String path = "/test";
        long userId = 1L;
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testDelete(path, userId);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                entityCaptor.capture(),
                eq(Object.class)
        );
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void patch_WithUserIdAndBody_ShouldCallPatchWithParameters() {
        String path = "/test";
        long userId = 1L;
        String body = "test body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = ((TestBaseClient) baseClient).testPatch(path, userId, body);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.PATCH),
                entityCaptor.capture(),
                eq(Object.class)
        );
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id")).containsExactly("1");
        assertThat(entityCaptor.getValue().getBody()).isEqualTo(body);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void prepareGatewayResponse_WithReflection() throws Exception {

        Method method = BaseClient.class.getDeclaredMethod("prepareGatewayResponse", ResponseEntity.class);
        method.setAccessible(true);

        BaseClient client = new BaseClient(restTemplate, baseUrl);

        ResponseEntity<Object> success = ResponseEntity.ok("success");
        ResponseEntity<Object> result = (ResponseEntity<Object>) method.invoke(client, success);
        assertThat(result).isSameAs(success);

        ResponseEntity<Object> error = ResponseEntity.badRequest().body("error");
        result = (ResponseEntity<Object>) method.invoke(client, error);
        assertThat(result.getBody()).isEqualTo("error");
    }


}