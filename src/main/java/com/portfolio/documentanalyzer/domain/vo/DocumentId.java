package com.portfolio.documentanalyzer.domain.vo;

import com.portfolio.documentanalyzer.domain.exception.InvalidDocumentUploadException;
import java.util.UUID;

public record DocumentId(UUID value) {
    public DocumentId {
        if (value == null) {
            throw new InvalidDocumentUploadException("Fallo al subir el documento, id no puede ser nulo");
        }
    }

    public static DocumentId generate() {
        return new DocumentId(UUID.randomUUID());
    }

    public static DocumentId of(UUID value) {
        return new DocumentId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
