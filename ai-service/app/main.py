from typing import Dict, List, Optional
from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(
    title="AI Document Analyzer Service",
    description="Microservicio interno para procesamiento vectorial y RAG",
    version="1.0.0",
)


# ==========================================
# Pydantic Models (DTOs equivalentes)
# ==========================================

class MessageRole(BaseModel):
    role: str = Field(
        ...,
        description="Rol del remitente en la conversación (USER, ASSISTANT o SYSTEM)",
        examples=["USER"],
    )
    content: str = Field(
        ...,
        description="Contenido textual del mensaje",
        examples=["¿Cuál es el tema principal del documento?"],
    )


class AiError(BaseModel):
    code: str = Field(
        ...,
        description="Código de error de la operación de IA",
        examples=["EMBEDDING_FAILED"],
    )
    message: str = Field(
        ...,
        description="Descripción legible del error ocurrido",
        examples=["No se pudo conectar con el servicio de embeddings"],
    )


class AiDocumentIndexRequest(BaseModel):
    document_id: str = Field(
        ...,
        description="Identificador único del documento a procesar e indexar",
        examples=["doc-123"],
    )

    user_id: Optional[str] = Field(
        default=None,
        description="Identificador único del usuario",
        examples=["user-123"],
    )
    content: str = Field(
        ...,
        description="Texto completo extraído del documento para vectorización",
        examples=["Contenido textual del documento a indexar..."],
    )
    metadata: Optional[Dict[str, str]] = Field(
        default=None,
        description="Metadatos asociados al documento (archivo, categoría, etc.)",
        examples=[{"filename": "informe_financiero.pdf", "category": "finanzas"}],
    )


class AiDocumentIndexData(BaseModel):
    document_id: str = Field(
        ...,
        description="Identificador único del documento procesado",
        examples=["doc-123"],
    )
    chunks_processed: int = Field(
        ...,
        description="Cantidad de fragmentos generados, vectorizados y almacenados",
        examples=[5],
    )


class AiDocumentIndexResponse(BaseModel):
    status: str = Field(
        ...,
        description="Estado del resultado de la operación (success o error)",
        examples=["success"],
    )
    data: Optional[AiDocumentIndexData] = Field(
        default=None,
        description="Información detallada del documento indexado en caso de éxito",
    )
    error: Optional[AiError] = Field(
        default=None,
        description="Detalle del error en caso de que la indexación falle",
    )


class AiRagQueryRequest(BaseModel):
    query: str = Field(
        ...,
        description="Pregunta del usuario en lenguaje natural",
        examples=["¿Cuáles son las conclusiones financieras del documento?"],
    )

    user_id: Optional[str] = Field(
        default=None,
        description="Identificador único del usuario",
        examples=["user-123"],
    )

    conversation_id: Optional[str] = Field(
        default=None,
        description="Identificador único de la conversación para mantener el contexto",
        examples=["conv-456"],
    )
    history: List[MessageRole] = Field(
        default_factory=list,
        description="Historial previo de mensajes de la conversación",
    )


class AiRagQueryData(BaseModel):
    answer: str = Field(
        ...,
        description="Respuesta generada por el modelo a partir de los documentos recuperados",
        examples=["Los ingresos netos aumentaron un 15% respecto al año anterior."],
    )
    sources: List[str] = Field(
        default_factory=list,
        description="Lista de fragmentos o chunks utilizados como referencia",
        examples=[["doc-123_chunk_0", "doc-123_chunk_1"]],
    )


class AiRagQueryResponse(BaseModel):
    status: str = Field(
        ...,
        description="Estado general de la respuesta (success o error)",
        examples=["success"],
    )
    data: Optional[AiRagQueryData] = Field(
        default=None,
        description="Datos de la respuesta generada por RAG junto con las fuentes consultadas",
    )
    error: Optional[AiError] = Field(
        default=None,
        description="Detalles del error si la consulta RAG falló",
    )


class HealthResponse(BaseModel):
    status: str = Field(
        ...,
        description="Estado operativo actual del servicio de IA",
        examples=["up"],
    )


# ==========================================
# Endpoints
# ==========================================

@app.get(
    "/health",
    tags=["Health"],
    summary="Verificar salud del servicio (legacy)",
    description="Endpoint simple de comprobación para monitoreo de contenedores y Docker.",
)
def health_legacy():
    return {
        "status": "up",
        "service": "document-analyzer-ai",
    }


@app.get(
    "/api/v1/health",
    response_model=HealthResponse,
    tags=["Health"],
    summary="Verificar estado operativo del servicio",
    description="Comprueba que el microservicio de IA esté en línea y listo para recibir peticiones.",
    responses={
        200: {
            "description": "El microservicio está en funcionamiento y saludable",
            "model": HealthResponse,
        }
    },
)
def health_check() -> HealthResponse:
    return HealthResponse(status="up")


@app.post(
    "/api/v1/documents/index",
    response_model=AiDocumentIndexResponse,
    response_model_exclude_none=True,
    tags=["Documents"],
    summary="Indexar documento para búsqueda vectorial",
    description="Recibe el contenido y metadatos de un documento, lo divide en fragmentos, genera sus embeddings vectoriales y los almacena en pgvector.",
    responses={
        200: {
            "description": "Documento fragmentado y almacenado correctamente en la base de datos vectorial",
            "model": AiDocumentIndexResponse,
        },
        500: {
            "description": "Error al procesar el texto o generar los embeddings vectoriales",
            "model": AiDocumentIndexResponse,
        },
    },
)
def index_document(request: AiDocumentIndexRequest) -> AiDocumentIndexResponse:
    return AiDocumentIndexResponse(
        status="success",
        data=AiDocumentIndexData(
            document_id=request.document_id,
            chunks_processed=1,
        ),
    )


@app.post(
    "/api/v1/rag/query",
    response_model=AiRagQueryResponse,
    response_model_exclude_none=True,
    tags=["RAG"],
    summary="Ejecutar consulta RAG sobre el corpus documental",
    description="Realiza una búsqueda por similitud semántica en la base vectorial con la consulta del usuario y genera una respuesta contextualizada usando el LLM.",
    responses={
        200: {
            "description": "Respuesta RAG sintetizada correctamente junto a las fuentes utilizadas",
            "model": AiRagQueryResponse,
        },
        500: {
            "description": "Error al consultar la base vectorial o al generar la respuesta con el LLM",
            "model": AiRagQueryResponse,
        },
    },
)
def query_rag(request: AiRagQueryRequest) -> AiRagQueryResponse:
    sources = [f"user_{request.user_id}_doc_chunk_1", f"user_{request.user_id}_doc_chunk_2"] if request.user_id else ["chunk-1", "chunk-2"]
    return AiRagQueryResponse(
        status="success",
        data=AiRagQueryData(
            answer=f"Mock response for query: {request.query}",
            sources=sources,
        ),
    )
