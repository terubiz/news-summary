CREATE TABLE collection_logs (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    article_count INT         NOT NULL DEFAULT 0,
    status        VARCHAR(50) NOT NULL,
    error_message TEXT,
    executed_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_collection_logs_user_id     ON collection_logs (user_id);
CREATE INDEX idx_collection_logs_executed_at ON collection_logs (executed_at DESC);
CREATE INDEX idx_collection_logs_user_executed ON collection_logs (user_id, executed_at DESC);
