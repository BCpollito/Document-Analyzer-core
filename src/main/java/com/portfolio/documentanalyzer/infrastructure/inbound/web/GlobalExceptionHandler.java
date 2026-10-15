package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.domain.exception.AiServiceIntegrationException;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AiServiceIntegrationException.class)
    public ResponseEntity<ErrorResponseDto> handleAiServiceIntegrationException(AiServiceIntegrationException ex) {
        log.error("Excepción de integración con IA capturada: [{}] {}", ex.getErrorCode(), ex.getMessage(), ex);

        HttpStatus status = AiServiceIntegrationException.ERROR_AI_TIMEOUT.equals(ex.getErrorCode())
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponseDto response = ErrorResponseDto.of(ex.getErrorCode(), ex.getMessage());

        return ResponseEntity.status(status).body(response);
    }
}
