package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.documentanalyzer.application.port.in.DeleteDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.GetDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.GetDocumentsUseCase;
import com.portfolio.documentanalyzer.application.port.in.UpdateDocumentUseCase;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.model.DocumentStatus;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UpdateDocumentRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GetDocumentsUseCase getDocumentsUseCase;

    @Mock
    private GetDocumentUseCase getDocumentUseCase;

    @Mock
    private UpdateDocumentUseCase updateDocumentUseCase;

    @Mock
    private DeleteDocumentUseCase deleteDocumentUseCase;

    private final UUID userUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID docUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final Principal mockPrincipal = () -> userUuid.toString();

    @BeforeEach
    void setUp() {
        DocumentController controller = new DocumentController(
                getDocumentsUseCase,
                getDocumentUseCase,
                updateDocumentUseCase,
                deleteDocumentUseCase
        );
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/documents/{documentId} retorna 200 OK y DocumentResponseDto")
    void getDocumentShouldReturnOk() throws Exception {
        Document doc = Document.reconstitute(
                DocumentId.of(docUuid),
                UserId.of(userUuid),
                "test.pdf",
                "/tmp/test.pdf",
                DocumentStatus.COMPLETED,
                Instant.now(),
                Instant.now(),
                null
        );

        when(getDocumentUseCase.getDocumentUseCase(docUuid.toString(), userUuid.toString()))
                .thenReturn(doc);

        mockMvc.perform(get("/api/v1/documents/" + docUuid)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docUuid.toString()))
                .andExpect(jsonPath("$.filename").value("test.pdf"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(getDocumentUseCase).getDocumentUseCase(docUuid.toString(), userUuid.toString());
    }

    @Test
    @DisplayName("GET /api/v1/documents retorna lista de documentos del usuario autenticado")
    void getAllDocumentsShouldReturnOk() throws Exception {
        Document doc = Document.reconstitute(
                DocumentId.of(docUuid),
                UserId.of(userUuid),
                "lista.pdf",
                "/tmp/lista.pdf",
                DocumentStatus.COMPLETED,
                Instant.now(),
                Instant.now(),
                null
        );

        when(getDocumentsUseCase.getDocumentsUseCase(userUuid.toString()))
                .thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/documents")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(docUuid.toString()))
                .andExpect(jsonPath("$[0].filename").value("lista.pdf"));

        verify(getDocumentsUseCase).getDocumentsUseCase(userUuid.toString());
    }

    @Test
    @DisplayName("PUT /api/v1/documents/{documentId} actualiza y retorna 200 OK")
    void updateDocumentShouldReturnOk() throws Exception {
        Document docUpdated = Document.reconstitute(
                DocumentId.of(docUuid),
                UserId.of(userUuid),
                "actualizado.pdf",
                "/tmp/actualizado.pdf",
                DocumentStatus.COMPLETED,
                Instant.now(),
                Instant.now(),
                null
        );

        when(updateDocumentUseCase.updateDocumentUseCase(docUuid.toString(), userUuid.toString(), "actualizado.pdf"))
                .thenReturn(docUpdated);

        UpdateDocumentRequestDto request = new UpdateDocumentRequestDto("actualizado.pdf");

        mockMvc.perform(put("/api/v1/documents/" + docUuid)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("actualizado.pdf"));

        verify(updateDocumentUseCase).updateDocumentUseCase(docUuid.toString(), userUuid.toString(), "actualizado.pdf");
    }

    @Test
    @DisplayName("DELETE /api/v1/documents/{documentId} retorna 204 No Content")
    void deleteDocumentShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/documents/" + docUuid)
                        .principal(mockPrincipal))
                .andExpect(status().isNoContent());

        verify(deleteDocumentUseCase).deleteDocumentUseCase(docUuid.toString(), userUuid.toString());
    }
}
