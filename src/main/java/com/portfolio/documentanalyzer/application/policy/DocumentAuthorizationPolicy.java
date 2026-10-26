package com.portfolio.documentanalyzer.application.policy;

import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.ResorceNotFoundException;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

/**
 * Política común de autorización y validación de propiedad para documentos.
 * Previene vulnerabilidades IDOR y fuga de información asegurando que un usuario
 * solo pueda interactuar con sus propios recursos.
 */
public class DocumentAuthorizationPolicy {

    private static final Logger log = LoggerFactory.getLogger(DocumentAuthorizationPolicy.class);
    private final DocumentRepositoryPort documentRepositoryPort;

    public DocumentAuthorizationPolicy(DocumentRepositoryPort documentRepositoryPort) {
        this.documentRepositoryPort = Objects.requireNonNull(documentRepositoryPort, "documentRepositoryPort no puede ser nulo");
    }

    /**
     * Valida que el documento exista y pertenezca al usuario especificado.
     * Si no existe o pertenece a otro usuario, lanza ResorceNotFoundException (404)
     * para no filtrar la existencia de recursos ajenos.
     */
    public Document verifyAndGetOwnership(String documentIdStr, String userIdStr) {
        UserId userId = parseUserId(userIdStr);
        DocumentId documentId = parseDocumentId(documentIdStr);

        return documentRepositoryPort.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> {
                    log.warn("SECURITY - Acceso no autorizado o recurso no encontrado. DocumentId: [{}], UserId: [{}]",
                            documentIdStr, userIdStr);
                    return new ResorceNotFoundException("Recurso no encontrado");
                });
    }

    /**
     * Verifica la existencia y propiedad del documento sin retornar la entidad.
     */
    public void verifyOwnership(String documentIdStr, String userIdStr) {
        UserId userId = parseUserId(userIdStr);
        DocumentId documentId = parseDocumentId(documentIdStr);

        if (!documentRepositoryPort.existsByIdAndUserId(documentId, userId)) {
            log.warn("SECURITY - Verificación de propiedad fallida. DocumentId: [{}], UserId: [{}]",
                    documentIdStr, userIdStr);
            throw new ResorceNotFoundException("Recurso no encontrado");
        }
    }

    public UserId parseUserId(String userIdStr) {
        try {
            return UserId.of(UUID.fromString(userIdStr));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("El formato del ID de usuario debe ser un UUID válido.");
        }
    }

    public DocumentId parseDocumentId(String documentIdStr) {
        try {
            return DocumentId.of(UUID.fromString(documentIdStr));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("El formato del ID del documento debe ser un UUID válido.");
        }
    }
}
