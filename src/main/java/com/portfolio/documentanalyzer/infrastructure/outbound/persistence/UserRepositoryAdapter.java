package com.portfolio.documentanalyzer.infrastructure.outbound.persistence;

import com.portfolio.documentanalyzer.application.port.out.UserRepositoryPort;
import com.portfolio.documentanalyzer.domain.model.Email;
import com.portfolio.documentanalyzer.domain.model.PasswordHash;
import com.portfolio.documentanalyzer.domain.model.User;
import com.portfolio.documentanalyzer.domain.model.UserId;
import com.portfolio.documentanalyzer.domain.model.Username;
import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity.UserEntity;
import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa UserRepositoryPort delegando en Spring Data JPA.
 * Realiza la traducción bidireccional entre la entidad de dominio User y la entidad JPA UserEntity.
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = Objects.requireNonNull(springDataUserRepository, "springDataUserRepository no puede ser nulo");
    }

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "El usuario de dominio no puede ser nulo");

        UserEntity entity = toEntity(user);
        UserEntity savedEntity = springDataUserRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return springDataUserRepository.findById(userId.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        if (email == null) {
            return false;
        }
        return springDataUserRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByUsername(Username username) {
        if (username == null) {
            return false;
        }
        return springDataUserRepository.existsByUsername(username.value());
    }

    @Override
    public java.util.Optional<User> findByEmail(Email email) {
        if (email == null) {
            return java.util.Optional.empty();
        }
        return springDataUserRepository.findByEmail(email.value()).map(this::toDomain);
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId().value(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.getPasswordHash().value(),
                user.getCreatedAt()
        );
    }

    private User toDomain(UserEntity entity) {
        return User.reconstitute(
                new UserId(entity.getId()),
                new Username(entity.getUsername()),
                new Email(entity.getEmail()),
                new PasswordHash(entity.getPasswordHash()),
                entity.getCreatedAt()
        );
    }
}
