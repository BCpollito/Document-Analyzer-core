CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_id UUID REFERENCES documents(id) ON DELETE SET NULL,
    title VARCHAR(255),
     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_conversations_title_not_blank CHECK (title IS NULL OR length(trim(title)) > 0)
);

CREATE TABLE conversation_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_conversation_messages_role CHECK (role IN ('USER', 'ASSISTANT')),
    CONSTRAINT chk_conversation_messages_content_not_blank CHECK (length(trim(content)) > 0)
);

CREATE INDEX idx_conversations_user_updated
    ON conversations(user_id, updated_at DESC);

CREATE INDEX idx_conversations_document_id
    ON conversations(document_id);

CREATE INDEX idx_conversation_messages_conversation_created
    ON conversation_messages(conversation_id, created_at);