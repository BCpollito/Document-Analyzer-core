package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.policy.DocumentAuthorizationPolicy;
import com.portfolio.documentanalyzer.application.port.in.DeleteDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.GetDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.GetDocumentsUseCase;
import com.portfolio.documentanalyzer.application.port.in.UpdateDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DocumentService implements DeleteDocumentUseCase, GetDocumentsUseCase, GetDocumentUseCase, UpdateDocumentUseCase {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepositoryPort documentRepositoryPort;
    private final DocumentAuthorizationPolicy authorizationPolicy;

    public DocumentService(DocumentRepositoryPort documentRepositoryPort, DocumentAuthorizationPolicy authorizationPolicy) {
        this.documentRepositoryPort = documentRepositoryPort;
        this.authorizationPolicy = authorizationPolicy;
    }

    public DocumentService(DocumentRepositoryPort documentRepositoryPort) {
        this(documentRepositoryPort, new DocumentAuthorizationPolicy(documentRepositoryPort));
    }

    @Override
    public Document getDocumentUseCase(String documentIdStr, String userIdStr) {
        Document document = authorizationPolicy.verifyAndGetOwnership(documentIdStr, userIdStr);
        log.info("AUDIT - Operación de lectura de documento exitosa. DocumentId: [{}], UserId: [{}]", documentIdStr, userIdStr);
        return document;
    }

    @Override
    public List<Document> getDocumentsUseCase(String userIdStr) {
        UserId userId = authorizationPolicy.parseUserId(userIdStr);
        List<Document> documents = documentRepositoryPort.findAllByUserId(userId);
        log.info("AUDIT - Consulta de listado de documentos exitosa. Total: {}, UserId: [{}]", documents.size(), userIdStr);
        return documents;
    }

    @Override
    public Document updateDocumentUseCase(String documentIdStr, String userIdStr, String newFilename) {
        Document document = authorizationPolicy.verifyAndGetOwnership(documentIdStr, userIdStr);
        document.updateFilename(newFilename);
        Document updatedDocument = documentRepositoryPort.save(document, document.getUserId());
        log.info("AUDIT - Operación de modificación de documento exitosa. DocumentId: [{}], UserId: [{}]", documentIdStr, userIdStr);
        return updatedDocument;
    }

    @Override
    public void deleteDocumentUseCase(String documentIdStr, String userIdStr) {
        authorizationPolicy.verifyOwnership(documentIdStr, userIdStr);
        UserId userId = authorizationPolicy.parseUserId(userIdStr);
        DocumentId documentId = authorizationPolicy.parseDocumentId(documentIdStr);
        documentRepositoryPort.deleteByIdAndUserId(documentId, userId);
        log.info("AUDIT - Operación de eliminación de documento exitosa. DocumentId: [{}], UserId: [{}]", documentIdStr, userIdStr);
    }
}
