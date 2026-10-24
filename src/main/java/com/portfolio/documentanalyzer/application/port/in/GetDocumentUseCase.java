package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.domain.model.Document;

public interface GetDocumentUseCase {
    Document getDocumentUseCase(String documentId, String userId);
}
