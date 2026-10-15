package com.portfolio.documentanalyzer.infrastructure.outbound.ai;

import com.portfolio.documentanalyzer.domain.exception.AiServiceIntegrationException;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexResponseDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryResponseDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.MessageRoleDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FastApiClientAdapterTest {

    private static final String BASE_URL = "http://localhost:8000";

    @Test
    @DisplayName("Debe instanciar FastApiClientAdapter correctamente con RestClient.Builder y baseUrl")
    void shouldInitializeWithRestClientBuilderAndBaseUrl() {
        RestClient.Builder builder = RestClient.builder();

        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL);

        assertThat(adapter.getRestClient()).isNotNull();
    }

    @Test
    @DisplayName("checkHealth() retorna true cuando el servicio responde HTTP 200")
    void checkHealthShouldReturnTrueWhen200Ok() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        server.expect(requestTo(BASE_URL + "/api/v1/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        boolean isHealthy = adapter.checkHealth();

        assertThat(isHealthy).isTrue();
        server.verify();
    }

    @Test
    @DisplayName("checkHealth() retorna false cuando el servicio responde con error de servidor (HTTP 500)")
    void checkHealthShouldReturnFalseWhenServerError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        server.expect(requestTo(BASE_URL + "/api/v1/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        boolean isHealthy = adapter.checkHealth();

        assertThat(isHealthy).isFalse();
        server.verify();
    }

    @Test
    @DisplayName("checkHealth() retorna false cuando el servicio responde con código distinto de 200")
    void checkHealthShouldReturnFalseWhenNot200() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        server.expect(requestTo(BASE_URL + "/api/v1/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        boolean isHealthy = adapter.checkHealth();

        assertThat(isHealthy).isFalse();
        server.verify();
    }

    @Test
    @DisplayName("checkHealth() retorna false cuando ocurre una excepción o error de conexión")
    void checkHealthShouldReturnFalseOnException() {
        RestClient.Builder builder = RestClient.builder();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, "http://invalid-host-unreachable:9999", null);

        boolean isHealthy = adapter.checkHealth();

        assertThat(isHealthy).isFalse();
    }

    @Test
    @DisplayName("indexDocument() realiza POST a /api/v1/documents/index y retorna AiDocumentIndexResponseDto")
    void indexDocumentShouldPostAndReturnResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        String responseJson = """
                {
                    "status": "SUCCESS",
                    "data": {
                        "document_id": "doc-123",
                        "chunks_processed": 5
                    }
                }
                """;

        server.expect(requestTo(BASE_URL + "/api/v1/documents/index"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.document_id").value("doc-123"))
                .andExpect(jsonPath("$.content").value("Texto de prueba"))
                .andExpect(jsonPath("$.metadata.filename").value("test.pdf"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AiDocumentIndexRequestDto request = new AiDocumentIndexRequestDto(
                "doc-123",
                "Texto de prueba",
                Map.of("filename", "test.pdf")
        );
        AiDocumentIndexResponseDto response = adapter.indexDocument(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.data()).isNotNull();
        assertThat(response.data().documentId()).isEqualTo("doc-123");
        assertThat(response.data().chunksProcessed()).isEqualTo(5);
        server.verify();
    }

    @Test
    @DisplayName("queryRag() realiza POST a /api/v1/rag/query y retorna AiRagQueryResponseDto")
    void queryRagShouldPostAndReturnResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        String responseJson = """
                {
                    "status": "SUCCESS",
                    "data": {
                        "answer": "Respuesta generada",
                        "sources": ["source1", "source2"]
                    }
                }
                """;

        server.expect(requestTo(BASE_URL + "/api/v1/rag/query"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.query").value("¿Cuál es el resumen?"))
                .andExpect(jsonPath("$.conversation_id").value("conv-456"))
                .andExpect(jsonPath("$.history[0].role").value("USER"))
                .andExpect(jsonPath("$.history[0].content").value("Hola"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AiRagQueryRequestDto request = new AiRagQueryRequestDto(
                "¿Cuál es el resumen?",
                "conv-456",
                List.of(MessageRoleDto.user("Hola"))
        );
        AiRagQueryResponseDto response = adapter.queryRag(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.data()).isNotNull();
        assertThat(response.data().answer()).isEqualTo("Respuesta generada");
        assertThat(response.data().sources()).containsExactly("source1", "source2");
        server.verify();
    }

    @Test
    @DisplayName("indexDocument() lanza AiServiceIntegrationException con AI_PROCESSING_ERROR en error HTTP 500")
    void indexDocumentShouldThrowAiServiceIntegrationExceptionOnHttp500() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        server.expect(requestTo(BASE_URL + "/api/v1/documents/index"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        AiDocumentIndexRequestDto request = new AiDocumentIndexRequestDto("doc-123", "Texto");

        assertThatThrownBy(() -> adapter.indexDocument(request))
                .isInstanceOf(AiServiceIntegrationException.class)
                .satisfies(ex -> {
                    AiServiceIntegrationException aiEx = (AiServiceIntegrationException) ex;
                    assertThat(aiEx.getErrorCode()).isEqualTo(AiServiceIntegrationException.ERROR_AI_PROCESSING);
                    assertThat(aiEx.getMessage()).isEqualTo("Error al procesar la solicitud en el servicio de IA.");
                });

        server.verify();
    }

    @Test
    @DisplayName("queryRag() lanza AiServiceIntegrationException con AI_PROCESSING_ERROR en error HTTP 400")
    void queryRagShouldThrowAiServiceIntegrationExceptionOnHttp400() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        server.expect(requestTo(BASE_URL + "/api/v1/rag/query"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        AiRagQueryRequestDto request = new AiRagQueryRequestDto("pregunta", "conv-1");

        assertThatThrownBy(() -> adapter.queryRag(request))
                .isInstanceOf(AiServiceIntegrationException.class)
                .satisfies(ex -> {
                    AiServiceIntegrationException aiEx = (AiServiceIntegrationException) ex;
                    assertThat(aiEx.getErrorCode()).isEqualTo(AiServiceIntegrationException.ERROR_AI_PROCESSING);
                    assertThat(aiEx.getMessage()).isEqualTo("Error al procesar la solicitud en el servicio de IA.");
                });

        server.verify();
    }

    @Test
    @DisplayName("indexDocument() lanza AiServiceIntegrationException con AI_TIMEOUT en error HTTP 504")
    void indexDocumentShouldThrowAiTimeoutOnHttp504() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        server.expect(requestTo(BASE_URL + "/api/v1/documents/index"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));

        AiDocumentIndexRequestDto request = new AiDocumentIndexRequestDto("doc-123", "Texto");

        assertThatThrownBy(() -> adapter.indexDocument(request))
                .isInstanceOf(AiServiceIntegrationException.class)
                .satisfies(ex -> {
                    AiServiceIntegrationException aiEx = (AiServiceIntegrationException) ex;
                    assertThat(aiEx.getErrorCode()).isEqualTo(AiServiceIntegrationException.ERROR_AI_TIMEOUT);
                    assertThat(aiEx.getMessage()).isEqualTo("El servicio de IA ha superado el tiempo límite de espera.");
                });

        server.verify();
    }

    @Test
    @DisplayName("indexDocument() lanza AiServiceIntegrationException con AI_TIMEOUT en SocketTimeoutException")
    void indexDocumentShouldThrowAiTimeoutOnSocketTimeout() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        server.expect(requestTo(BASE_URL + "/api/v1/documents/index"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        AiDocumentIndexRequestDto request = new AiDocumentIndexRequestDto("doc-123", "Texto");

        assertThatThrownBy(() -> adapter.indexDocument(request))
                .isInstanceOf(AiServiceIntegrationException.class)
                .satisfies(ex -> {
                    AiServiceIntegrationException aiEx = (AiServiceIntegrationException) ex;
                    assertThat(aiEx.getErrorCode()).isEqualTo(AiServiceIntegrationException.ERROR_AI_TIMEOUT);
                    assertThat(aiEx.getMessage()).isEqualTo("El servicio de IA ha superado el tiempo límite de espera.");
                });

        server.verify();
    }

    @Test
    @DisplayName("queryRag() lanza AiServiceIntegrationException con AI_PROCESSING_ERROR en error de conexión")
    void queryRagShouldThrowAiProcessingErrorOnConnectionRefused() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FastApiClientAdapter adapter = new FastApiClientAdapter(builder, BASE_URL, null);

        server.expect(requestTo(BASE_URL + "/api/v1/rag/query"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new IOException("Connection refused")));

        AiRagQueryRequestDto request = new AiRagQueryRequestDto("pregunta", "conv-1");

        assertThatThrownBy(() -> adapter.queryRag(request))
                .isInstanceOf(AiServiceIntegrationException.class)
                .satisfies(ex -> {
                    AiServiceIntegrationException aiEx = (AiServiceIntegrationException) ex;
                    assertThat(aiEx.getErrorCode()).isEqualTo(AiServiceIntegrationException.ERROR_AI_PROCESSING);
                    assertThat(aiEx.getMessage()).isEqualTo("No se pudo establecer conexión con el servicio de IA.");
                });

        server.verify();
    }
}
