package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.domain.model.Document;

public interface DeleteDocumentUseCase {
    void deleteDocumentUseCase(String documentId, String userId);
}
