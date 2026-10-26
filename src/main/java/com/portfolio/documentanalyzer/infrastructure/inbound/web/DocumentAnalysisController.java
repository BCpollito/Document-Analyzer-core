package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.application.port.out.ConversationRepositoryPort;
import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.ResorceNotFoundException;
import com.portfolio.documentanalyzer.domain.vo.ConversationId;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Document Analysis", description = "Endpoints para la indexación y consulta mediante IA con aislamiento por usuario")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DocumentAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(DocumentAnalysisController.class);

    private final FastApiClientAdapter fastApiClientAdapter;
    private final DocumentRepositoryPort documentRepositoryPort;
    private final ConversationRepositoryPort conversationRepositoryPort;

    public DocumentAnalysisController(
            FastApiClientAdapter fastApiClientAdapter,
            DocumentRepositoryPort documentRepositoryPort,
            ConversationRepositoryPort conversationRepositoryPort
    ) {
        this.fastApiClientAdapter = fastApiClientAdapter;
        this.documentRepositoryPort = documentRepositoryPort;
        this.conversationRepositoryPort = conversationRepositoryPort;
    }

    public DocumentAnalysisController(FastApiClientAdapter fastApiClientAdapter) {
        this(fastApiClientAdapter, null, null);
    }

    @PostMapping("/documents/index")
    @Operation(
            summary = "Indexar documento para análisis",
            description = "Envía el contenido y metadatos de un documento para ser procesado, fragmentado y almacenado en la base de datos vectorial garantizando aislamiento."
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
    public ResponseEntity<DocumentIndexResponse> indexDocument(
            @Valid @RequestBody DocumentIndexRequest request,
            Principal principal
    ) {
        String userId = principal != null ? principal.getName() : null;

        if (userId != null && documentRepositoryPort != null) {
            try {
                UUID docUuid = UUID.fromString(request.documentId());
                UUID userUuid = UUID.fromString(userId);
                DocumentId docId = DocumentId.of(docUuid);
                UserId uId = UserId.of(userUuid);

                // Si el documento existe globalmente pero no pertenece a este usuario -> rechazar (anti-IDOR)
                if (documentRepositoryPort.findById(docId).isPresent() &&
                        !documentRepositoryPort.existsByIdAndUserId(docId, uId)) {
                    log.warn("SECURITY - Intento no autorizado de indexar o sobreescribir documento ajeno [{}]. Solicitante: [{}]",
                            request.documentId(), userId);
                    throw new ResorceNotFoundException("Recurso no encontrado");
                }
            } catch (IllegalArgumentException e) {
                log.debug("ID de documento o usuario no es UUID estándar, omitiendo validación relacional: {}", e.getMessage());
            }
        }

        AiDocumentIndexRequestDto aiRequest = new AiDocumentIndexRequestDto(
                request.documentId(),
                userId,
                request.content(),
                request.metadata()
        );

        AiDocumentIndexResponseDto aiResponse = fastApiClientAdapter.indexDocument(aiRequest);
        log.info("AUDIT - Documento indexado exitosamente. DocumentId: [{}], UserId: [{}]", request.documentId(), userId);

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
            description = "Consulta la base de conocimientos vectorial mediante Retrieval-Augmented Generation (RAG) filtrando por usuario y respetando la propiedad."
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
    public ResponseEntity<RagQueryResponse> queryRag(
            @Valid @RequestBody RagQueryRequest request,
            Principal principal
    ) {
        String userId = principal != null ? principal.getName() : null;

        if (userId != null && conversationRepositoryPort != null &&
                request.conversationId() != null && !request.conversationId().isBlank()) {
            try {
                UUID convUuid = UUID.fromString(request.conversationId());
                UUID userUuid = UUID.fromString(userId);
                boolean isOwned = conversationRepositoryPort.existsByIdAndUserId(
                        ConversationId.of(convUuid), UserId.of(userUuid)
                );
                if (!isOwned) {
                    log.warn("SECURITY - Consulta RAG rechazada: Conversación [{}] no encontrada o no pertenece al usuario [{}]",
                            request.conversationId(), userId);
                    throw new ResorceNotFoundException("Conversación no encontrada");
                }
            } catch (IllegalArgumentException e) {
                log.debug("ID de conversación no es UUID estándar: {}", e.getMessage());
            }
        }

        List<MessageRoleDto> history = request.history() != null
                ? request.history().stream()
                        .map(msg -> new MessageRoleDto(msg.role(), msg.content()))
                        .toList()
                : List.of();

        AiRagQueryRequestDto aiRequest = new AiRagQueryRequestDto(
                request.query(),
                userId,
                request.conversationId(),
                history
        );

        AiRagQueryResponseDto aiResponse = fastApiClientAdapter.queryRag(aiRequest);
        log.info("AUDIT - Consulta RAG completada exitosamente. ConversationId: [{}], UserId: [{}]",
                request.conversationId(), userId);

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
