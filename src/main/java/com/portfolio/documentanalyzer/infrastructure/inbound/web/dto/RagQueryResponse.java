package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta generada por el sistema RAG (Retrieval-Augmented Generation)")
public record RagQueryResponse(
        @Schema(description = "Estado de la respuesta", example = "success")
        @JsonProperty("status")
        String status,

        @Schema(description = "Datos de la respuesta generada y las fuentes consultadas")
        @JsonProperty("data")
        Data data,

        @Schema(description = "Detalle del error si la consulta falló")
        @JsonProperty("error")
        ErrorDetail error
) {
    public static RagQueryResponse success(String answer, List<String> sources) {
        return new RagQueryResponse("success", new Data(answer, sources), null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Resultado de la consulta RAG con respuesta y fuentes")
    public record Data(
            @Schema(description = "Respuesta sintética generada por el modelo de IA a partir de los documentos", example = "El informe concluye un incremento del 15% en los ingresos operativos.")
            @JsonProperty("answer")
            String answer,

            @Schema(description = "Lista de identificadores de fragmentos (chunks) utilizados como fuente para generar la respuesta", example = "[\"doc-123_chunk_0\", \"doc-123_chunk_1\"]")
            @JsonProperty("sources")
            List<String> sources
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detalle del error")
    public record ErrorDetail(
            @Schema(description = "Código del error", example = "AI_PROCESSING_ERROR")
            @JsonProperty("code")
            String code,

            @Schema(description = "Descripción del error", example = "Error durante el procesamiento del RAG")
            @JsonProperty("message")
            String message
    ) {}
}
