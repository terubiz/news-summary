-- Summary ↔ Index impact mapping
CREATE TABLE summary_index_impacts (
    id               BIGSERIAL PRIMARY KEY,
    summary_id       BIGINT       NOT NULL REFERENCES summaries (id) ON DELETE CASCADE,
    index_symbol     VARCHAR(50)  NOT NULL,
    impact_direction VARCHAR(50)  NOT NULL
);

CREATE INDEX idx_summary_index_impacts_summary_id ON summary_index_impacts (summary_id);
CREATE INDEX idx_summary_index_impacts_symbol     ON summary_index_impacts (index_symbol);

-- Summary ↔ NewsArticle many-to-many join table
CREATE TABLE summary_source_articles (
    summary_id BIGINT NOT NULL REFERENCES summaries (id) ON DELETE CASCADE,
    article_id BIGINT NOT NULL REFERENCES news_articles (id) ON DELETE CASCADE,
    PRIMARY KEY (summary_id, article_id)
);

CREATE INDEX idx_summary_source_articles_summary_id ON summary_source_articles (summary_id);
CREATE INDEX idx_summary_source_articles_article_id ON summary_source_articles (article_id);
