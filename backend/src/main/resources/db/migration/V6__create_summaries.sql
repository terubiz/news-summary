CREATE TABLE summaries (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    summary_text     TEXT         NOT NULL,
    supplement_level VARCHAR(50)  NOT NULL,
    summary_mode     VARCHAR(50)  NOT NULL,
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    retry_count      INT          NOT NULL DEFAULT 0,
    generated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_summaries_user_id      ON summaries (user_id);
CREATE INDEX idx_summaries_status       ON summaries (status);
CREATE INDEX idx_summaries_generated_at ON summaries (generated_at DESC);
CREATE INDEX idx_summaries_user_generated ON summaries (user_id, generated_at DESC);
