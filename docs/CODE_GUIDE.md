# Personal Hub 代码导读

> 写给铭轩的参观地图。按这份导读逛一遍，你就能看懂这个项目八成。
> 技术栈：Java 17 · Spring Boot 3 · Thymeleaf · Tailwind CSS v4 · JPA/Hibernate · MySQL 8 · Flyway · Docker

## 一、这项目是干嘛的

你的个人网站：作品集（项目）+ 资源库 + 笔记 + 关于我 + 后台管理 + 站内搜索。
前台给访客看，后台（/admin）给你自己管内容。

## 二、项目结构（重点看 src/main/java）

```
personal-hub/
├── src/main/java/com/tianshi/hub/
│   ├── HubApplication.java          # 启动类
│   ├── config/                      # 安全配置、Web 配置、上传配置
│   ├── controller/                  # 控制器：接 URL，调 service，返回视图名
│   │   ├── HomeController.java      # 首页
│   │   ├── ProjectController.java   # 项目列表/详情
│   │   ├── PostController.java      # 笔记
│   │   ├── ResourceController.java  # 资源
│   │   ├── SearchController.java    # 站内搜索（新）
│   │   └── admin/                   # 后台管理控制器
│   ├── service/                     # 业务逻辑层（接口 + Impl 实现）
│   ├── repository/                  # 数据访问层（JPA 接口，几乎不用写 SQL）
│   ├── entity/                      # 实体类 = 表结构映射（Project/Post/Resource/Category/Tag...）
│   └── dto/                         # 数据传输对象（表单/接口专用）
├── src/main/resources/
│   ├── application.yml              # 配置（端口、数据库、上传大小）
│   ├── db/migration/                # Flyway 数据库迁移（V1__xxx.sql ... 版本化建表/种子数据）
│   ├── templates/                   # Thymeleaf 页面模板
│   │   ├── fragments/layout.html    # 公共布局（导航/页脚/搜索覆盖层）
│   │   ├── fragments/theme-init.html# 防闪屏主题初始化脚本
│   │   ├── index.html / about.html / search.html ...
│   │   └── admin/                   # 后台页面
│   └── static/                      # 静态资源
│       ├── css/app.css              # ★ 手写样式源文件（主题变量 + 自定义类）
│       ├── css/main.css             # ★ Tailwind 编译产物（不要手改！）
│       └── js/                      # theme.js（主题切换）/ search.js（搜索交互）
├── scripts/build-css.sh             # 编译 app.css → main.css
├── tools/tailwindcss-linux-x64      # Tailwind standalone 编译器
└── docker-compose.yml               # MySQL 容器
```

## 三、一个请求的旅程（以搜索为例）

1. 你在页面搜「Java」→ 浏览器发 `GET /search?q=Java`
2. `SearchController.search()` 接住 → 参数校验（trim、空则重定向首页、超 50 字截断）
3. 调 `SearchService.search(q)` → 内部调三个 repository 的 `search(q)` 方法
4. Repository 是 JPA 接口，方法上 `@Query` 是 JPQL（对象查询语言，防注入）
5. 查出来的 `List<Project>` 等塞进 `Model`
6. 返回视图名 `"search"` → Thymeleaf 渲染 `templates/search.html` → HTML 回浏览器

**规律**：Controller 永远不碰数据库，Service 写业务，Repository 只管查，Entity 对应表。这是标准三层，跟你学校教的一致，只是包名更规范。

## 四、页面模板怎么看

- `layout.html` 是个**布局片段**，用 `th:fragment="layout(pageTitle, content, pageDescription)"` 定义。所有页面都用它当骨架：
  ```html
  th:replace="~{fragments/layout :: layout('搜索', ~{::content}, '站内搜索：项目、笔记、资源。')}"
  ```
  三个参数：页面标题、内容片段、SEO 描述。**改布局（导航/页脚/搜索入口）只改 layout.html 一处。**
- `th:each` 循环渲染列表，`th:if` 条件渲染，`th:href="@{/projects/{slug}(slug=${p.slug})}"` 生成链接。
- 静态资源用 `th:href="@{/css/main.css}"` 这种 Thymeleaf 写法（会自动处理上下文路径）。

## 五、样式怎么改（重要！）

- **改 `app.css`**（源文件），里面：
  - `:root { --ph-bg: ...; --ph-accent: ... }` 是暗色主题变量，`[data-theme="light"]` 是亮色覆盖。改颜色只动变量。
  - 下半部分是自定义类（.nav-link、.motion-card、.search-overlay...）
- 改完运行 `./scripts/build-css.sh` → 生成 `main.css`（编译产物，提交要一起提交，但**永远别手改 main.css**，下次编译会覆盖）。
- 页面里那些 `text-4xl`、`space-y-10` 是 Tailwind 工具类，写在 HTML 的 class 里，编译器扫描模板自动生成。

## 六、数据库

- 表结构全部由 `db/migration/` 下的 Flyway 迁移文件管理（V1、V2...按版本执行，只增不改）。
- 要加字段：新建 `V(N+1)__描述.sql`，写 `ALTER TABLE` / `INSERT` 种子数据，重启自动执行。
- 开发库在本地 Docker MySQL（容器名 `personal-hub-mysql`），测试用 H2 内存库。

## 七、怎么跑起来

```bash
cd ~/codex-workspace/personal-hub
docker ps | grep mysql                # 确认数据库容器在跑
set -a && source .env && set +a       # 读密码环境变量
mvn spring-boot:run                   # 开发模式（或 java -jar target/*.jar）
# 浏览器打开 http://localhost:8080
```

跑测试：`mvn test`（目前 142 个，全绿）。

## 八、建议的阅读顺序

1. `HubApplication.java` → 感受一下 Spring Boot 入口有多薄
2. `HomeController.java` → 看 Controller 怎么把数据给页面
3. `SearchController.java` + `SearchServiceImpl.java` + `ProjectRepository.java` → 三层连起来看
4. `templates/fragments/layout.html` → 理解布局复用
5. `templates/search.html` + `static/js/search.js` + `app.css` 搜索段 → 前端三板斧
6. 随便点开一个 `entity/` 类 + 对应的 migration SQL → 实体与表的对应关系

有任何一行看不懂，随时截给我，我给你逐行讲 😊
