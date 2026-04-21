CREATE TABLE delivery_logs (
    id            BIGSERIAL PRIMARY KEY,
    channel_id    BIGINT       NOT NULL REFERENCES delivery_channels (id) ON DELETE CASCADE,
    summary_id    BIGINT       NOT NULL REFERENCES summaries (id) ON DELETE CASCADE,
    status        VARCHAR(50)  NOT NULL,
    retry_count   INT          NOT NULL DEFAULT 0,
    error_message TEXT,
    sent_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_logs_channel_id  ON delivery_logs (channel_id);
CREATE INDEX idx_delivery_logs_summary_id  ON delivery_logs (summary_id);
CREATE INDEX idx_delivery_logs_status      ON delivery_logs (status);
CREATE INDEX idx_delivery_logs_channel_summary ON delivery_logs (channel_id, summary_id);
