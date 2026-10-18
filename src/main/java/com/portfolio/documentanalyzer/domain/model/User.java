package com.portfolio.documentanalyzer.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio User (Rich Domain Model).
 * <p>
 * Compone los Value Objects UserId, Username, Email y PasswordHash.
 * Solo almacena el hash de la contraseña, nunca el texto plano. Las políticas de contraseña
 * (longitud, caracteres, etc.) son responsabilidad del caso de uso previo al hash.
 */
public final class User {

    private final UserId id;
    private Username username;
    private Email email;
    private PasswordHash passwordHash;
    private final Instant createdAt;

    private User(UserId id, Username username, Email email, PasswordHash passwordHash, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "El id del usuario es obligatorio");
        this.username = Objects.requireNonNull(username, "El username es obligatorio");
        this.email = Objects.requireNonNull(email, "El email es obligatorio");
        this.passwordHash = Objects.requireNonNull(passwordHash, "El hash de la contraseña es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación del usuario es obligatoria");
    }

    /**
     * Crea un usuario nuevo para el registro a partir de Value Objects.
     */
    public static User createRegistration(Username username, Email email, PasswordHash passwordHash) {
        return new User(
                UserId.generate(),
                username,
                email,
                passwordHash,
                Instant.now());
    }

    /**
     * Sobrecarga de conveniencia para crear un usuario a partir de valores primitivos.
     */
    public static User createRegistration(String username, String email, String passwordHash) {
        return createRegistration(new Username(username), new Email(email), new PasswordHash(passwordHash));
    }

    /**
     * Reconstituye un usuario existente desde persistencia con Value Objects.
     */
    public static User reconstitute(UserId id, Username username, Email email, PasswordHash passwordHash, Instant createdAt) {
        return new User(id, username, email, passwordHash, createdAt);
    }

    /**
     * Sobrecarga de conveniencia para reconstituir un usuario desde persistencia con valores primitivos.
     */
    public static User reconstitute(UUID id, String username, String email, String passwordHash, Instant createdAt) {
        return reconstitute(new UserId(id), new Username(username), new Email(email), new PasswordHash(passwordHash), createdAt);
    }

    /** Cambia el username manteniendo las invariantes. */
    public void changeUsername(Username newUsername) {
        this.username = Objects.requireNonNull(newUsername, "El username es obligatorio");
    }

    public void changeUsername(String newUsername) {
        changeUsername(new Username(newUsername));
    }

    /** Cambia el email manteniendo las invariantes. */
    public void changeEmail(Email newEmail) {
        this.email = Objects.requireNonNull(newEmail, "El email es obligatorio");
    }

    public void changeEmail(String newEmail) {
        changeEmail(new Email(newEmail));
    }

    /** Reemplaza el hash de la contraseña. */
    public void changePasswordHash(PasswordHash newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "El hash de la contraseña es obligatorio");
    }

    public void changePasswordHash(String newPasswordHash) {
        changePasswordHash(new PasswordHash(newPasswordHash));
    }

    public UserId getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof User other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id.value() + ", username='" + username.value() + "', email='" + email.value() + "'}";
    }
}
