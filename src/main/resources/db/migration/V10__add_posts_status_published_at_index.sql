ALTER TABLE posts
    ADD INDEX idx_posts_status_published_at (status, published_at);
