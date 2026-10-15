package com.portfolio.documentanalyzer.infrastructure.outbound.ai;

import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexResponseDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryRequestDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryResponseDto;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Getter
@Component
public class FastApiClientAdapter {

    private static final Logger log = LoggerFactory.getLogger(FastApiClientAdapter.class);

    private final RestClient restClient;

    public FastApiClientAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${ai-service.url:http://ai-service:8000}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public boolean checkHealth() {
        try {
            ResponseEntity<Void> response = restClient.get()
                    .uri("/api/v1/health")
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode().isSameCodeAs(HttpStatus.OK);
        } catch (Exception e) {
            log.warn("Health check failed for AI service: {}", e.getMessage());
            return false;
        }
    }

    public AiDocumentIndexResponseDto indexDocument(AiDocumentIndexRequestDto request) {
        return restClient.post()
                .uri("/api/v1/documents/index")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiDocumentIndexResponseDto.class);
    }

    public AiRagQueryResponseDto queryRag(AiRagQueryRequestDto request) {
        return restClient.post()
                .uri("/api/v1/rag/query")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiRagQueryResponseDto.class);
    }
}

