package com.portfolio.documentanalyzer.infrastructure.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estructura estandarizada de respuesta de error")
public record ErrorResponseDto(
        @Schema(description = "Estado de la respuesta", example = "error")
        @JsonProperty("status")
        String status,

        @Schema(description = "Detalle del error")
        @JsonProperty("error")
        ErrorDetail error
) {
    public static ErrorResponseDto of(String code, String message) {
        return new ErrorResponseDto("error", new ErrorDetail(code, message));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Información detallada del error")
    public record ErrorDetail(
            @Schema(description = "Código identificador del error", example = "AI_TIMEOUT")
            @JsonProperty("code")
            String code,

            @Schema(description = "Mensaje legible para el usuario final", example = "El servicio de IA ha superado el tiempo límite de espera.")
            @JsonProperty("message")
            String message
    ) {}
}
