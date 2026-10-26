package com.portfolio.documentanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.documentanalyzer.application.policy.DocumentAuthorizationPolicy;
import com.portfolio.documentanalyzer.application.port.out.ConversationRepositoryPort;
import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.application.service.DocumentService;
import com.portfolio.documentanalyzer.domain.model.Conversation;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.model.DocumentStatus;
import com.portfolio.documentanalyzer.domain.vo.ConversationId;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.Title;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.DocumentAnalysisController;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.DocumentController;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.GlobalExceptionHandler;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.DocumentIndexRequest;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.RagQueryRequest;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UpdateDocumentRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.FastApiClientAdapter;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexResponseDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas integrales de aislamiento de datos entre dos usuarios (RF-007: CA-01 al CA-06).
 * Demuestra matemáticamente que un Usuario B no puede acceder, modificar, eliminar,
 * listar ni consultar conversaciones o fragmentos vectoriales pertenecientes al Usuario A.
 */
@ExtendWith(MockitoExtension.class)
class UserDataIsolationTest {

    private MockMvc documentMockMvc;
    private MockMvc analysisMockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DocumentRepositoryPort documentRepositoryPort;

    @Mock
    private ConversationRepositoryPort conversationRepositoryPort;

    @Mock
    private FastApiClientAdapter fastApiClientAdapter;

    // Usuario A (legítimo propietario de Doc A y Conv A)
    private final UUID userAUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final Principal principalUserA = () -> userAUuid.toString();

    // Usuario B (atacante potencial o usuario externo)
    private final UUID userBUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final Principal principalUserB = () -> userBUuid.toString();

    // Recursos de Usuario A
    private final UUID docAUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID convAUuid = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    // Recursos de Usuario B
    private final UUID docBUuid = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private Document docA;
    private Document docB;
    private Conversation convA;

    @BeforeEach
    void setUp() {
        DocumentAuthorizationPolicy policy = new DocumentAuthorizationPolicy(documentRepositoryPort);
        DocumentService documentService = new DocumentService(documentRepositoryPort, policy);

        DocumentController documentController = new DocumentController(
                documentService,
                documentService,
                documentService,
                documentService
        );

        DocumentAnalysisController analysisController = new DocumentAnalysisController(
                fastApiClientAdapter,
                documentRepositoryPort,
                conversationRepositoryPort
        );

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        this.documentMockMvc = MockMvcBuilders.standaloneSetup(documentController)
                .setControllerAdvice(exceptionHandler)
                .build();

        this.analysisMockMvc = MockMvcBuilders.standaloneSetup(analysisController)
                .setControllerAdvice(exceptionHandler)
                .build();

        docA = Document.reconstitute(
                DocumentId.of(docAUuid),
                UserId.of(userAUuid),
                "secreto_corporativo_usuarioA.pdf",
                "/storage/docs/a.pdf",
                DocumentStatus.COMPLETED,
                Instant.now(),
                Instant.now(),
                null
        );

        docB = Document.reconstitute(
                DocumentId.of(docBUuid),
                UserId.of(userBUuid),
                "publico_usuarioB.pdf",
                "/storage/docs/b.pdf",
                DocumentStatus.COMPLETED,
                Instant.now(),
                Instant.now(),
                null
        );

        convA = Conversation.reconstitute(
                ConversationId.of(convAUuid),
                UserId.of(userAUuid),
                DocumentId.of(docAUuid),
                Title.of("Conversacion Privada de Usuario A"),
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("CA-01 & CA-02: Usuario B no puede leer documento de Usuario A (Prevención de IDOR retorna 404)")
    void userBCannotReadUserADocument() throws Exception {
        when(documentRepositoryPort.findByIdAndUserId(DocumentId.of(docAUuid), UserId.of(userAUuid)))
                .thenReturn(Optional.of(docA));
        when(documentRepositoryPort.findByIdAndUserId(DocumentId.of(docAUuid), UserId.of(userBUuid)))
                .thenReturn(Optional.empty());

        // 1. Usuario A puede leer su propio documento
        documentMockMvc.perform(get("/api/v1/documents/" + docAUuid)
                        .principal(principalUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docAUuid.toString()))
                .andExpect(jsonPath("$.filename").value("secreto_corporativo_usuarioA.pdf"));

        // 2. Usuario B intenta leer el documento de A cambiando solo el ID -> 404 Not Found
        documentMockMvc.perform(get("/api/v1/documents/" + docAUuid)
                        .principal(principalUserB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("CA-02: Usuario B no puede modificar documento de Usuario A")
    void userBCannotModifyUserADocument() throws Exception {
        when(documentRepositoryPort.findByIdAndUserId(DocumentId.of(docAUuid), UserId.of(userBUuid)))
                .thenReturn(Optional.empty());

        UpdateDocumentRequestDto hackRequest = new UpdateDocumentRequestDto("archivo_hackeado.pdf");

        documentMockMvc.perform(put("/api/v1/documents/" + docAUuid)
                        .principal(principalUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hackRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        verify(documentRepositoryPort, never()).save(any(), any());
        assertThat(docA.getFilename()).isEqualTo("secreto_corporativo_usuarioA.pdf");
    }

    @Test
    @DisplayName("CA-02: Usuario B no puede eliminar documento de Usuario A")
    void userBCannotDeleteUserADocument() throws Exception {
        when(documentRepositoryPort.existsByIdAndUserId(DocumentId.of(docAUuid), UserId.of(userBUuid)))
                .thenReturn(false);

        documentMockMvc.perform(delete("/api/v1/documents/" + docAUuid)
                        .principal(principalUserB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        verify(documentRepositoryPort, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("CA-01 & CA-02: Listado de documentos de Usuario B jamás expone documentos de Usuario A")
    void documentListingIsStrictlyIsolated() throws Exception {
        when(documentRepositoryPort.findAllByUserId(UserId.of(userBUuid)))
                .thenReturn(List.of(docB));

        documentMockMvc.perform(get("/api/v1/documents")
                        .principal(principalUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(docBUuid.toString()))
                .andExpect(jsonPath("$[0].filename").value("publico_usuarioB.pdf"));
    }

    @Test
    @DisplayName("CA-01 & CA-02: Usuario B no puede reindexar ni sobreescribir documento de Usuario A")
    void userBCannotReindexUserADocument() throws Exception {
        when(documentRepositoryPort.findById(DocumentId.of(docAUuid)))
                .thenReturn(Optional.of(docA));
        when(documentRepositoryPort.existsByIdAndUserId(DocumentId.of(docAUuid), UserId.of(userBUuid)))
                .thenReturn(false);

        DocumentIndexRequest request = new DocumentIndexRequest(
                docAUuid.toString(),
                "Contenido malicioso inyectado por Usuario B"
        );

        analysisMockMvc.perform(post("/api/v1/documents/index")
                        .principal(principalUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        verify(fastApiClientAdapter, never()).indexDocument(any());
    }

    @Test
    @DisplayName("CA-03 & CA-06: Búsqueda vectorial RAG propaga el ID del usuario del servidor para filtrado")
    void ragQueryFiltersStrictlyByUser() throws Exception {
        AiRagQueryResponseDto aiResponse = new AiRagQueryResponseDto(
                "success",
                new AiRagQueryResponseDto.Data(
                        "Respuesta aislada para Usuario B",
                        List.of("user_" + userBUuid + "_doc_chunk_1")
                ),
                (AiRagQueryResponseDto.Error) null
        );

        when(fastApiClientAdapter.queryRag(any(AiRagQueryRequestDto.class))).thenReturn(aiResponse);

        RagQueryRequest request = new RagQueryRequest("¿Qué dice mi documento?", null, List.of());

        analysisMockMvc.perform(post("/api/v1/rag/query")
                        .principal(principalUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.answer").value("Respuesta aislada para Usuario B"))
                .andExpect(jsonPath("$.data.sources[0]").value("user_" + userBUuid + "_doc_chunk_1"));

        ArgumentCaptor<AiRagQueryRequestDto> captor = ArgumentCaptor.forClass(AiRagQueryRequestDto.class);
        verify(fastApiClientAdapter).queryRag(captor.capture());

        AiRagQueryRequestDto capturedRequest = captor.getValue();
        assertThat(capturedRequest.userId()).isEqualTo(userBUuid.toString());
        assertThat(capturedRequest.userId()).isNotEqualTo(userAUuid.toString());
    }

    @Test
    @DisplayName("CA-04: Usuario B no puede consultar historial RAG usando la conversación de Usuario A")
    void userBCannotAccessUserAConversationHistory() throws Exception {
        when(conversationRepositoryPort.existsByIdAndUserId(ConversationId.of(convAUuid), UserId.of(userBUuid)))
                .thenReturn(false);

        RagQueryRequest request = new RagQueryRequest(
                "¿Qué se habló antes?",
                convAUuid.toString(),
                List.of()
        );

        analysisMockMvc.perform(post("/api/v1/rag/query")
                        .principal(principalUserB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        verify(fastApiClientAdapter, never()).queryRag(any());
    }
}
