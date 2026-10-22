package com.portfolio.documentanalyzer.domain.model;

import com.portfolio.documentanalyzer.domain.vo.ConversationId;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.Title;
import com.portfolio.documentanalyzer.domain.vo.UserId;

import java.time.Instant;
import java.util.Objects;

public final class Conversation {

    private  ConversationId id;
    private UserId userId;
    private DocumentId documentId;
    private Title title;
    private Instant createdAt;
    private Instant updatedAt;

    public Conversation(ConversationId id, UserId userId, DocumentId documentId, Title title, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "El id de la conversacion es obligatorio");
        this.userId =  Objects.requireNonNull(userId, "El id del usuario es obligatorio");
        this.documentId =  documentId;
        this.title = validateTitle(title.toString());
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creacion de la conversacion es obligatoria");
        this.updatedAt =  Objects.requireNonNull(updatedAt, "La fecha de actualizacion de la conversacion es obligatoria");
    }


    //Generar una conversacion nueva

    public static Conversation create(UserId userId, DocumentId documentId, Title title) {
        Instant now = Instant.now();
        return new Conversation(
                ConversationId.generate(),
                userId,
                documentId,
                title,
                now,
                now
        );
    }
    // Reconstituir una conversacion existente traida desde la base de datos (JPA)
    public static Conversation reconstitute(ConversationId id, UserId userId, DocumentId documentId,
                                            Title title, Instant createdAt, Instant updatedAt) {
        return new Conversation(id, userId, documentId, title, createdAt, updatedAt);
    }

    // Comprobar si la conversacion pertenece a un usuario
    public boolean isOwnedBy(UserId userId) {
        return this.userId.equals(userId);
    }
    public void updateTitle(Title newTitle) {
        this.title = newTitle;
        this.updatedAt = Instant.now();
    }
    private static Title validateTitle(String title) {
        return Title.of(title.trim());
    }

    public ConversationId getId() { return id; }
    public UserId getUserId() { return userId; }
    public DocumentId getDocumentId() { return documentId; }
    public Title getTitle() { return title; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
