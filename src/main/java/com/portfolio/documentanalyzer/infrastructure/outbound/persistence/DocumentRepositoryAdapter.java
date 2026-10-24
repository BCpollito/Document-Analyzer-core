package com.portfolio.documentanalyzer.infrastructure.outbound.persistence;

import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity.DocumentEntity;
import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity.UserEntity;
import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.repository.SpringDataDocumentRepository;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class DocumentRepositoryAdapter implements DocumentRepositoryPort {

    private final SpringDataDocumentRepository springDataDocumentRepository;

    public DocumentRepositoryAdapter(SpringDataDocumentRepository springDataDocumentRepository) {
        this.springDataDocumentRepository = Objects.requireNonNull(springDataDocumentRepository, "springDataDocumentRepository no puede ser nulo");
    }

    @Override
    public Document save (Document document, UserId userId){
        Objects.requireNonNull(document, "El documento no puede ser nulo");

        DocumentEntity entity = toEntity(document);
        DocumentEntity savedEntity = springDataDocumentRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Document> findByIdAndUserId(DocumentId documentId, UserId userId){
        if (documentId == null){
            return java.util.Optional.empty();
        }
        return springDataDocumentRepository.findByIdAndUserId(documentId.value(), userId.value())
                .map(this::toDomain);
    }

    @Override
    public List<Document> findAllByUserId(UserId userId){
        return springDataDocumentRepository.findAllByUserId(userId.value())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndUserId(DocumentId documentId, UserId userId){
        if (documentId == null){
            return false;
        }
        return springDataDocumentRepository.existsByIdAndUserId(documentId.value(), userId.value());
    }

    @Override
    public void deleteByIdAndUserId(DocumentId documentId, UserId userId){
        springDataDocumentRepository.deleteByIdAndUserId(documentId.value(), userId.value());
    }
    private DocumentEntity toEntity(Document document) {
        if (document == null) {
            return null;
        }
        return new DocumentEntity(
                document.getId().value(),
                document.getUserId().value(),
                document.getFilename(),
                document.getFilePath(),
                document.getStatus(),
                document.getUploadDate(),
                document.getUpdatedAt(),
                document.getErrorMessage()
        );
    }

    private Document toDomain(DocumentEntity entity) {
        if (entity == null) {
            return null;
        }
        return Document.reconstitute(
                DocumentId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getFileName(),
                entity.getFilePath(),
                entity.getStatus(),
                entity.getUploadDate(),
                entity.getUpdateAt(),
                entity.getErrorMessage()
        );
    }
}
