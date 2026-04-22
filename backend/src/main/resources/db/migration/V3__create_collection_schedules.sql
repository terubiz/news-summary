CREATE TABLE collection_schedules (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    cron_expression VARCHAR(100) NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_collection_schedules_user_id ON collection_schedules (user_id);
CREATE INDEX idx_collection_schedules_enabled  ON collection_schedules (enabled);
