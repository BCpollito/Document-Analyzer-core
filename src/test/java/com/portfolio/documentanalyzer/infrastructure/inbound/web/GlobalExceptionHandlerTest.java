package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.domain.exception.AiServiceIntegrationException;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.ErrorResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleAiServiceIntegrationException retorna HTTP 503 ante AI_TIMEOUT")
    void shouldReturn503OnAiTimeout() {
        AiServiceIntegrationException exception = new AiServiceIntegrationException(
                AiServiceIntegrationException.ERROR_AI_TIMEOUT,
                "El servicio de IA ha superado el tiempo límite de espera."
        );

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleAiServiceIntegrationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("error");
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("AI_TIMEOUT");
        assertThat(response.getBody().error().message()).isEqualTo("El servicio de IA ha superado el tiempo límite de espera.");
    }

    @Test
    @DisplayName("handleAiServiceIntegrationException retorna HTTP 500 ante AI_PROCESSING_ERROR")
    void shouldReturn500OnAiProcessingError() {
        AiServiceIntegrationException exception = new AiServiceIntegrationException(
                AiServiceIntegrationException.ERROR_AI_PROCESSING,
                "Error al procesar la solicitud en el servicio de IA."
        );

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleAiServiceIntegrationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("error");
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("AI_PROCESSING_ERROR");
        assertThat(response.getBody().error().message()).isEqualTo("Error al procesar la solicitud en el servicio de IA.");
    }

    @Test
    @DisplayName("Garantiza que la respuesta nunca contenga traza de pila o stacktrace")
    void shouldNeverExposeStacktrace() {
        RuntimeException cause = new RuntimeException("Detalle técnico interno confidencial");
        AiServiceIntegrationException exception = new AiServiceIntegrationException(
                "AI_PROCESSING_ERROR",
                "Mensaje amigable",
                cause
        );

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleAiServiceIntegrationException(exception);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().message()).doesNotContain("Detalle técnico interno confidencial");
        assertThat(response.getBody().error().message()).isEqualTo("Mensaje amigable");
    }
}
