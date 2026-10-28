package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.domain.model.Document;

public interface UpdateDocumentUseCase {
    Document updateDocumentUseCase(String documentId, String userId, String newFilename);
}
