CREATE INDEX IF NOT EXISTS idx_news_created_on_desc_id
    ON news (created_on DESC, id);
