-- V16: 插入真实笔记正文种子数据

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'JDBC 六步骤：连接数据库的正确姿势',
       'jdbc-six-steps',
       '从 JDBC 的六个固定步骤切入，说明驱动加载、连接获取、执行 SQL 与资源释放的基础流程。',
       '# JDBC 六步骤：连接数据库的正确姿势\n\n> 来源：Java 应用开发课程实验一（2025-11-24，B206）· 整理：婉萤\n\n## JDBC 是什么\n\nJDBC（Java Database Connectivity）是 Java 连接数据库的标准 API。无论用 MyBatis 还是 Spring Data JPA，底层都是它。理解 JDBC 的六个步骤，是理解一切 ORM 框架的起点。\n\n## 六个步骤\n\n1. **加载驱动** —— `Class.forName("com.mysql.jdbc.Driver")`，注册驱动类\n2. **建立连接** —— `DriverManager.getConnection(url, username, password)`\n3. **获取执行器** —— `conn.prepareStatement(sql)`，预编译 SQL 语句\n4. **执行 SQL** —— `executeUpdate()`（增删改）或 `executeQuery()`（查询）\n5. **处理结果集** —— 遍历 `ResultSet` 取出每一行数据\n6. **释放资源** —— 按顺序关闭 ResultSet → Statement → Connection\n\n## 实战练习\n\n实验基于联系人表完成八个功能，覆盖全部四个操作类型：\n\n```sql\ncreate table person(\n  id int primary key,\n  name varchar(15) not null,\n  sex varchar(5),\n  mobile varchar(11) not null unique,\n  email varchar(30) unique,\n  city varchar(20)\n);\n```\n\n- **增**：根据用户输入向联系人表插入一条数据（使用自增主键）\n- **查**：按 id 查询单个联系人；查询并打印所有联系人\n- **改**：先显示原有信息，再按用户输入的新信息修改各字段\n- **删**：按 id 删除联系人，若不存在则输出「查无此人」\n- **模糊查询**：按姓名、手机号模糊匹配（`LIKE %keyword%`）\n\n## 关键心得\n\n- `PreparedStatement` 用 `?` 占位符代替字符串拼接，既防 SQL 注入又自动处理类型\n- 释放资源必须放在 `finally` 中，顺序不能颠倒（先开的后关）\n- 模糊查询注意：`LIKE ''%'' + keyword + ''%''` 的拼接方式，以及通配符的转义\n\n## 配套项目\n\n`TestJDBC`：JDBC 六步骤练习工程，含 `TestInsert` / `TestUpdate` 两个示例类。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'jdbc-six-steps');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'JDBCUtils 工具类：把重复代码收进工具箱',
       'jdbc-utils-dao',
       '记录把 JDBC 连接、配置读取和资源关闭抽成工具类，并配合 DAO 分层减少重复代码。',
       '# JDBCUtils 工具类：把重复代码收进工具箱\n\n> 来源：Java 应用开发课程实验二（2025-12-01）· 整理：婉萤\n\n## 为什么需要工具类\n\n写第一个 JDBC 程序时会发现：每次操作都要重复「加载驱动 → 建连接 → 关资源」，一个功能里真正有区别的只有 SQL 那一行。**重复三次就该抽离**——JDBCUtils 就是那把剪刀。\n\n## 设计要点\n\n**配置外置**：数据库地址、账号密码不写死在代码里，放进 `db.properties`，改环境不用改代码：\n\n```properties\nmydriver=com.mysql.jdbc.Driver\nurl=jdbc:mysql://localhost:3306/dbname\nusername=root\npassword=***\n```\n\n**两个静态方法搞定一切**：\n\n```java\npublic class JDBCUtils {\n    // 读取 properties 配置，加载驱动，返回连接\n    public static Connection getConnection() { ... }\n\n    // 按 结果集→执行器→连接 顺序关闭，每个都判空、各自 try-catch\n    public static void close(ResultSet rs, PreparedStatement pstm, Connection conn) { ... }\n}\n```\n\n## 工程化的细节\n\n- **异常处理**：底层异常统一包装成 `RuntimeException("获取链接失败", e)`，保留原始异常链（cause），排查时能看到根因\n- **关闭资源**：三个 `close` 分开 try-catch——一个资源关闭失败不能影响另外两个的释放\n- **类路径加载**：`JDBCUtils.class.getResourceAsStream("/com/ts/cmx/conf/db.properties")`，按包结构组织配置文件\n\n## 下一步\n\n工具类只是第一层。再往上走就是 **DAO 分层**：视图层（Scanner 菜单）→ 业务层 → DAO 层，每层只干自己的事。部门管理系统三部曲完整走了这条路。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'engineering-practice' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'jdbc-utils-dao');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT '部门管理系统三部曲：一个业务的三次技术演进',
       'dept-system-evolution',
       '用同一个部门职员管理业务复盘集合、POI Excel、JDBC 数据库三版实现的演进过程。',
       '# 部门管理系统三部曲：一个业务的三次技术演进\n\n> 来源：Java 语言程序设计实训 第二次 / 第三次（2025-06-30 / 07-03）· 整理：婉萤\n\n同一个「部门职员管理系统」，我用三种技术实现了三遍。回头看，这三次迭代恰好画出了一条清晰的成长路线：\n\n## 第一版：纯集合（内存态）\n\n用 `List<Dept>` 存数据，Scanner 菜单驱动。注册、查询、删除功能都有，但**程序一关数据就没了**。\n\n这一版练的是基础：泛型 `List<Emp>`、实体类设计、视图层交互。\n\n## 第二版：POI Excel 持久化\n\n引入 Apache POI，数据存进 Excel 文件：\n\n- 注册职员 → 写入 `.xls` 文件（HSSFWorkbook / HSSFRow / HSSFCell）\n- 查询职员 → 读取 Excel 逐行解析\n- 附带练了**遍历集合时修改的正确姿势**：用迭代器的 `remove()`，而不是在 for-each 里直接删——后者会抛 `ConcurrentModificationException`\n\n## 第三版：JDBC + MySQL 持久化\n\nExcel 终究不是数据库。第三版把存储层换成 MySQL：\n\n- 引入 `mysql-connector`，注册/查询职员走真实 SQL\n- 核心概念：**缓存数据过期**。内存 List 是缓存，数据库是事实源——数据写库后要同步刷新缓存，否则查到的是旧数据\n- 这也是「缓存与数据库一致性」问题的第一次亲身体验：先更新数据库、再失效缓存，而不是反过来\n\n## 收获\n\n三版对比让我真正理解了「分层架构」为什么存在：**存储方式从 List → Excel → MySQL 换了个遍，但业务逻辑（注册、查询、删除职员）几乎没变**。数据访问层隔离得越干净，换存储的成本就越低。这也是后来学 MyBatis、Spring Data JPA 时一点就通的原因。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'engineering-practice' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'dept-system-evolution');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'PreparedStatement：防 SQL 注入的正确姿势',
       'prepared-statement',
       '从拼接 SQL 的风险讲起，说明 PreparedStatement 参数绑定如何阻断 SQL 注入并提升可维护性。',
       '# PreparedStatement：防 SQL 注入的正确姿势\n\n> 来源：Java 应用开发 实验三（大二上学期）· 整理：婉萤\n\n## 为什么不能拼 SQL\n\n初学 JDBC 最常见的写法是把参数直接拼进 SQL 字符串：\n\n```java\nString sql = "SELECT * FROM student WHERE name = ''" + name + "''";\n```\n\n看起来没问题，但如果 `name` 是 `'' OR ''1''=''1`，这条 SQL 就变成了：\n\n```sql\nSELECT * FROM student WHERE name = '''' OR ''1''=''1''\n```\n\n条件恒真，整张表被拖出来——这就是 SQL 注入。**任何来自用户输入的内容，都不能直接拼进 SQL。**\n\n## 正确做法：PreparedStatement\n\n```java\nString sql = "SELECT * FROM student WHERE name = ?";\nPreparedStatement pstm = conn.prepareStatement(sql);\npstm.setString(1, name);   // 参数绑定，类型安全\nResultSet rs = pstm.executeQuery();\n```\n\n`?` 占位符 + `setXxx()` 参数绑定，数据库把参数当**数据**而不是**代码**处理，注入路径从根上被切断。\n\n## 实验内容\n\n基于 student 表完成增删改查全流程，全部走参数化 SQL：\n\n- 插入学生记录（setInt / setString 按类型绑定）\n- 按 id / 姓名查询\n- 修改学生信息（先查后改，回显原值）\n- 删除记录（判断影响行数）\n\n## 额外收益\n\n- **预编译**：同一条 SQL 结构复用执行计划，批量操作更快\n- **类型安全**：`setInt(1, id)` 在编译期就保证类型匹配\n- **可读性**：SQL 与数据分离，代码不再是一锅粥\n\n这个习惯我一直带到了后来所有项目里——项目里的 SQL 全部参数绑定，零拼接。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'prepared-statement');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'MyBatis 入门：从 JDBC 到 ORM',
       'mybatis-basics',
       '对比原生 JDBC 的样板代码、硬编码 SQL 与手写映射，梳理 MyBatis 的核心价值。',
       '# MyBatis 入门：从 JDBC 到 ORM\n\n> 来源：数据库访问框架技术课程 + 大二下学期笔记 · 整理：婉萤\n\n## 先记住 JDBC 的痛\n\n写过原生的 JDBC 再回头看，它有三个硬伤：\n\n1. 连接创建/关闭的样板代码占了一半篇幅\n2. SQL 硬编码在 Java 字符串里，改一条语句要重新编译\n3. 结果集到对象的映射全靠手写 `rs.getString("name")`，字段一多就是灾难\n\nMyBatis 就是来治这三处的。\n\n## 三件套结构\n\n**mybatis-config.xml** —— 核心配置：数据源、事务、Mapper 注册：\n\n```xml\n<environments default="mysql_conn">\n  <environment id="mysql_conn">\n    <transactionManager type="JDBC"/>\n    <dataSource type="POOLED">\n      <property name="driver" value="com.mysql.jdbc.Driver"/>\n      <property name="url" value="jdbc:mysql://localhost:3306/db?serverTimezone=Asia/Shanghai"/>\n    </dataSource>\n  </environment>\n</environments>\n```\n\n**Mapper XML** —— SQL 的家，与 DAO 接口一一对应：\n\n```xml\n<mapper namespace="com.ts.dao.PersonDao">\n  <select id="findByName" resultType="com.ts.entity.Person">\n    SELECT * FROM person WHERE name LIKE CONCAT(''%'', #{name}, ''%'')\n  </select>\n</mapper>\n```\n\n**DAO 接口** —— 只声明方法，实现由框架动态代理生成。\n\n## 学习路径上的关键节点\n\n- **参数传递**：单参数、多参数（@Param）、Map、对象\n- **动态 SQL**：`<if>` / `<where>` / `<foreach>`——按条件拼查询不写一堆 if 拼字符串\n- **关联映射**：一对多（Emp-Classes）、多对一、一对一（Idcard-People），自定义 ResultMap\n- **日志**：log4j 配置 `log4j.logger.com.ts.dao=TRACE`，SQL 执行一目了然\n\n## 踩坑记录\n\n- `mybatis-config.xml` 的 URL 里 `&` 必须写成 `&amp;`，否则解析报错\n- Mapper 的 namespace 必须与 DAO 接口全限定名一致，resource 路径用 `/` 不用 `.`\n\n配套练习：`MybatisDemo`（10+ 组映射、动态 SQL、多表关联）、`Snack_TestProject`（多表 + 中间表关联）。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'mybatis-basics');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'MySQL DQL 查询实战',
       'mysql-dql',
       '整理 SELECT、WHERE、聚合分组、连接查询与子查询等 DQL 基础能力，配合典型 SQL 示例复盘。',
       '# MySQL DQL 查询实战\n\n> 来源：数据库技术与应用 第二次实验 + day13 综合练习 · 整理：婉萤\n\n## 单表基础\n\n- `SELECT` 列筛选 + `AS` 别名\n- `WHERE` 条件过滤：比较、`BETWEEN`、`IN`、`LIKE` 模糊匹配\n- 常见函数：`IFNULL`、`CONCAT`、日期函数、`ROUND`\n\n## 聚合与分组\n\n```sql\nSELECT deptno, COUNT(*) AS cnt, AVG(sal) AS avg_sal\nFROM emp\nGROUP BY deptno\nHAVING AVG(sal) > 2000;\n```\n\n要点：`WHERE` 过滤**行**（分组前），`HAVING` 过滤**组**（分组后），两者不能混用。\n\n## 多表查询\n\n**内连接**（只留匹配行）：\n\n```sql\nSELECT e.ename, d.dname\nFROM emp e\nJOIN dept d ON e.deptno = d.deptno;\n```\n\n**外连接**（保留不匹配的一侧）：`LEFT JOIN` 查「没有雇员的部门」这类题必须用它——`WHERE e.empno IS NULL` 是关键手法。\n\n## 子查询\n\n- 单行子查询：`WHERE sal = (SELECT MAX(sal) FROM emp)`\n- 多行子查询：`IN`、`ALL`、`ANY`\n- 相关子查询：外层每行触发一次内层——「找出工资高于本部门平均工资的员工」\n\n## 经典题目（dept/emp 数据集）\n\n1. 查出薪金等于或高于部门 30 中任意一名员工薪金的员工\n2. 各部门人数、平均工资、平均服务年限\n3. 各工作岗位的最低工资及对应雇员姓名\n4. 列转行：把每个部门的人名横排成一行\n\n这些题基本覆盖了面试 SQL 的常考题型。写 SQL 的顺序：**先定结果列 → 再想数据来源（可能要多表/子查询）→ 最后套分组与过滤**。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'mysql-dql');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT '数据库应用系统设计：部门职员管理系统',
       'mysql-db-design',
       '围绕 DEPT、EMP、SALGRADE 三表模型，记录从建表到复杂查询的数据库应用系统设计过程。',
       '# 数据库应用系统设计：部门职员管理系统\n\n> 来源：数据库技术与应用考核项目（期末）· 整理：婉萤\n\n一个完整的数据库小项目：从建表到复杂查询，全程手写 SQL。\n\n## 三表设计\n\n经典的 `DEPT / EMP / SALGRADE` 模型：\n\n```sql\nCREATE TABLE dept (\n  deptno INT PRIMARY KEY, dname VARCHAR(20), loc VARCHAR(20)\n);\nCREATE TABLE emp (\n  empno INT PRIMARY KEY, ename VARCHAR(20), job VARCHAR(20),\n  mgr INT, hiredate DATE, sal DECIMAL(7,2), comm DECIMAL(7,2),\n  deptno INT, FOREIGN KEY (deptno) REFERENCES dept(deptno)\n);\nCREATE TABLE salgrade (grade INT, losal INT, hisal INT);\n```\n\n设计要点：主外键、字段类型与约束（NOT NULL / UNIQUE）、decimal 用于金额。\n\n## DML 实战\n\n- 批量插入多条记录\n- `DELETE ... WHERE ename LIKE ''S%''`\n- `UPDATE emp SET sal = sal * 1.1 WHERE ...`（全员提薪 10%）\n\n## DQL 重头戏\n\n- 列转行领导查询（`GROUP_CONCAT` 把同组姓名拼成一行）\n- 外连接查各部门人数（含 0 人的部门）\n- 部门最高工资排名子查询\n- 子查询求平均工资\n- 经典辨析：`WHERE` 与 `HAVING` 的区别（分组前过滤行 vs 分组后过滤组）\n\n## 项目小结\n\n这个项目把「数据库设计 → 建表 → 数据操作 → 复杂查询」完整走了一遍。最大的心得：**SQL 要先用嘴说清楚逻辑，再动手写**——查询需求翻译成「从哪张表、按什么条件、留下哪些列」，写出来基本不会错。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'engineering-practice' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'mysql-db-design');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'MySQL 进阶：视图、索引与事务',
       'mysql-view-index-tx',
       '梳理视图封装查询、索引优化访问路径与事务保证一致性的 MySQL 进阶知识点。',
       '# MySQL 进阶：视图、索引与事务\n\n> 来源：数据库技术与应用 第三次实验 · 整理：婉萤\n\n## 视图：包装好的查询\n\n视图是存储在数据库里的命名查询，本质是一层「虚拟表」：\n\n```sql\nCREATE VIEW v_emp_dept AS\nSELECT e.ename, d.dname FROM emp e JOIN dept d ON e.deptno = d.deptno;\n```\n\n- **作用**：简化复杂查询、隔离敏感列（只暴露需要的字段）、逻辑层不变（底层表结构改了，改视图即可）\n- **注意**：视图不存数据，查询视图 = 实时执行底层 SQL\n\n## 索引：查询加速器\n\n在 `ename` 上建索引前后，`WHERE ename = ''...''` 的执行计划从 `type=ALL`（全表扫描）变成 `type=ref`（索引查找）：\n\n```sql\nALTER TABLE emp ADD INDEX idx_ename (ename);\nEXPLAIN SELECT * FROM emp WHERE ename = ''SCOTT'';\n```\n\n- 索引适合**高频查询、低修改**的列\n- 不是越多越好：每次写入都要维护索引\n- 复合索引注意最左前缀原则\n\n## 事务：要么全做，要么不做\n\n转账场景：A 扣钱、B 加钱，两步必须同生共死。\n\n```sql\nSTART TRANSACTION;\nUPDATE account SET balance = balance - 100 WHERE id = 1;\nUPDATE account SET balance = balance + 100 WHERE id = 2;\nCOMMIT;  -- 或 ROLLBACK\n```\n\nACID 四特性里，实务中最常打交道的是 A（原子性）和 I（隔离性）。\n\n## 存储引擎\n\n- **InnoDB**：支持事务、行级锁、外键——现代默认选择\n- **MyISAM**：不支持事务，表级锁，读多写少的旧场景\n\n配套实践：JDBC 里用 `conn.setAutoCommit(false)` + `commit()`/`rollback()` 手动控制事务——银行账户系统的转账原子性就是这么做的。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'mysql-view-index-tx');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'Java 集合框架：一张图理清体系',
       'java-collections',
       '从 Collection 与 Map 两条主线梳理 List、Set、Queue、HashMap 等常用集合的定位。',
       '# Java 集合框架：一张图理清体系\n\n> 来源：集合框架课程笔记 · 整理：婉萤\n\n## 总览\n\n```\nCollection\n├── List   —— 有序、可重复：ArrayList / LinkedList\n├── Set    —— 无序、不可重复：HashSet / TreeSet\n└── Queue  —— 队列：LinkedList / PriorityQueue\nMap         —— 键值对：HashMap / TreeMap / Hashtable\n```\n\n## 三大接口怎么选\n\n| 接口 | 特点 | 典型场景 |\n|---|---|---|\n| List | 按索引存取，元素可重复 | 保存一组有序数据、按位置访问 |\n| Set | 自动去重 | 标签集合、去重统计 |\n| Map | 键值映射，键唯一 | 缓存、计数（key → 次数） |\n\n## ArrayList vs LinkedList\n\n- **ArrayList**：底层数组，**查快增删慢**（中间插入要整体搬移）\n- **LinkedList**：底层双向链表，**增删快查慢**（按下标访问要遍历）\n- 默认用 ArrayList，只有在频繁头尾增删时才考虑 LinkedList\n\n## 三种遍历方式\n\n```java\n// 1. 传统 for（List 专用，靠下标）\nfor (int i = 0; i < list.size(); i++) { ... }\n\n// 2. 增强 for（最常用，遍历中不能修改集合）\nfor (String s : list) { ... }\n\n// 3. 迭代器（唯一能在遍历中安全删除的方式）\nIterator<String> it = list.iterator();\nwhile (it.hasNext()) {\n    String s = it.next();\n    if (shouldRemove(s)) it.remove();  // 而不是 list.remove(s)\n}\n```\n\n## 高频踩坑\n\n- **遍历中删除**：增强 for 里直接 `list.remove()` 会抛 `ConcurrentModificationException`，必须用迭代器（部门管理系统实训里专门练过）\n- **HashMap 遍历**：`entrySet()` 比 `keySet()` 再 get 高效得多\n- **自动装箱**：`List<Integer>` 的 `remove(1)` 是按**下标**删，`remove(Integer.valueOf(1))` 才是按**值**删\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'java-collections');

INSERT INTO posts (title, slug, summary, content, status, category_id, published_at)
SELECT 'Java 异常与 IO 流：两个面试高频主题',
       'java-exception-io',
       '把 Throwable 异常体系和 IO 流分类放在一起复盘，聚焦 Java 基础中的高频面试主题。',
       '# Java 异常与 IO 流：两个面试高频主题\n\n> 来源：异常专题 + IO 流课程笔记 · 整理：婉萤\n\n## 异常：先看懂 Throwable 家谱\n\n```\nThrowable\n├── Error        —— 系统级错误，程序无法处理（OutOfMemoryError 等）\n└── Exception\n    ├── RuntimeException   —— 运行时异常：空指针、下标越界、类型转换……\n    └── 受检异常            —— 编译期强制处理：IOException、SQLException……\n```\n\n处理三件套：\n\n- **try-catch-finally**：finally 里放资源释放，return 也会执行\n- **throws**：自己不想处理就声明抛给上层\n- **throw**：主动抛异常，配合自定义异常表达业务错误（如「余额不足」）\n\n要点：异常链——包装时保留原始异常 `throw new RuntimeException("xxx", e)`，日志里才有根因。\n\n## IO 流：按「方向 × 单位」分类\n\n- 按方向：输入流（读）/ 输出流（写）\n- 按单位：字节流（InputStream/OutputStream，万物皆字节）/ 字符流（Reader/Writer，处理文本）\n\n典型组合：`FileInputStream` + `FileOutputStream` 做文件复制，`BufferedReader` 按行读文本。\n\n## 实验里的实战\n\n- 26 个大写字母写入 `a.txt`，读出后转小写\n- `c.txt` 内容去重 + 自然排序，写入 `d.txt`（集合 + IO 联动）\n- 对象序列化：`ObjectOutputStream` 把对象整个写入文件\n\n## 一句话总结\n\n异常管理的是「**出错了怎么办**」，IO 管理的是「**数据从哪来、到哪去**」。两者共同点是：**资源用完必须关**——异常处理里忘了关流，就是内存泄漏的开始。\n',
       'published',
       c.id,
       '2026-08-18 09:00:00'
FROM categories c
WHERE c.slug = 'study-notes' AND c.type = 'post'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE slug = 'java-exception-io');

INSERT IGNORE INTO post_tags (post_id, tag_id)
SELECT p.id, t.id
FROM posts p
JOIN (
    SELECT 'jdbc-six-steps' AS post_slug, 'JDBC' AS tag_name
    UNION ALL SELECT 'jdbc-six-steps', 'MySQL'
    UNION ALL SELECT 'jdbc-utils-dao', 'JDBC'
    UNION ALL SELECT 'jdbc-utils-dao', '三层架构'
    UNION ALL SELECT 'dept-system-evolution', 'JDBC'
    UNION ALL SELECT 'dept-system-evolution', 'POI'
    UNION ALL SELECT 'dept-system-evolution', '三层架构'
    UNION ALL SELECT 'prepared-statement', 'JDBC'
    UNION ALL SELECT 'prepared-statement', 'SQL 注入'
    UNION ALL SELECT 'mybatis-basics', 'MyBatis'
    UNION ALL SELECT 'mybatis-basics', 'MySQL'
    UNION ALL SELECT 'mysql-dql', 'MySQL'
    UNION ALL SELECT 'mysql-db-design', 'MySQL'
    UNION ALL SELECT 'mysql-db-design', '数据库设计'
    UNION ALL SELECT 'mysql-view-index-tx', 'MySQL'
    UNION ALL SELECT 'mysql-view-index-tx', '视图'
    UNION ALL SELECT 'mysql-view-index-tx', '索引'
    UNION ALL SELECT 'mysql-view-index-tx', '事务'
    UNION ALL SELECT 'java-collections', 'Java 基础'
    UNION ALL SELECT 'java-collections', '集合'
    UNION ALL SELECT 'java-exception-io', 'Java 基础'
    UNION ALL SELECT 'java-exception-io', '异常处理'
    UNION ALL SELECT 'java-exception-io', 'IO 流'
) tag_links ON tag_links.post_slug = p.slug
JOIN tags t ON t.name = tag_links.tag_name;
