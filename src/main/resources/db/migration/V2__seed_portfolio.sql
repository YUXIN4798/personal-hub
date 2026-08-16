ALTER TABLE projects
    ADD COLUMN description VARCHAR(2000) AFTER summary,
    ADD COLUMN tech_stack VARCHAR(500) AFTER description,
    ADD COLUMN featured TINYINT(1) NOT NULL DEFAULT 0 AFTER status,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER featured;

INSERT INTO categories (name, slug, type, sort_order)
VALUES ('作品集', 'portfolio', 'project', 1);

INSERT INTO tags (name, slug)
VALUES
    ('Java', 'java'),
    ('Servlet', 'servlet'),
    ('MyBatis', 'mybatis'),
    ('MySQL', 'mysql'),
    ('Maven', 'maven'),
    ('JavaWeb', 'javaweb'),
    ('JUnit', 'junit'),
    ('JDBC', 'jdbc'),
    ('Apache POI', 'apache-poi'),
    ('JSP', 'jsp');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '期末系统测试考试平台',
       'finally-system-exam',
       '基于 Maven、MyBatis 与 Servlet 三层架构的后台管理系统。',
       '这是一个面向系统测试课程期末项目的后台管理平台，围绕商品、分类、供应商和用户等核心业务建立清晰的三层结构。项目包含 Excel 数据导出、登录认证与权限过滤，重点实践了传统 Java Web 应用中的模块拆分和请求控制。',
       'Java, Servlet, MyBatis, MySQL, Maven',
       'published', 1, 1,
       c.id, CURRENT_TIMESTAMP
FROM categories c
WHERE c.slug = 'portfolio' AND c.type = 'project';

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT 'JavaWeb 课程实验 Lab2',
       'lab2-javaweb',
       '覆盖 Servlet、Filter、Listener、Session 与 Cookie 的 Java Web 综合实验。',
       'Lab2 将 Java Web 基础组件串联成一个完整的用户管理流程，包含验证码登录注册、用户增删改查和 Excel 导出。通过这个实验系统地理解请求生命周期、会话状态管理以及过滤器在登录权限控制中的作用。',
       'Java, JavaWeb, Servlet, MySQL',
       'published', 1, 2,
       c.id, CURRENT_TIMESTAMP
FROM categories c
WHERE c.slug = 'portfolio' AND c.type = 'project';

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT 'MyBatis 学习项目',
       'mybatis-demo',
       '围绕多表映射、动态 SQL 和 DAO 设计的 MyBatis 练习项目。',
       '项目使用 11 张表构建实体与 DAO 映射练习，覆盖多对一、一对多和动态 SQL 等常见关系查询场景。配套 30 多个测试用例，逐步验证 CRUD、关联查询和边界条件，帮助巩固持久层的设计思路。',
       'Java, MyBatis, MySQL, JUnit',
       'published', 0, 3,
       c.id, CURRENT_TIMESTAMP
FROM categories c
WHERE c.slug = 'portfolio' AND c.type = 'project';

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT 'JDBC 分层实践',
       'jdbc-layered-practice',
       '从 JDBC 工具类出发实现 DAO、Service 分层的后端基础实践。',
       '项目没有依赖 ORM，而是手写 JDBC 工具类并划分 DAO 与 Service 层，分别实现图书管理系统和银行账户系统。实践重点包括连接与资源释放、事务边界、参数绑定，以及业务代码与数据库访问代码的职责分离。',
       'Java, JDBC, MySQL',
       'published', 0, 4,
       c.id, CURRENT_TIMESTAMP
FROM categories c
WHERE c.slug = 'portfolio' AND c.type = 'project';

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT 'Excel 自动化练习',
       'excel-automation',
       '使用 Apache POI 完成 Excel 读写，并结合 Java 基础算法进行数据处理。',
       '项目围绕 Apache POI 的工作簿、工作表、单元格和样式 API 展开，完成常见 Excel 文件的读取、写入和格式处理。过程中结合 Java 基础算法练习数据整理和转换，为后续报表导出类功能积累可复用经验。',
       'Java, Apache POI',
       'published', 0, 5,
       c.id, CURRENT_TIMESTAMP
FROM categories c
WHERE c.slug = 'portfolio' AND c.type = 'project';

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '零食商城原型',
       'snack-shop-prototype',
       '围绕商品管理和购物流程搭建的 Java Web 原型。',
       '这是一个以零食商品为主题的 Web 原型，覆盖商品展示、商品管理和基础购物流程。项目以 JSP 页面和 MySQL 数据持久化为主，重点练习从页面交互到后端数据处理的完整链路。',
       'Java, JSP, MySQL',
       'published', 0, 6,
       c.id, CURRENT_TIMESTAMP
FROM categories c
WHERE c.slug = 'portfolio' AND c.type = 'project';

INSERT INTO project_tags (project_id, tag_id)
SELECT p.id, t.id
FROM projects p
JOIN tags t ON
    (p.slug = 'finally-system-exam' AND t.slug IN ('java', 'servlet', 'mybatis', 'mysql', 'maven'))
    OR (p.slug = 'lab2-javaweb' AND t.slug IN ('java', 'javaweb', 'servlet', 'mysql'))
    OR (p.slug = 'mybatis-demo' AND t.slug IN ('java', 'mybatis', 'mysql', 'junit'))
    OR (p.slug = 'jdbc-layered-practice' AND t.slug IN ('java', 'jdbc', 'mysql'))
    OR (p.slug = 'excel-automation' AND t.slug IN ('java', 'apache-poi'))
    OR (p.slug = 'snack-shop-prototype' AND t.slug IN ('java', 'jsp', 'mysql'));
