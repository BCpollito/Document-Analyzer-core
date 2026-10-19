package com.portfolio.documentanalyzer.infrastructure.outbound.persistence.repository;

import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad UserEntity.
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    java.util.Optional<UserEntity> findByEmail(String email);
}
