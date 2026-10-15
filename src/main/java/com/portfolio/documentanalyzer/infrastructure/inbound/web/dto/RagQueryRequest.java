package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Petición de consulta RAG sobre el contenido de los documentos")
public record RagQueryRequest(
        @Schema(description = "Pregunta o consulta del usuario", example = "¿Cuáles fueron las conclusiones principales del informe?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La consulta (query) es obligatoria")
        @JsonProperty("query")
        String query,

        @Schema(description = "Identificador de la conversación", example = "conv-456")
        @JsonProperty("conversation_id")
        String conversationId,

        @Schema(description = "Historial cronológico de mensajes previos de la conversación")
        @JsonProperty("history")
        List<ChatMessageDto> history
) {
    public RagQueryRequest(String query, String conversationId) {
        this(query, conversationId, List.of());
    }
}
