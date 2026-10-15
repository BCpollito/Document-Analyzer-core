from typing import Dict, List, Optional
from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(
    title="Document Analyzer AI Service",
    version="0.1.0",
)


# ==========================================
# Pydantic Models (DTOs equivalentes)
# ==========================================

class MessageRole(BaseModel):
    role: str
    content: str


class AiError(BaseModel):
    code: str
    message: str


class AiDocumentIndexRequest(BaseModel):
    document_id: str
    content: str
    metadata: Optional[Dict[str, str]] = None


class AiDocumentIndexData(BaseModel):
    document_id: str
    chunks_processed: int


class AiDocumentIndexResponse(BaseModel):
    status: str
    data: Optional[AiDocumentIndexData] = None
    error: Optional[AiError] = None


class AiRagQueryRequest(BaseModel):
    query: str
    conversation_id: Optional[str] = None
    history: List[MessageRole] = Field(default_factory=list)


class AiRagQueryData(BaseModel):
    answer: str
    sources: List[str] = Field(default_factory=list)


class AiRagQueryResponse(BaseModel):
    status: str
    data: Optional[AiRagQueryData] = None
    error: Optional[AiError] = None


class HealthResponse(BaseModel):
    status: str


# ==========================================
# Endpoints
# ==========================================

@app.get("/health", tags=["Health"])
def health_legacy():
    return {
        "status": "up",
        "service": "document-analyzer-ai",
    }


@app.get("/api/v1/health", response_model=HealthResponse, tags=["Health"])
def health_check() -> HealthResponse:
    return HealthResponse(status="up")


@app.post(
    "/api/v1/documents/index",
    response_model=AiDocumentIndexResponse,
    response_model_exclude_none=True,
    tags=["Documents"],
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
)
def query_rag(request: AiRagQueryRequest) -> AiRagQueryResponse:
    return AiRagQueryResponse(
        status="success",
        data=AiRagQueryData(
            answer=f"Mock response for query: {request.query}",
            sources=["chunk-1", "chunk-2"],
        ),
    )
