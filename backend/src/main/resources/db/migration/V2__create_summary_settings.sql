CREATE TABLE summary_settings (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    selected_indices      VARCHAR(50)[] NOT NULL DEFAULT '{}',
    analysis_perspectives VARCHAR(100)[] NOT NULL DEFAULT '{}',
    supplement_level      VARCHAR(50)  NOT NULL DEFAULT 'INTERMEDIATE',
    summary_mode          VARCHAR(50)  NOT NULL DEFAULT 'STANDARD',
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_summary_settings_user_id ON summary_settings (user_id);
