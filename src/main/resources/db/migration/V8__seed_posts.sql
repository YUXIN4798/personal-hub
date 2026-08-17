INSERT INTO categories (name, slug, type, sort_order)
VALUES ('学习笔记', 'study-notes', 'post', 1),
       ('工程实践', 'engineering-practice', 'post', 2)
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO tags (name, slug)
VALUES ('Java', 'java'),
       ('MySQL', 'mysql'),
       ('Spring Boot', 'spring-boot'),
       ('后端工程', 'backend-engineering')
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'Java 学习路线复盘',
       'java-learning-roadmap-review',
       '从 Java 基础、Java Web 到 Spring Boot 的阶段性学习方法复盘。',
       '学习 Java 时，最重要的不是把知识点按目录背完，而是尽快形成一条可以运行、可以调试、可以复盘的实践链路。\n\n第一阶段先建立语言基础：集合、异常、IO、面向对象和常用并发工具。每学完一个主题，都用一个小程序验证边界条件，例如集合在空数据、重复数据和并发访问下的行为。\n\n第二阶段进入 Java Web，重点理解请求生命周期、Session、Filter 和数据库事务。最后再用 Spring Boot 重做同一个业务，通过对比可以看清框架替你解决了什么，以及哪些职责仍然必须由自己设计。\n\n目前对我最有效的节奏是：当天完成最小实践，第二天只看代码和测试结果做复盘，每周整理一次可以迁移到下个项目的模板。',
       'published',
       c.id,
       '2026-08-12 09:00:00'
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'java-learning-roadmap-review');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'MySQL 索引实战笔记',
       'mysql-index-practice-notes',
       '从查询条件、联合索引和 EXPLAIN 结果出发，记录索引优化的判断过程。',
       '索引优化的起点应该是慢查询和执行计划，而不是先凭经验添加索引。先确认查询的过滤条件、排序字段和返回行数，再用 EXPLAIN 检查是否命中预期索引。\n\n联合索引需要遵循最左匹配原则。对于 WHERE tenant_id = ? AND status = ? ORDER BY created_at DESC 的列表查询，可以优先考虑 (tenant_id, status, created_at)；但最终是否有效，仍要结合数据分布和实际执行计划验证。\n\n索引不是越多越好。每个索引都会增加写入成本和存储成本，所以完成优化后要回到 INSERT、UPDATE 和分页场景做回归，确认整体收益而不是只看一条 SELECT。',
       'published',
       c.id,
       '2026-08-14 14:30:00'
FROM categories c
WHERE c.slug = 'engineering-practice' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'mysql-index-practice-notes');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'Spring Boot 分层设计检查清单',
       'spring-boot-layering-checklist',
       '用一份简短清单检查 Controller、Service、Repository 的职责边界。',
       'Controller 负责参数绑定、校验和视图返回，不直接访问 Repository，也不承载业务规则。\n\nService 负责事务边界、状态转换和跨 Repository 协作。涉及创建或更新关联数据时，要把主实体与关联表同步放在同一个事务中，并处理空标签、重复 slug 和不存在的外键输入。\n\nRepository 只表达数据访问意图。优先使用 Spring Data 派生查询或带参数的 JPQL，并为公开状态、排序和分页场景补上针对性的测试。',
       'published',
       c.id,
       '2026-08-16 10:15:00'
FROM categories c
WHERE c.slug = 'engineering-practice' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'spring-boot-layering-checklist');

INSERT IGNORE INTO post_tags (post_id, tag_id)
SELECT p.id, t.id
FROM posts p
JOIN (
    SELECT 'java-learning-roadmap-review' AS post_slug, 'Java' AS tag_name
    UNION ALL SELECT 'java-learning-roadmap-review', '后端工程'
    UNION ALL SELECT 'mysql-index-practice-notes', 'MySQL'
    UNION ALL SELECT 'mysql-index-practice-notes', '后端工程'
    UNION ALL SELECT 'spring-boot-layering-checklist', 'Spring Boot'
    UNION ALL SELECT 'spring-boot-layering-checklist', '后端工程'
) tag_links ON tag_links.post_slug = p.slug
JOIN tags t ON t.name = tag_links.tag_name;
