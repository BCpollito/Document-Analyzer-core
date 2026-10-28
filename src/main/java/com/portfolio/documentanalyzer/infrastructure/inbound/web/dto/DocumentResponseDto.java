package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.model.DocumentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record DocumentResponseDto(
        @Schema(description = "Identificador único asignado al documnento", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        @JsonProperty("id")
        String id,

        @Schema(description = "Nombre de documento", example = "finanzas-marzo")
        @JsonProperty("filename")
        String filename,

        @Schema(description = "Estado del documento", example = "PROCESSING")
        @JsonProperty("status")
        String status
) {
}
