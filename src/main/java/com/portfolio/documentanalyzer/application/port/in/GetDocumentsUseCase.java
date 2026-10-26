package com.portfolio.documentanalyzer.application.port.in;

import com.portfolio.documentanalyzer.domain.model.Document;

import java.util.List;

public interface GetDocumentsUseCase {
    List<Document> getDocumentsUseCase(String userId);
}
