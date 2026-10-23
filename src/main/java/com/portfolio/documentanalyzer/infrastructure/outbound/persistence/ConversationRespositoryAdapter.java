package com.portfolio.documentanalyzer.infrastructure.outbound.persistence;

import com.portfolio.documentanalyzer.application.port.out.ConversationRepositoryPort;
import com.portfolio.documentanalyzer.domain.model.Conversation;
import com.portfolio.documentanalyzer.domain.model.Document;
import com.portfolio.documentanalyzer.domain.vo.ConversationId;
import com.portfolio.documentanalyzer.domain.vo.DocumentId;
import com.portfolio.documentanalyzer.domain.vo.Title;
import com.portfolio.documentanalyzer.domain.vo.UserId;
import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity.ConversationEntity;
import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.entity.DocumentEntity;
import com.portfolio.documentanalyzer.infrastructure.outbound.persistence.repository.SpringDataConversationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class ConversationRespositoryAdapter implements ConversationRepositoryPort {

    private final SpringDataConversationRepository springDataConversationRepository;

    public ConversationRespositoryAdapter(SpringDataConversationRepository springDataConversationRepository) {
        this.springDataConversationRepository = Objects.requireNonNull(springDataConversationRepository, "springDataConversationReposiroty no puede esta nulo");

    }

    @Override
    public Optional<Conversation> findByIdAndUserId(ConversationId conversationId, UserId userId){
        return springDataConversationRepository.findByIdAndUserId(conversationId.value(), userId.value())
                .map(this::toDomain);
    }

    @Override
    public List<Conversation> findAllByUserId(UserId userId){
        return springDataConversationRepository.findAllByUserId(userId.value())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByIdAndUserId(ConversationId conversationId, UserId userId){
        if (conversationId == null){
            return false;
        }

        return springDataConversationRepository.existsByIdAndUserId(conversationId.value(), userId.value());
    }
    private ConversationEntity toEntity(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        return new ConversationEntity(
                conversation.getId().value(),
                conversation.getUserId().value(),
                conversation.getDocumentId().value(),
                conversation.getTitle().value(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private Conversation toDomain(ConversationEntity entity) {
        if (entity == null) {
            return null;
        }
        return Conversation.reconstitute(
                ConversationId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                DocumentId.of(entity.getDocumentId()),
                Title.of(entity.getTitle()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()

        );
    }
}
