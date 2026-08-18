-- V15: 插入真实作品集种子数据

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '期末综合考试系统',
       'finally-system-test-exam',
       '课程期末综合项目：Servlet+JSP+JSTL+MyBatis 全栈管理系统。双角色登录（管理员/普通用户）、图形验证码、三层 Filter 拦截，管理员/分类/商品/供应商/用户五实体完整 CRUD，约 20 个 JSP 页面，支持 POI 导出 Excel。',
       '课程期末综合项目：Servlet+JSP+JSTL+MyBatis 全栈管理系统。双角色登录（管理员/普通用户）、图形验证码、三层 Filter 拦截，管理员/分类/商品/供应商/用户五实体完整 CRUD，约 20 个 JSP 页面，支持 POI 导出 Excel。',
       'Java / Servlet / JSP / JSTL / MyBatis / MySQL / POI / Hutool',
       'published', 1, 1,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'java-web' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'finally-system-test-exam');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT 'Web 用户管理系统',
       'lab2-user-management',
       'Servlet 核心技术全覆盖实验项目：注册登录、用户 CRUD、个人资料修改、验证码、Cookie/Session 演示、Filter 链与 Listener 演示、Excel 导出。18 个 JSP 页面，覆盖 Web 基础能力面。',
       'Servlet 核心技术全覆盖实验项目：注册登录、用户 CRUD、个人资料修改、验证码、Cookie/Session 演示、Filter 链与 Listener 演示、Excel 导出。18 个 JSP 页面，覆盖 Web 基础能力面。',
       'Java / Servlet / JSP / MyBatis / MySQL / POI / Lombok',
       'published', 0, 2,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'java-web' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'lab2-user-management');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '银行管理系统（JavaWeb 版）',
       'bank-system-web',
       'JavaWeb 大作业：JSP+Servlet+MyBatis+MySQL。Session 登录态、Hutool 图形验证码、过滤器角色鉴权、Excel 导出，前端采用 Glassmorphism 毛玻璃 UI，含安全实践与扩展论述。',
       'JavaWeb 大作业：JSP+Servlet+MyBatis+MySQL。Session 登录态、Hutool 图形验证码、过滤器角色鉴权、Excel 导出，前端采用 Glassmorphism 毛玻璃 UI，含安全实践与扩展论述。',
       'Java / JSP / Servlet / MyBatis / MySQL / Hutool',
       'published', 0, 3,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'java-web' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'bank-system-web');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '图书借阅管理系统',
       'book-borrow-system',
       '纯 JDBC 控制台项目，不依赖任何框架：四表设计（图书/类型/用户/借阅记录）、连表查询与分页、完整借书还书业务、登录功能。DAO/Service 接口实现分离，PreparedStatement 防注入。',
       '纯 JDBC 控制台项目，不依赖任何框架：四表设计（图书/类型/用户/借阅记录）、连表查询与分页、完整借书还书业务、登录功能。DAO/Service 接口实现分离，PreparedStatement 防注入。',
       'Java / JDBC / MySQL / 三层架构',
       'published', 1, 4,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'java-se' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'book-borrow-system');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '银行账户系统（事务版）',
       'bank-account-tx',
       'JDBC 事务专项：JDBCUtils 用 ThreadLocal 保证同一线程同一连接，手动控制事务实现转账原子性。同一主题三版迭代（基础版→工具类版→事务版），体现对 JDBC 的渐进理解。',
       'JDBC 事务专项：JDBCUtils 用 ThreadLocal 保证同一线程同一连接，手动控制事务实现转账原子性。同一主题三版迭代（基础版→工具类版→事务版），体现对 JDBC 的渐进理解。',
       'Java / JDBC / 事务 / MySQL',
       'published', 0, 5,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'java-se' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'bank-account-tx');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '在线考试系统',
       'exam-system',
       '纯前端在线考试应用（无框架）：试卷管理、JSON 试卷导入、倒计时答题、单选多选填空自动判分、简答手动批阅、错题本、历史记录、成绩趋势（Chart.js）、暗色/亮色主题、Canvas 粒子背景。',
       '纯前端在线考试应用（无框架）：试卷管理、JSON 试卷导入、倒计时答题、单选多选填空自动判分、简答手动批阅、错题本、历史记录、成绩趋势（Chart.js）、暗色/亮色主题、Canvas 粒子背景。',
       'HTML / CSS / JavaScript / Chart.js',
       'published', 1, 6,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'frontend' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'exam-system');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '飞机大战',
       'airplane-game',
       'HTML5 游戏课程项目集：从环境搭建到帧频动画、碰撞检测、纹理播放器，分 10 章递进，最终成品「飞机大战」。另有植物大战僵尸、赛车碰撞等趣味小游戏作业。',
       'HTML5 游戏课程项目集：从环境搭建到帧频动画、碰撞检测、纹理播放器，分 10 章递进，最终成品「飞机大战」。另有植物大战僵尸、赛车碰撞等趣味小游戏作业。',
       'HTML5 / PixiJS / Canvas',
       'published', 0, 7,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'frontend' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'airplane-game');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT '学生成绩数据分析系统',
       'student-score-analysis',
       'Python 团队项目（组长）：CSV 成绩读取校验、NumPy 统计（均值/极值/标准差）、排名与分数段、jieba 评语词频 + 词云、Matplotlib 三图输出。含 5 个实战排坑记录（中文乱码、词云字体、编码等）。',
       'Python 团队项目（组长）：CSV 成绩读取校验、NumPy 统计（均值/极值/标准差）、排名与分数段、jieba 评语词频 + 词云、Matplotlib 三图输出。含 5 个实战排坑记录（中文乱码、词云字体、编码等）。',
       'Python / NumPy / Matplotlib / jieba / wordcloud',
       'published', 0, 8,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'python' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'student-score-analysis');

INSERT INTO projects (
    title, slug, summary, description, tech_stack, status, featured, sort_order,
    category_id, published_at
)
SELECT 'Personal Hub',
       'personal-hub',
       '本站：个人数字基地。Java 17 + Spring Boot 3 + Thymeleaf + Tailwind + JPA + MySQL 8 + Flyway + Docker Compose。前后台齐全（作品集/资源/笔记/文件管理）、双主题、Markdown 渲染，三 AI 协作工程实践（婉萤统筹/cc 编码/语轩审计）。',
       '本站：个人数字基地。Java 17 + Spring Boot 3 + Thymeleaf + Tailwind + JPA + MySQL 8 + Flyway + Docker Compose。前后台齐全（作品集/资源/笔记/文件管理）、双主题、Markdown 渲染，三 AI 协作工程实践（婉萤统筹/cc 编码/语轩审计）。',
       'Java 17 / Spring Boot 3 / Thymeleaf / Tailwind CSS / JPA / MySQL / Flyway / Docker',
       'published', 1, 9,
       c.id, '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'full-stack' AND c.type = 'project'
  AND NOT EXISTS (SELECT 1 FROM projects WHERE slug = 'personal-hub');

INSERT IGNORE INTO project_tags (project_id, tag_id)
SELECT p.id, t.id
FROM projects p
JOIN (
    SELECT 'finally-system-test-exam' AS project_slug, 'Servlet' AS tag_name
    UNION ALL SELECT 'finally-system-test-exam', 'JSP'
    UNION ALL SELECT 'finally-system-test-exam', 'MyBatis'
    UNION ALL SELECT 'finally-system-test-exam', 'MySQL'
    UNION ALL SELECT 'finally-system-test-exam', 'POI'
    UNION ALL SELECT 'finally-system-test-exam', 'Hutool'
    UNION ALL SELECT 'lab2-user-management', 'Servlet'
    UNION ALL SELECT 'lab2-user-management', 'JSP'
    UNION ALL SELECT 'lab2-user-management', 'MyBatis'
    UNION ALL SELECT 'lab2-user-management', 'MySQL'
    UNION ALL SELECT 'lab2-user-management', 'POI'
    UNION ALL SELECT 'bank-system-web', 'Servlet'
    UNION ALL SELECT 'bank-system-web', 'JSP'
    UNION ALL SELECT 'bank-system-web', 'MyBatis'
    UNION ALL SELECT 'bank-system-web', 'MySQL'
    UNION ALL SELECT 'bank-system-web', 'Hutool'
    UNION ALL SELECT 'book-borrow-system', 'JDBC'
    UNION ALL SELECT 'book-borrow-system', 'MySQL'
    UNION ALL SELECT 'book-borrow-system', '三层架构'
    UNION ALL SELECT 'bank-account-tx', 'JDBC'
    UNION ALL SELECT 'bank-account-tx', '事务'
    UNION ALL SELECT 'bank-account-tx', '三层架构'
    UNION ALL SELECT 'exam-system', 'JavaScript'
    UNION ALL SELECT 'exam-system', 'HTML5'
    UNION ALL SELECT 'airplane-game', 'HTML5'
    UNION ALL SELECT 'airplane-game', 'JavaScript'
    UNION ALL SELECT 'student-score-analysis', 'Python'
    UNION ALL SELECT 'student-score-analysis', 'NumPy'
    UNION ALL SELECT 'personal-hub', 'Spring Boot'
    UNION ALL SELECT 'personal-hub', 'MyBatis'
    UNION ALL SELECT 'personal-hub', 'MySQL'
    UNION ALL SELECT 'personal-hub', 'AI 协作'
) tag_links ON tag_links.project_slug = p.slug
JOIN tags t ON t.name = tag_links.tag_name;
