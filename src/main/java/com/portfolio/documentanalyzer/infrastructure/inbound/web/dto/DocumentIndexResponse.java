package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta del proceso de indexación de documento")
public record DocumentIndexResponse(
        @Schema(description = "Estado de la operación", example = "success")
        @JsonProperty("status")
        String status,

        @Schema(description = "Datos del resultado de la indexación")
        @JsonProperty("data")
        Data data,

        @Schema(description = "Detalle del error si la operación falló")
        @JsonProperty("error")
        ErrorDetail error
) {
    public static DocumentIndexResponse success(String documentId, Integer chunksProcessed) {
        return new DocumentIndexResponse("success", new Data(documentId, chunksProcessed), null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Datos detallados del documento indexado")
    public record Data(
            @Schema(description = "Identificador del documento indexado", example = "doc-123")
            @JsonProperty("document_id")
            String documentId,

            @Schema(description = "Número de fragmentos (chunks) procesados y almacenados", example = "5")
            @JsonProperty("chunks_processed")
            Integer chunksProcessed
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detalle del error")
    public record ErrorDetail(
            @Schema(description = "Código del error", example = "AI_PROCESSING_ERROR")
            @JsonProperty("code")
            String code,

            @Schema(description = "Mensaje descriptivo del error", example = "Error durante la indexación")
            @JsonProperty("message")
            String message
    ) {}
}
