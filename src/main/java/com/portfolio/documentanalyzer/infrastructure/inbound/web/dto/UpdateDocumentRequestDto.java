package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud para actualizar los metadatos o nombre de un documento")
public record UpdateDocumentRequestDto(
        @Schema(description = "Nuevo nombre para el documento", example = "informe_actualizado.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre del archivo es obligatorio")
        @JsonProperty("filename")
        String filename
) {}
