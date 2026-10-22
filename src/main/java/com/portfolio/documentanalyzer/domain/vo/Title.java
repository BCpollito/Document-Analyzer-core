package com.portfolio.documentanalyzer.domain.vo;


import com.portfolio.documentanalyzer.domain.exception.InvalidConversationException;

public record Title(String value) {
    public static final int MAX_LENGTH = 50;

    public Title {
        if (value == null || value.isBlank()) {
            throw new InvalidConversationException("El titulo es obligatorio");
        }
        value = value.strip();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidConversationException(
                    "El titulo excede la longitud máxima de " + MAX_LENGTH + " caracteres");
        }
    }

    public static Title of(String value) {
        return new Title(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
