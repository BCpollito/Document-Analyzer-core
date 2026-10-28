package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.domain.exception.AiServiceIntegrationException;
import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;
import com.portfolio.documentanalyzer.domain.exception.ResorceNotFoundException;
import com.portfolio.documentanalyzer.domain.exception.UserAlreadyExistsException;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn("Conflicto al registrar usuario: {}", ex.getMessage());
        ErrorResponseDto response = ErrorResponseDto.of("USER_ALREADY_EXISTS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidUserRegistrationException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidUserRegistrationException(InvalidUserRegistrationException ex) {
        log.warn("Registro de usuario inválido: {}", ex.getMessage());
        ErrorResponseDto response = ErrorResponseDto.of("INVALID_REGISTRATION_DATA", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Datos de solicitud inválidos");
        log.warn("Error de validación en la solicitud: {}", errorMessage);
        ErrorResponseDto response = ErrorResponseDto.of("VALIDATION_ERROR", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResorceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResorceNotFoundException ex){
        log.warn("Recurso no encontrado" + ex.getMessage());
        ErrorResponseDto responseDto = ErrorResponseDto.of("RESOURCE_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
    }
}
