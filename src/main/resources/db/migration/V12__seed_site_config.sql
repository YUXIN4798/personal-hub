INSERT INTO site_config (config_key, config_value)
VALUES
    ('homepage.hero.title', '把想法\n做成系统。'),
    ('homepage.hero.subtitle', '曹铭轩 · Java 后端方向 · AI 应用实践者\n在真实项目里磨代码，在每次迭代里练判断。'),
    ('homepage.projects.count', '3'),
    ('homepage.notes.count', '3'),
    ('homepage.resources.count', '3')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
