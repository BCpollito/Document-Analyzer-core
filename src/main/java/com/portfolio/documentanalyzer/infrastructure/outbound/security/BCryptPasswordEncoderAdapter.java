package com.portfolio.documentanalyzer.infrastructure.outbound.security;

import com.portfolio.documentanalyzer.application.port.out.PasswordEncoderPort;
import com.portfolio.documentanalyzer.application.port.out.PasswordHasherPort;
import com.portfolio.documentanalyzer.domain.model.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Adaptador de seguridad para la generación de hashes de contraseñas mediante BCrypt.
 * Cumple con la restricción de que la base de datos nunca almacene contraseñas en texto plano.
 */
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort, PasswordHasherPort {

    private final BCryptPasswordEncoder passwordEncoder;

    public BCryptPasswordEncoderAdapter() {
        this(new BCryptPasswordEncoder());
    }

    public BCryptPasswordEncoderAdapter(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder no puede ser nulo");
    }

    @Override
    public String encode(String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword no puede ser nulo");
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String raw, String hash){
        return passwordEncoder.matches(raw, hash);
    }
}
