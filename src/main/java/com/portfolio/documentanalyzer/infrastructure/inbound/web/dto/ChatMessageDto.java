package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Mensaje individual dentro del historial de conversación")
public record ChatMessageDto(
        @Schema(description = "Rol del emisor del mensaje (USER, ASSISTANT o SYSTEM)", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El rol es obligatorio")
        @JsonProperty("role")
        String role,

        @Schema(description = "Contenido textual del mensaje", example = "¿Cuál es el balance general?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El contenido es obligatorio")
        @JsonProperty("content")
        String content
) {
    public static ChatMessageDto user(String content) {
        return new ChatMessageDto("USER", content);
    }

    public static ChatMessageDto assistant(String content) {
        return new ChatMessageDto("ASSISTANT", content);
    }

    public static ChatMessageDto system(String content) {
        return new ChatMessageDto("SYSTEM", content);
    }
}
