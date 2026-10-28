package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.policy.DocumentAuthorizationPolicy;
import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.ResorceNotFoundException;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.model.DocumentStatus;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepositoryPort documentRepositoryPort;

    private DocumentService documentService;

    private final UUID user1Uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID user2Uuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID docUuid = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private Document sampleDocument;

    @BeforeEach
    void setUp() {
        DocumentAuthorizationPolicy policy = new DocumentAuthorizationPolicy(documentRepositoryPort);
        documentService = new DocumentService(documentRepositoryPort, policy);

        sampleDocument = Document.reconstitute(
                DocumentId.of(docUuid),
                UserId.of(user1Uuid),
                "documento1.pdf",
                "/uploads/documento1.pdf",
                DocumentStatus.COMPLETED,
                Instant.now(),
                Instant.now(),
                null
        );
    }

    @Test
    @DisplayName("getDocumentUseCase: Retorna documento si pertenece al usuario")
    void getDocumentSuccess() {
        when(documentRepositoryPort.findByIdAndUserId(DocumentId.of(docUuid), UserId.of(user1Uuid)))
                .thenReturn(Optional.of(sampleDocument));

        Document result = documentService.getDocumentUseCase(docUuid.toString(), user1Uuid.toString());

        assertThat(result).isNotNull();
        assertThat(result.getId().value()).isEqualTo(docUuid);
        assertThat(result.getFilename()).isEqualTo("documento1.pdf");
    }

    @Test
    @DisplayName("getDocumentUseCase: Lanza ResorceNotFoundException si no pertenece al usuario (CA-02 IDOR)")
    void getDocumentForbiddenOrNotFound() {
        when(documentRepositoryPort.findByIdAndUserId(DocumentId.of(docUuid), UserId.of(user2Uuid)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocumentUseCase(docUuid.toString(), user2Uuid.toString()))
                .isInstanceOf(ResorceNotFoundException.class)
                .hasMessage("Recurso no encontrado");
    }

    @Test
    @DisplayName("getDocumentsUseCase: Retorna únicamente documentos asociados al usuario")
    void getDocumentsSuccess() {
        when(documentRepositoryPort.findAllByUserId(UserId.of(user1Uuid)))
                .thenReturn(List.of(sampleDocument));

        List<Document> result = documentService.getDocumentsUseCase(user1Uuid.toString());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId().value()).isEqualTo(user1Uuid);
    }

    @Test
    @DisplayName("updateDocumentUseCase: Modifica documento si pertenece al usuario")
    void updateDocumentSuccess() {
        when(documentRepositoryPort.findByIdAndUserId(DocumentId.of(docUuid), UserId.of(user1Uuid)))
                .thenReturn(Optional.of(sampleDocument));
        when(documentRepositoryPort.save(any(Document.class), eq(UserId.of(user1Uuid))))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Document updated = documentService.updateDocumentUseCase(docUuid.toString(), user1Uuid.toString(), "renombrado.pdf");

        assertThat(updated.getFilename()).isEqualTo("renombrado.pdf");
        verify(documentRepositoryPort).save(any(Document.class), eq(UserId.of(user1Uuid)));
    }

    @Test
    @DisplayName("updateDocumentUseCase: Falla con 404 al intentar modificar documento ajeno (CA-02)")
    void updateDocumentAjenoThrows() {
        when(documentRepositoryPort.findByIdAndUserId(DocumentId.of(docUuid), UserId.of(user2Uuid)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.updateDocumentUseCase(docUuid.toString(), user2Uuid.toString(), "hack.pdf"))
                .isInstanceOf(ResorceNotFoundException.class);
        verify(documentRepositoryPort, never()).save(any(), any());
    }

    @Test
    @DisplayName("deleteDocumentUseCase: Elimina documento si pertenece al usuario")
    void deleteDocumentSuccess() {
        when(documentRepositoryPort.existsByIdAndUserId(DocumentId.of(docUuid), UserId.of(user1Uuid)))
                .thenReturn(true);

        documentService.deleteDocumentUseCase(docUuid.toString(), user1Uuid.toString());

        verify(documentRepositoryPort).deleteByIdAndUserId(DocumentId.of(docUuid), UserId.of(user1Uuid));
    }

    @Test
    @DisplayName("deleteDocumentUseCase: Falla con 404 al intentar eliminar documento ajeno (CA-02)")
    void deleteDocumentAjenoThrows() {
        when(documentRepositoryPort.existsByIdAndUserId(DocumentId.of(docUuid), UserId.of(user2Uuid)))
                .thenReturn(false);

        assertThatThrownBy(() -> documentService.deleteDocumentUseCase(docUuid.toString(), user2Uuid.toString()))
                .isInstanceOf(ResorceNotFoundException.class);
        verify(documentRepositoryPort, never()).deleteByIdAndUserId(any(), any());
    }
}
