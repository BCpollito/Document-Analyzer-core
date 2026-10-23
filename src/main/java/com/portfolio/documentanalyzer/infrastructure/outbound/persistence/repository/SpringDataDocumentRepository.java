package com.portfolio.documentanalyzer.infrastructure.outbound.persistence.repository;

import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataDocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    Optional<DocumentEntity> findByIdAndUserId(UUID id, UUID userId);


    List<DocumentEntity> findAllByUserId(UUID userId);


    boolean existsByIdAndUserId(UUID id, UUID userId);


    void deleteByIdAndUserId(UUID id, UUID userId);
}
