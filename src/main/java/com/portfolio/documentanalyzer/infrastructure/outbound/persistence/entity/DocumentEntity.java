package com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity;

import com.portfolio.documentanalyzer.domain.model.DocumentStatus;
import jakarta.persistence.*;


import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id
    @Column (name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "filename", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(name = "upload_date", nullable = false)
    private Instant uploadDate;

    @Column(name = "updated_at", nullable = false)
    private Instant updateAt;

    @Column(name = "error_message", nullable = true)
    private String errorMessage;

    protected DocumentEntity(){

    }

    public DocumentEntity(UUID id, UUID userId, String fileName, String filePath, DocumentStatus status, Instant uploadDate, Instant updateAt, String errorMessage) {
        this.id = id;
        this.userId = userId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
        this.uploadDate = uploadDate;
        this.updateAt = updateAt;
        this.errorMessage = errorMessage;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Instant getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Instant updateAt) {
        this.updateAt = updateAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
