CREATE TABLE delivery_channels (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    channel_type      VARCHAR(50)   NOT NULL,
    encrypted_config  TEXT          NOT NULL,
    delivery_schedule VARCHAR(100)  NOT NULL DEFAULT 'IMMEDIATE',
    filter_indices    VARCHAR(50)[] NOT NULL DEFAULT '{}',
    enabled           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_channels_user_id ON delivery_channels (user_id);
CREATE INDEX idx_delivery_channels_enabled ON delivery_channels (enabled);
CREATE INDEX idx_delivery_channels_user_enabled ON delivery_channels (user_id, enabled);
