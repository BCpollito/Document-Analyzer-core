package com.portfolio.documentanalyzer.infrastructure.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.documentanalyzer.domain.exception.AiServiceIntegrationException;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.ChatMessageDto;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.DocumentIndexRequest;
import com.portfolio.documentanalyzer.infrastructure.inbound.web.dto.RagQueryRequest;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.FastApiClientAdapter;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiDocumentIndexResponseDto;
import com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto.AiRagQueryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentAnalysisControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FastApiClientAdapter fastApiClientAdapter;

    @BeforeEach
    void setUp() {
        DocumentAnalysisController controller = new DocumentAnalysisController(fastApiClientAdapter);
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/documents/index retorna 200 OK y DocumentIndexResponse")
    void indexDocumentShouldReturnOk() throws Exception {
        AiDocumentIndexResponseDto adapterResponse = new AiDocumentIndexResponseDto(
                "success",
                new AiDocumentIndexResponseDto.Data("doc-123", 4),
                (AiDocumentIndexResponseDto.Error) null
        );

        when(fastApiClientAdapter.indexDocument(any())).thenReturn(adapterResponse);

        DocumentIndexRequest request = new DocumentIndexRequest(
                "doc-123",
                "Texto de prueba",
                Map.of("filename", "informe.pdf")
        );

        mockMvc.perform(post("/api/v1/documents/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.document_id").value("doc-123"))
                .andExpect(jsonPath("$.data.chunks_processed").value(4));
    }

    @Test
    @DisplayName("POST /api/v1/rag/query retorna 200 OK y RagQueryResponse")
    void queryRagShouldReturnOk() throws Exception {
        AiRagQueryResponseDto adapterResponse = new AiRagQueryResponseDto(
                "success",
                new AiRagQueryResponseDto.Data("Respuesta generada", List.of("chunk-1", "chunk-2")),
                (AiRagQueryResponseDto.Error) null
        );

        when(fastApiClientAdapter.queryRag(any())).thenReturn(adapterResponse);

        RagQueryRequest request = new RagQueryRequest(
                "¿Cuál es el resumen?",
                "conv-456",
                List.of(ChatMessageDto.user("Hola"))
        );

        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.answer").value("Respuesta generada"))
                .andExpect(jsonPath("$.data.sources[0]").value("chunk-1"))
                .andExpect(jsonPath("$.data.sources[1]").value("chunk-2"));
    }

    @Test
    @DisplayName("POST /api/v1/rag/query maneja AiTimeout retornando 503 con formato de error")
    void queryRagShouldReturn503OnTimeout() throws Exception {
        when(fastApiClientAdapter.queryRag(any())).thenThrow(
                new AiServiceIntegrationException(
                        AiServiceIntegrationException.ERROR_AI_TIMEOUT,
                        "El servicio de IA ha superado el tiempo límite de espera."
                )
        );

        RagQueryRequest request = new RagQueryRequest("¿Qué dice?", "conv-1");

        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("AI_TIMEOUT"))
                .andExpect(jsonPath("$.error.message").value("El servicio de IA ha superado el tiempo límite de espera."));
    }

    @Test
    @DisplayName("POST /api/v1/rag/query maneja AiProcessingError retornando 500 con formato de error")
    void queryRagShouldReturn500OnProcessingError() throws Exception {
        when(fastApiClientAdapter.queryRag(any())).thenThrow(
                new AiServiceIntegrationException(
                        AiServiceIntegrationException.ERROR_AI_PROCESSING,
                        "Error al procesar la solicitud en el servicio de IA."
                )
        );

        RagQueryRequest request = new RagQueryRequest("¿Qué dice?", "conv-1");

        mockMvc.perform(post("/api/v1/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.code").value("AI_PROCESSING_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Error al procesar la solicitud en el servicio de IA."));
    }
}
