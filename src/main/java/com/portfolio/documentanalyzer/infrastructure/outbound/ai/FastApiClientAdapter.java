package com.portfolio.documentanalyzer.infrastructure.outbound.ai;

import com.portfolio.documentanalyzer.domain.exception.AiServiceIntegrationException;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexResponseDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryResponseDto;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.function.Supplier;

@Getter
@Component
public class FastApiClientAdapter {

    private static final Logger log = LoggerFactory.getLogger(FastApiClientAdapter.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    private final RestClient restClient;

    @Autowired
    public FastApiClientAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${ai-service.url:http://ai-service:8000}") String baseUrl
    ) {
        this(restClientBuilder, baseUrl, createDefaultRequestFactory());
    }

    FastApiClientAdapter(
            RestClient.Builder restClientBuilder,
            String baseUrl,
            ClientHttpRequestFactory requestFactory
    ) {
        if (requestFactory != null) {
            restClientBuilder.requestFactory(requestFactory);
        }
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    private static ClientHttpRequestFactory createDefaultRequestFactory() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(READ_TIMEOUT);
        return factory;
    }

    public boolean checkHealth() {
        try {
            ResponseEntity<Void> response = restClient.get()
                    .uri("/api/v1/health")
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode().isSameCodeAs(HttpStatus.OK);
        } catch (Exception e) {
            log.warn("Health check failed for AI service: {}", e.getMessage());
            return false;
        }
    }

    public AiDocumentIndexResponseDto indexDocument(AiDocumentIndexRequestDto request) {
        return executeWithHandling(() -> restClient.post()
                .uri("/api/v1/documents/index")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiDocumentIndexResponseDto.class));
    }

    public AiRagQueryResponseDto queryRag(AiRagQueryRequestDto request) {
        return executeWithHandling(() -> restClient.post()
                .uri("/api/v1/rag/query")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiRagQueryResponseDto.class));
    }

    private <T> T executeWithHandling(Supplier<T> action) {
        try {
            return action.get();
        } catch (ResourceAccessException e) {
            log.error("Error de comunicación/timeout con el servicio de IA: {}", e.getMessage(), e);
            if (isTimeout(e)) {
                throw new AiServiceIntegrationException(
                        AiServiceIntegrationException.ERROR_AI_TIMEOUT,
                        "El servicio de IA ha superado el tiempo límite de espera."
                );
            }
            throw new AiServiceIntegrationException(
                    AiServiceIntegrationException.ERROR_AI_PROCESSING,
                    "No se pudo establecer conexión con el servicio de IA."
            );
        } catch (RestClientResponseException e) {
            log.error("Error HTTP {} devuelto por el servicio de IA", e.getStatusCode(), e);
            if (e.getStatusCode().value() == 504) {
                throw new AiServiceIntegrationException(
                        AiServiceIntegrationException.ERROR_AI_TIMEOUT,
                        "El servicio de IA ha superado el tiempo límite de espera."
                );
            }
            throw new AiServiceIntegrationException(
                    AiServiceIntegrationException.ERROR_AI_PROCESSING,
                    "Error al procesar la solicitud en el servicio de IA."
            );
        } catch (AiServiceIntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al comunicarse con el servicio de IA: {}", e.getMessage(), e);
            throw new AiServiceIntegrationException(
                    AiServiceIntegrationException.ERROR_AI_PROCESSING,
                    "Ocurrió un error inesperado al procesar la solicitud en el servicio de IA."
            );
        }
    }

    private boolean isTimeout(ResourceAccessException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof java.net.SocketTimeoutException
                    || cause instanceof java.net.http.HttpTimeoutException
                    || cause instanceof java.util.concurrent.TimeoutException
                    || cause.getClass().getSimpleName().toLowerCase().contains("timeout")) {
                return true;
            }
            cause = cause.getCause();
        }
        String msg = e.getMessage();
        return msg != null && msg.toLowerCase().contains("timeout");
    }
}
