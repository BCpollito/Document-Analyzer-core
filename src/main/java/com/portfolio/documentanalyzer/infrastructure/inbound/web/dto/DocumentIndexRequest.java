package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Petición para indexar un documento en el sistema de análisis")
public record DocumentIndexRequest(
        @Schema(description = "Identificador único del documento", example = "doc-123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El document_id es obligatorio")
        @JsonProperty("document_id")
        String documentId,

        @Schema(description = "Contenido de texto completo del documento a indexar", example = "Texto extraído del documento PDF o archivo corporativo...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El contenido es obligatorio")
        @JsonProperty("content")
        String content,

        @Schema(description = "Metadatos adicionales asociados al documento", example = "{\"filename\": \"informe_q3.pdf\", \"category\": \"finanzas\"}")
        @JsonProperty("metadata")
        Map<String, String> metadata
) {
    public DocumentIndexRequest(String documentId, String content) {
        this(documentId, content, null);
    }
}
