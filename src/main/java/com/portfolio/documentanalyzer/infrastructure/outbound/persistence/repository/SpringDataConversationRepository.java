package com.portfolio.documentanalyzer.infrastructure.outbound.persistence.repository;

import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SpringDataConversationRepository extends JpaRepository<ConversationEntity, UUID> {
    Optional<ConversationEntity> findByIdAndUserId(UUID id, UUID userId);


    List<ConversationEntity> findAllByUserId(UUID userId);


    boolean existsByIdAndUserId(UUID id, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);

}
