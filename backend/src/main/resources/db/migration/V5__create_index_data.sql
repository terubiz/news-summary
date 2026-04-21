CREATE TABLE index_data (
    id             BIGSERIAL PRIMARY KEY,
    symbol         VARCHAR(50)    NOT NULL,
    current_value  DECIMAL(18, 4) NOT NULL,
    change_amount  DECIMAL(18, 4) NOT NULL,
    change_rate    DECIMAL(10, 6) NOT NULL,
    is_stale       BOOLEAN        NOT NULL DEFAULT FALSE,
    fetched_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_index_data_symbol     ON index_data (symbol);
CREATE INDEX idx_index_data_fetched_at ON index_data (fetched_at);
CREATE INDEX idx_index_data_symbol_fetched_at ON index_data (symbol, fetched_at DESC);
