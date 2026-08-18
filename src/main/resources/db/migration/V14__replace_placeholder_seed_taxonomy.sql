-- V14: 清理旧占位种子并新增真实素材分类与标签

DELETE rt FROM resource_tags rt
JOIN resources r ON r.id = rt.resource_id
WHERE r.slug IN (
    'java-interview-notes',
    'spring-boot-annotations-cheatsheet',
    'mysql-index-optimization-notes',
    'wsl2-dev-environment-checklist',
    'tianshi-javaweb-courseware'
);

DELETE pt FROM project_tags pt
JOIN projects p ON p.id = pt.project_id
WHERE p.slug IN (
    'finally-system-exam',
    'lab2-javaweb',
    'mybatis-demo',
    'jdbc-layered-practice',
    'excel-automation',
    'snack-shop-prototype'
);

DELETE pt FROM post_tags pt
JOIN posts p ON p.id = pt.post_id
WHERE p.slug IN (
    'java-learning-roadmap-review',
    'mysql-index-practice-notes',
    'spring-boot-layering-checklist'
);

DELETE FROM resources
WHERE slug IN (
    'java-interview-notes',
    'spring-boot-annotations-cheatsheet',
    'mysql-index-optimization-notes',
    'wsl2-dev-environment-checklist',
    'tianshi-javaweb-courseware'
)
AND title IN (
    'Java 面试八股文整理.pdf',
    'Spring Boot 常用注解速查表.md',
    'MySQL 索引优化笔记.md',
    'WSL2 开发环境配置清单.md',
    '天狮学院 JavaWeb 课件合集.zip'
);

DELETE FROM projects
WHERE slug IN (
    'finally-system-exam',
    'lab2-javaweb',
    'mybatis-demo',
    'jdbc-layered-practice',
    'excel-automation',
    'snack-shop-prototype'
)
AND title IN (
    '期末系统测试考试平台',
    'JavaWeb 课程实验 Lab2',
    'MyBatis 学习项目',
    'JDBC 分层实践',
    'Excel 自动化练习',
    '零食商城原型'
);

DELETE FROM posts
WHERE slug IN (
    'java-learning-roadmap-review',
    'mysql-index-practice-notes',
    'spring-boot-layering-checklist'
)
AND title IN (
    'Java 学习路线复盘',
    'MySQL 索引实战笔记',
    'Spring Boot 分层设计检查清单'
);

INSERT INTO categories (name, slug, type, sort_order)
VALUES
    ('Java Web', 'java-web', 'project', 1),
    ('Java SE', 'java-se', 'project', 2),
    ('前端', 'frontend', 'project', 3),
    ('Python', 'python', 'project', 4),
    ('全栈项目', 'full-stack', 'project', 5),
    ('学习笔记', 'study-notes', 'post', 1),
    ('工程实践', 'engineering-practice', 'post', 2),
    ('开发工具', 'dev-tools', 'resource', 1)
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO tags (name, slug)
VALUES
    ('Java', 'java'),
    ('Servlet', 'servlet'),
    ('JSP', 'jsp'),
    ('MyBatis', 'mybatis'),
    ('JDBC', 'jdbc'),
    ('MySQL', 'mysql'),
    ('POI', 'poi'),
    ('Spring Boot', 'spring-boot'),
    ('Hutool', 'hutool'),
    ('HTML5', 'html5'),
    ('PixiJS', 'pixijs'),
    ('JavaScript', 'javascript'),
    ('Python', 'python'),
    ('NumPy', 'numpy'),
    ('事务', 'transaction'),
    ('三层架构', 'three-tier-architecture'),
    ('AI 协作', 'ai-collaboration'),
    ('Java 基础', 'java-basics'),
    ('集合', 'collections'),
    ('异常处理', 'exception-handling'),
    ('IO 流', 'io-stream'),
    ('数据库设计', 'database-design'),
    ('视图', 'view'),
    ('索引', 'index'),
    ('SQL 注入', 'sql-injection')
ON DUPLICATE KEY UPDATE name = name;
