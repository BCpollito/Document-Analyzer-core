package com.portfolio.documentanalyzer.application.service;

import com.portfolio.documentanalyzer.application.port.in.DeleteDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.GetDocumentUseCase;
import com.portfolio.documentanalyzer.application.port.in.GetDocumentsUseCase;
import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.domain.exception.ResorceNotFoundException;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DocumentService implements DeleteDocumentUseCase, GetDocumentsUseCase, GetDocumentUseCase {

    private final DocumentRepositoryPort documentRepositoryPort;

    public DocumentService(DocumentRepositoryPort documentRepositoryPort) {
        this.documentRepositoryPort = documentRepositoryPort;
    }

    @Override
    public Document getDocumentUseCase(String documentIdStr, String userIdStr){
        UUID uuidUser;
        UUID uuidDoc;
        try {
            uuidUser = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("El formato del ID de usuario debe ser un UUID válido.");
        }

        try {
            uuidDoc = UUID.fromString(documentIdStr);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("El formato del ID del documento debe ser un UUID válido.");
        }

        UserId userId = UserId.of(uuidUser);
        DocumentId documentId = DocumentId.of(uuidDoc);

        if (!documentRepositoryPort.existsByIdAndUserId(documentId, userId)){
            throw new ResorceNotFoundException("Recurso no encontrado");
        }

        return documentRepositoryPort.findByIdAndUserId(documentId, userId).orElseThrow(()-> new ResorceNotFoundException("Documento no encontrado"));

    }

    @Override
    public List<Document> getDocumentsUseCase(String userIdStr){
        UUID uuid;

        try {
            uuid = UUID.fromString(userIdStr);
        }catch (IllegalArgumentException | NullPointerException e){
            throw new IllegalArgumentException("EL formato del ID del usuario debe ser un UUID valido.");
        }
        UserId userId= UserId.of(uuid);

        return documentRepositoryPort.findAllByUserId(userId);
    }

    @Override
    public void deleteDocumentUseCase(String documentIdStr, String userIdStr){
        UUID uuidUser;
        UUID uuidDoc;
        try {
            uuidUser = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("El formato del ID de usuario debe ser un UUID válido.");
        }

        try {
            uuidDoc = UUID.fromString(documentIdStr);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("El formato del ID del documento debe ser un UUID válido.");
        }

        UserId userId = UserId.of(uuidUser);
        DocumentId documentId = DocumentId.of(uuidDoc);

        if (!documentRepositoryPort.existsByIdAndUserId(documentId, userId)){
            throw new ResorceNotFoundException("Recurso no encontrado");
        }
        documentRepositoryPort.deleteByIdAndUserId(documentId, userId);
    }
}
