package com.portfolio.documentanalyzer.domain.model;

import com.portfolio.documentanalyzer.domain.exception.InvalidUserRegistrationException;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Entidad de dominio User (Rich Domain Model).
 * <p>
 * Solo almacena el hash de la contraseña, nunca el texto plano. Las políticas de contraseña
 * (longitud, caracteres, etc.) son responsabilidad del caso de uso previo al hash.
 */
public final class User {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_PASSWORD_HASH_LENGTH = 255;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9](?:[A-Za-z0-9._%+-]*[A-Za-z0-9])?@"
                    + "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$");

    private final UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private final Instant createdAt;

    private User(UUID id, String username, String email, String passwordHash, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    /**
     * Crea un usuario nuevo para el registro.
     *
     * @param username     nombre de usuario (obligatorio, máximo 50 caracteres)
     * @param email        email del usuario (se normaliza a minúsculas y sin espacios laterales)
     * @param passwordHash hash ya procesado de la contraseña
     * @throws InvalidUserRegistrationException si el username o el email son inválidos o el hash está vacío
     */
    public static User createRegistration(String username, String email, String passwordHash) {
        return new User(
                UUID.randomUUID(),
                normalizeAndValidateUsername(username),
                normalizeAndValidateEmail(email),
                validatePasswordHash(passwordHash),
                Instant.now());
    }

    /**
     * Reconstituye un usuario existente desde persistencia (aplica las mismas invariantes).
     */
    public static User reconstitute(UUID id, String username, String email, String passwordHash, Instant createdAt) {
        if (id == null) {
            throw new InvalidUserRegistrationException("El id del usuario es obligatorio");
        }
        if (createdAt == null) {
            throw new InvalidUserRegistrationException("La fecha de creación del usuario es obligatoria");
        }
        return new User(id, normalizeAndValidateUsername(username), normalizeAndValidateEmail(email),
                validatePasswordHash(passwordHash), createdAt);
    }

    /** Cambia el username manteniendo las invariantes. */
    public void changeUsername(String newUsername) {
        this.username = normalizeAndValidateUsername(newUsername);
    }

    private static String normalizeAndValidateUsername(String rawUsername) {
        if (rawUsername == null || rawUsername.isBlank()) {
            throw new InvalidUserRegistrationException("El username es obligatorio");
        }
        String normalized = rawUsername.strip();
        if (normalized.length() > MAX_USERNAME_LENGTH) {
            throw new InvalidUserRegistrationException(
                    "El username excede la longitud máxima de " + MAX_USERNAME_LENGTH + " caracteres");
        }
        return normalized;
    }

    /** Cambia el email manteniendo las invariantes. */
    public void changeEmail(String newEmail) {
        this.email = normalizeAndValidateEmail(newEmail);
    }

    /** Reemplaza el hash de la contraseña (ya procesado por el caso de uso). */
    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = validatePasswordHash(newPasswordHash);
    }

    private static String normalizeAndValidateEmail(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new InvalidUserRegistrationException("El email es obligatorio");
        }
        String normalized = rawEmail.strip().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_EMAIL_LENGTH || normalized.contains("..")
                || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidUserRegistrationException("El formato del email no es válido");
        }
        return normalized;
    }

    private static String validatePasswordHash(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new InvalidUserRegistrationException("El hash de la contraseña es obligatorio");
        }
        if (hash.length() > MAX_PASSWORD_HASH_LENGTH) {
            throw new InvalidUserRegistrationException("El hash de la contraseña excede la longitud máxima permitida");
        }
        return hash;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
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
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
