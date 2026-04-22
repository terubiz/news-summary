CREATE TABLE news_articles (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(500) NOT NULL,
    content      TEXT         NOT NULL,
    source_url   VARCHAR(2048) NOT NULL UNIQUE,
    source_name  VARCHAR(255) NOT NULL,
    published_at TIMESTAMP    NOT NULL,
    collected_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_news_articles_source_url   ON news_articles (source_url);
CREATE INDEX idx_news_articles_title        ON news_articles (title);
CREATE INDEX idx_news_articles_collected_at ON news_articles (collected_at);
CREATE INDEX idx_news_articles_published_at ON news_articles (published_at);
