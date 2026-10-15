package com.portfolio.documentanalyzer.infrastructure.outbound.ai.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiDtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("AiDocumentIndexRequestDto serializa y deserializa correctamente con snake_case")
    void testAiDocumentIndexRequestDto() throws Exception {
        AiDocumentIndexRequestDto request = new AiDocumentIndexRequestDto(
                "doc-123",
                "Contenido del documento para indexar",
                Map.of("filename", "test.pdf", "category", "finance")
        );

        String json = objectMapper.writeValueAsString(request);
        assertThat(json).contains("\"document_id\":\"doc-123\"");
        assertThat(json).contains("\"content\":\"Contenido del documento para indexar\"");
        assertThat(json).contains("\"metadata\":");

        AiDocumentIndexRequestDto deserialized = objectMapper.readValue(json, AiDocumentIndexRequestDto.class);
        assertThat(deserialized.documentId()).isEqualTo("doc-123");
        assertThat(deserialized.content()).isEqualTo("Contenido del documento para indexar");
        assertThat(deserialized.metadata()).containsEntry("filename", "test.pdf");
    }

    @Test
    @DisplayName("AiDocumentIndexResponseDto serializa y deserializa datos y omitir errores nulos")
    void testAiDocumentIndexResponseDtoSuccess() throws Exception {
        AiDocumentIndexResponseDto.Data data = new AiDocumentIndexResponseDto.Data("doc-123", 10);
        AiDocumentIndexResponseDto response = new AiDocumentIndexResponseDto("SUCCESS", data, (AiDocumentIndexResponseDto.Error) null);

        String json = objectMapper.writeValueAsString(response);
        assertThat(json).contains("\"status\":\"SUCCESS\"");
        assertThat(json).contains("\"data\":{\"document_id\":\"doc-123\",\"chunks_processed\":10}");
        assertThat(json).doesNotContain("\"error\"");

        AiDocumentIndexResponseDto deserialized = objectMapper.readValue(json, AiDocumentIndexResponseDto.class);
        assertThat(deserialized.status()).isEqualTo("SUCCESS");
        assertThat(deserialized.data()).isNotNull();
        assertThat(deserialized.data().documentId()).isEqualTo("doc-123");
        assertThat(deserialized.data().chunksProcessed()).isEqualTo(10);
        assertThat(deserialized.error()).isNull();
    }

    @Test
    @DisplayName("AiDocumentIndexResponseDto deserializa error correctamente")
    void testAiDocumentIndexResponseDtoError() throws Exception {
        String json = """
                {
                    "status": "ERROR",
                    "error": {
                        "code": "EMBEDDING_FAILED",
                        "message": "Model service unavailable"
                    }
                }
                """;

        AiDocumentIndexResponseDto deserialized = objectMapper.readValue(json, AiDocumentIndexResponseDto.class);
        assertThat(deserialized.status()).isEqualTo("ERROR");
        assertThat(deserialized.data()).isNull();
        assertThat(deserialized.error()).isNotNull();
        assertThat(deserialized.error().code()).isEqualTo("EMBEDDING_FAILED");
        assertThat(deserialized.error().message()).isEqualTo("Model service unavailable");
    }

    @Test
    @DisplayName("AiRagQueryRequestDto serializa y deserializa consulta con historial de mensajes")
    void testAiRagQueryRequestDto() throws Exception {
        List<MessageRoleDto> history = List.of(
                MessageRoleDto.user("¿Cuál es el resumen?"),
                MessageRoleDto.assistant("El documento describe...")
        );

        AiRagQueryRequestDto request = new AiRagQueryRequestDto("¿Quién es el autor?", "conv-456", history);

        String json = objectMapper.writeValueAsString(request);
        assertThat(json).contains("\"query\":\"¿Quién es el autor?\"");
        assertThat(json).contains("\"conversation_id\":\"conv-456\"");
        assertThat(json).contains("\"role\":\"USER\"");
        assertThat(json).contains("\"role\":\"ASSISTANT\"");

        AiRagQueryRequestDto deserialized = objectMapper.readValue(json, AiRagQueryRequestDto.class);
        assertThat(deserialized.query()).isEqualTo("¿Quién es el autor?");
        assertThat(deserialized.conversationId()).isEqualTo("conv-456");
        assertThat(deserialized.history()).hasSize(2);
        assertThat(deserialized.history().get(0).role()).isEqualTo("USER");
        assertThat(deserialized.history().get(0).content()).isEqualTo("¿Cuál es el resumen?");
    }

    @Test
    @DisplayName("AiRagQueryResponseDto serializa y deserializa respuesta RAG con fuentes")
    void testAiRagQueryResponseDto() throws Exception {
        AiRagQueryResponseDto.Data data = new AiRagQueryResponseDto.Data(
                "La respuesta generada por RAG",
                List.of("chunk-1", "chunk-2")
        );
        AiRagQueryResponseDto response = new AiRagQueryResponseDto("SUCCESS", data, (AiRagQueryResponseDto.Error) null);

        String json = objectMapper.writeValueAsString(response);
        assertThat(json).contains("\"status\":\"SUCCESS\"");
        assertThat(json).contains("\"answer\":\"La respuesta generada por RAG\"");
        assertThat(json).contains("\"sources\":[\"chunk-1\",\"chunk-2\"]");
        assertThat(json).doesNotContain("\"error\"");

        AiRagQueryResponseDto deserialized = objectMapper.readValue(json, AiRagQueryResponseDto.class);
        assertThat(deserialized.status()).isEqualTo("SUCCESS");
        assertThat(deserialized.data().answer()).isEqualTo("La respuesta generada por RAG");
        assertThat(deserialized.data().sources()).containsExactly("chunk-1", "chunk-2");
        assertThat(deserialized.error()).isNull();
    }
}
