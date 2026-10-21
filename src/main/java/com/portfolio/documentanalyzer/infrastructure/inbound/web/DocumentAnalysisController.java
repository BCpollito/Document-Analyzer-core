package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.infrastructure.config.OpenApiConfig;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.DocumentIndexRequest;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.DocumentIndexResponse;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.ErrorResponseDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.RagQueryRequest;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.RagQueryResponse;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.FastApiClientAdapter;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexResponseDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryResponseDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.MessageRoleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Document Analysis", description = "Endpoints para la indexación y consulta mediante IA")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DocumentAnalysisController {

    private final FastApiClientAdapter fastApiClientAdapter;

    public DocumentAnalysisController(FastApiClientAdapter fastApiClientAdapter) {
        this.fastApiClientAdapter = fastApiClientAdapter;
    }

    @PostMapping("/documents/index")
    @Operation(
            summary = "Indexar documento para análisis",
            description = "Envía el contenido y metadatos de un documento para ser procesado, fragmentado y almacenado en la base de datos vectorial."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento indexado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DocumentIndexResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno durante el procesamiento del documento en el servicio de IA",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Servicio de IA no disponible o tiempo de espera agotado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    public ResponseEntity<DocumentIndexResponse> indexDocument(@Valid @RequestBody DocumentIndexRequest request) {
        AiDocumentIndexRequestDto aiRequest = new AiDocumentIndexRequestDto(
                request.documentId(),
                request.content(),
                request.metadata()
        );

        AiDocumentIndexResponseDto aiResponse = fastApiClientAdapter.indexDocument(aiRequest);

        DocumentIndexResponse response = new DocumentIndexResponse(
                aiResponse.status(),
                aiResponse.data() != null
                        ? new DocumentIndexResponse.Data(aiResponse.data().documentId(), aiResponse.data().chunksProcessed())
                        : null,
                aiResponse.error() != null
                        ? new DocumentIndexResponse.ErrorDetail(aiResponse.error().code(), aiResponse.error().message())
                        : null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rag/query")
    @Operation(
            summary = "Realizar consulta RAG sobre los documentos indexados",
            description = "Consulta la base de conocimientos vectorial mediante Retrieval-Augmented Generation (RAG) utilizando el historial conversacional."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Consulta RAG procesada exitosamente con fuentes asociadas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RagQueryResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno en el procesamiento de la consulta en el servicio de IA",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Servicio de IA no disponible o tiempo de espera agotado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    public ResponseEntity<RagQueryResponse> queryRag(@Valid @RequestBody RagQueryRequest request) {
        List<MessageRoleDto> history = request.history() != null
                ? request.history().stream()
                        .map(msg -> new MessageRoleDto(msg.role(), msg.content()))
                        .toList()
                : List.of();

        AiRagQueryRequestDto aiRequest = new AiRagQueryRequestDto(
                request.query(),
                request.conversationId(),
                history
        );

        AiRagQueryResponseDto aiResponse = fastApiClientAdapter.queryRag(aiRequest);

        RagQueryResponse response = new RagQueryResponse(
                aiResponse.status(),
                aiResponse.data() != null
                        ? new RagQueryResponse.Data(aiResponse.data().answer(), aiResponse.data().sources())
                        : null,
                aiResponse.error() != null
                        ? new RagQueryResponse.ErrorDetail(aiResponse.error().code(), aiResponse.error().message())
                        : null
        );

        return ResponseEntity.ok(response);
    }
}
