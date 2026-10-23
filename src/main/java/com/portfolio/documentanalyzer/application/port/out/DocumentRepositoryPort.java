package com.portfolio.documentanalyzer.application.port.out;

import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepositoryPort {

    Optional<Document> findByIdAndUserId(DocumentId documentId, UserId userId);

    List<Document> findAllByUserId(UserId userId);

    boolean existsByIdAndUserId(DocumentId documentId, UserId userId);
    void deleteByIdAndUserId(DocumentId documentId, UserId userId);
}
