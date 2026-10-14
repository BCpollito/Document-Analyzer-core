
from fastapi import FastAPI

app = FastAPI(
    title="Document Analyzer AI Service",
    version="0.1.0",
)


@app.get("/health", tags=["Health"])
def health_check():
    return {
        "status": "ok",
        "service": "document-analyzer-ai",
    }
