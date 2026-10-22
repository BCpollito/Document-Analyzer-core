package com.portfolio.documentanalyzer.domain.model;

import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.UserId;

import java.time.Instant;
import java.util.Objects;

public final class Document {
    private final DocumentId id;
    private final UserId userId;
    private String filename;
    private String filePath;
    private DocumentStatus status;
    private final Instant uploadDate;
    private Instant updatedAt;
    private String errorMessage;
    // Constructor privado para forzar el uso de los métodos de fábrica
    private Document(DocumentId id, UserId userId, String filename, String filePath,
                     DocumentStatus status, Instant uploadDate, Instant updatedAt, String errorMessage) {
        this.id = Objects.requireNonNull(id, "El id del documento es obligatorio");
        this.userId = Objects.requireNonNull(userId, "El id del usuario es obligatorio");
        this.filename = validateNotBlank(filename, "El nombre del archivo no puede estar vacío");
        this.filePath = validateNotBlank(filePath, "La ruta del archivo no puede estar vacía");
        this.status = Objects.requireNonNull(status, "El estado del documento es obligatorio");
        this.uploadDate = Objects.requireNonNull(uploadDate, "La fecha de subida es obligatoria");
        this.updatedAt = Objects.requireNonNull(updatedAt, "La fecha de actualización es obligatoria");
        this.errorMessage = errorMessage;
    }

    // Crear un nuevo documento al sistema cuando el usuario lo sube , ademas se marca como pendiente y con fechas actuales
    public static Document create(UserId userId, String filename, String filePath) {
        Instant now = Instant.now();
        return new Document(
                DocumentId.generate(),
                userId,
                filename,
                filePath,
                DocumentStatus.PENDING,
                now,
                now,
                null
        );
    }
    //Reconstituye un documento existente recuperado desde la base de datos (JPA)
    public static Document reconstitute(DocumentId id, UserId userId, String filename, String filePath,
                                        DocumentStatus status, Instant uploadDate, Instant updatedAt, String errorMessage) {
        return new Document(id, userId, filename, filePath, status, uploadDate, updatedAt, errorMessage);
    }

    // Comprobar si el documento le pertenece al usuario
    public boolean isOwnedBy(UserId userId) {
        return this.userId.equals(userId);
    }
    public void markAsProcessing() {
        this.status = DocumentStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }
    public void markAsCompleted() {
        this.status = DocumentStatus.COMPLETED;
        this.errorMessage = null;
        this.updatedAt = Instant.now();
    }
    public void markAsError(String message) {
        this.status = DocumentStatus.ERROR;
        this.errorMessage = message;
        this.updatedAt = Instant.now();
    }
    private static String validateNotBlank(String value, String errorMsg) {
        Objects.requireNonNull(value, errorMsg);
        if (value.isBlank()) {
            throw new IllegalArgumentException(errorMsg);
        }
        return value.trim();
    }

    public DocumentId getId() { return id; }
    public UserId getUserId() { return userId; }
    public String getFilename() { return filename; }
    public String getFilePath() { return filePath; }
    public DocumentStatus getStatus() { return status; }
    public Instant getUploadDate() { return uploadDate; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getErrorMessage() { return errorMessage; }
}
