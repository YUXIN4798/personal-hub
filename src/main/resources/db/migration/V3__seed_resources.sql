ALTER TABLE resources
    ADD COLUMN description VARCHAR(2000) AFTER summary,
    ADD COLUMN file_path VARCHAR(500) AFTER type,
    ADD COLUMN original_name VARCHAR(255) AFTER file_path,
    ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0 AFTER original_name,
    ADD COLUMN version VARCHAR(32) NOT NULL DEFAULT 'v1.0' AFTER file_size,
    ADD COLUMN download_count BIGINT NOT NULL DEFAULT 0 AFTER version,
    ADD COLUMN visibility VARCHAR(32) NOT NULL DEFAULT 'public' AFTER download_count,
    ADD COLUMN checksum CHAR(64) AFTER visibility;

INSERT INTO categories (name, slug, type, sort_order)
VALUES
    ('开发文档', 'dev-docs', 'resource', 1),
    ('学习笔记', 'study-notes', 'resource', 2),
    ('工具配置', 'tool-configs', 'resource', 3),
    ('课件资料', 'courseware', 'resource', 4);

INSERT INTO tags (name, slug)
VALUES
    ('Spring Boot', 'spring-boot'),
    ('Linux', 'linux'),
    ('Git', 'git'),
    ('WSL2', 'wsl2'),
    ('JavaWeb', 'javaweb-course')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO resources (
    title, slug, summary, description, url, type, file_path, original_name, file_size,
    version, download_count, visibility, checksum, category_id
)
SELECT 'Java 面试八股文整理.pdf',
       'java-interview-notes',
       '围绕 Java 基础、集合、并发和 JVM 的面试知识整理。',
       '示例资源数据，文件路径与 checksum 均为占位值，真实上传下载文件将在后续任务接入。',
       '/uploads/placeholder-java-interview-notes.pdf',
       'file',
       '/uploads/placeholder-java-interview-notes.pdf',
       'Java 面试八股文整理.pdf',
       524288,
       'v1.0',
       0,
       'public',
       'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
       c.id
FROM categories c
WHERE c.slug = 'dev-docs' AND c.type = 'resource';

INSERT INTO resources (
    title, slug, summary, description, url, type, file_path, original_name, file_size,
    version, download_count, visibility, checksum, category_id
)
SELECT 'Spring Boot 常用注解速查表.md',
       'spring-boot-annotations-cheatsheet',
       '整理 Spring Boot 开发中常见注解的用途与典型使用场景。',
       '示例资源数据，文件路径与 checksum 均为占位值，真实上传下载文件将在后续任务接入。',
       '/uploads/placeholder-spring-boot-annotations-cheatsheet.pdf',
       'file',
       '/uploads/placeholder-spring-boot-annotations-cheatsheet.pdf',
       'Spring Boot 常用注解速查表.md',
       131072,
       'v1.0',
       0,
       'public',
       'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
       c.id
FROM categories c
WHERE c.slug = 'dev-docs' AND c.type = 'resource';

INSERT INTO resources (
    title, slug, summary, description, url, type, file_path, original_name, file_size,
    version, download_count, visibility, checksum, category_id
)
SELECT 'MySQL 索引优化笔记.md',
       'mysql-index-optimization-notes',
       '记录 MySQL 索引设计、执行计划和常见优化判断。',
       '示例资源数据，文件路径与 checksum 均为占位值，真实上传下载文件将在后续任务接入。',
       '/uploads/placeholder-mysql-index-optimization-notes.pdf',
       'file',
       '/uploads/placeholder-mysql-index-optimization-notes.pdf',
       'MySQL 索引优化笔记.md',
       196608,
       'v1.0',
       0,
       'public',
       'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc',
       c.id
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'resource';

INSERT INTO resources (
    title, slug, summary, description, url, type, file_path, original_name, file_size,
    version, download_count, visibility, checksum, category_id
)
SELECT 'WSL2 开发环境配置清单.md',
       'wsl2-dev-environment-checklist',
       '沉淀 Windows + WSL2 下 Java 后端开发环境配置步骤。',
       '示例资源数据，文件路径与 checksum 均为占位值，真实上传下载文件将在后续任务接入。',
       '/uploads/placeholder-wsl2-dev-environment-checklist.pdf',
       'file',
       '/uploads/placeholder-wsl2-dev-environment-checklist.pdf',
       'WSL2 开发环境配置清单.md',
       98304,
       'v1.0',
       0,
       'public',
       'dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd',
       c.id
FROM categories c
WHERE c.slug = 'tool-configs' AND c.type = 'resource';

INSERT INTO resources (
    title, slug, summary, description, url, type, file_path, original_name, file_size,
    version, download_count, visibility, checksum, category_id
)
SELECT '天狮学院 JavaWeb 课件合集.zip',
       'tianshi-javaweb-courseware',
       'JavaWeb 课程学习资料的合集入口，便于后续统一整理。',
       '示例资源数据，文件路径与 checksum 均为占位值，真实上传下载文件将在后续任务接入。',
       '/uploads/placeholder-tianshi-javaweb-courseware.pdf',
       'file',
       '/uploads/placeholder-tianshi-javaweb-courseware.pdf',
       '天狮学院 JavaWeb 课件合集.zip',
       10485760,
       'v1.0',
       0,
       'public',
       'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee',
       c.id
FROM categories c
WHERE c.slug = 'courseware' AND c.type = 'resource';

INSERT INTO resource_tags (resource_id, tag_id)
SELECT r.id, t.id
FROM resources r
JOIN tags t ON
    (r.slug = 'java-interview-notes' AND t.slug IN ('java'))
    OR (r.slug = 'spring-boot-annotations-cheatsheet' AND t.slug IN ('java', 'spring-boot'))
    OR (r.slug = 'mysql-index-optimization-notes' AND t.slug IN ('mysql'))
    OR (r.slug = 'wsl2-dev-environment-checklist' AND t.slug IN ('linux', 'git', 'wsl2'))
    OR (r.slug = 'tianshi-javaweb-courseware' AND t.slug IN ('java', 'javaweb-course'));
