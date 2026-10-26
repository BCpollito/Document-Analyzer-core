package com.portfolio.documentanalyzer.infrastructure.config;

import com.portfolio.documentanalyzer.application.policy.DocumentAuthorizationPolicy;
import com.portfolio.documentanalyzer.application.port.out.DocumentRepositoryPort;
import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.application.service.DocumentService;
import com.portfolio.documentanalyzer.application.service.IndexDocumentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentUseCaseConfig {

    @Bean
    public DocumentAuthorizationPolicy documentAuthorizationPolicy(DocumentRepositoryPort documentRepositoryPort) {
        return new DocumentAuthorizationPolicy(documentRepositoryPort);
    }

    @Bean
    public IndexDocumentService indexDocumentService(
            DocumentRepositoryPort documentRepositoryPort,
            UserRepositoryPort userRepositoryPort
    ) {
        return new IndexDocumentService(documentRepositoryPort, userRepositoryPort);
    }

    @Bean
    public DocumentService documentService(
            DocumentRepositoryPort documentRepositoryPort,
            DocumentAuthorizationPolicy documentAuthorizationPolicy
    ) {
        return new DocumentService(documentRepositoryPort, documentAuthorizationPolicy);
    }
}
