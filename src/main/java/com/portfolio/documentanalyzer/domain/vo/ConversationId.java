package com.portfolio.documentanalyzer.domain.vo;

import com.portfolio.documentanalyzer.domain.exception.InvalidConversationException;

import java.util.UUID;

public record ConversationId(UUID value) {
    public ConversationId {
        if (value == null) {
            throw new InvalidConversationException("Fallo al crear nueva conversacion, id no puede ser nulo");
        }
    }

    public static ConversationId generate() {
        return new ConversationId(UUID.randomUUID());
    }

    public static ConversationId of(UUID value) {
        return new ConversationId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
