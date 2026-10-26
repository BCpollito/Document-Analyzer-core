package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.portfolio.documentanalyzer.application.port.in.DeleteDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.GetDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.GetDocumentsUseCase;
import com.portfolio.documentanalyzer.application.port.in.UpdateDocumentUseCase;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.infrastructure.config.OpenApiConfig;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.DocumentResponseDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.UpdateDocumentRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Endpoints para la gestión segura de documentos del usuario")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DocumentController {

    private final GetDocumentsUseCase getDocumentsUseCase;
    private final GetDocumentUseCase getDocumentUseCase;
    private final UpdateDocumentUseCase updateDocumentUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;

    public DocumentController(
            GetDocumentsUseCase getDocumentsUseCase,
            GetDocumentUseCase getDocumentUseCase,
            UpdateDocumentUseCase updateDocumentUseCase,
            DeleteDocumentUseCase deleteDocumentUseCase
    ) {
        this.getDocumentsUseCase = getDocumentsUseCase;
        this.getDocumentUseCase = getDocumentUseCase;
        this.updateDocumentUseCase = updateDocumentUseCase;
        this.deleteDocumentUseCase = deleteDocumentUseCase;
    }

    public DocumentController(
            GetDocumentsUseCase getDocumentsUseCase,
            GetDocumentUseCase getDocumentUseCase,
            DeleteDocumentUseCase deleteDocumentUseCase
    ) {
        this(getDocumentsUseCase, getDocumentUseCase, null, deleteDocumentUseCase);
    }

    @Operation(summary = "Obtener un documento por ID", description = "Retorna el documento solicitado siempre que pertenezca al usuario autenticado.")
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponseDto> getDocument(Principal principal, @PathVariable UUID documentId) {
        String userId = getAuthenticatedUserId(principal);
        Document document = getDocumentUseCase.getDocumentUseCase(documentId.toString(), userId);
        return ResponseEntity.ok(toDto(document));
    }

    @Operation(summary = "Listar todos los documentos", description = "Retorna únicamente los documentos pertenecientes al usuario autenticado.")
    @GetMapping
    public ResponseEntity<List<DocumentResponseDto>> getAllDocuments(Principal principal) {
        String userId = getAuthenticatedUserId(principal);
        List<Document> allDocuments = getDocumentsUseCase.getDocumentsUseCase(userId);
        List<DocumentResponseDto> dtoList = allDocuments.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Modificar documento", description = "Actualiza los datos del documento si pertenece al usuario autenticado.")
    @PutMapping("/{documentId}")
    public ResponseEntity<DocumentResponseDto> updateDocument(
            Principal principal,
            @PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentRequestDto request) {
        String userId = getAuthenticatedUserId(principal);
        if (updateDocumentUseCase == null) {
            throw new UnsupportedOperationException("Operación de actualización no configurada");
        }
        Document document = updateDocumentUseCase.updateDocumentUseCase(documentId.toString(), userId, request.filename());
        return ResponseEntity.ok(toDto(document));
    }

    @Operation(summary = "Eliminar documento", description = "Elimina un documento si y solo si pertenece al usuario autenticado.")
    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(Principal principal, @PathVariable UUID documentId) {
        String userId = getAuthenticatedUserId(principal);
        deleteDocumentUseCase.deleteDocumentUseCase(documentId.toString(), userId);
    }

    private String getAuthenticatedUserId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        return principal.getName();
    }

    private DocumentResponseDto toDto(Document document) {
        return new DocumentResponseDto(
                document.getId().toString(),
                document.getFilename(),
                document.getStatus().toString()
        );
    }
}
