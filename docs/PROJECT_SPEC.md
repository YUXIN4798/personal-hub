# PROJECT_SPEC.md — Personal Hub 产品规格书

> 版本 v1.0 · 2026-08-16 定稿 · 产品负责人：婉萤 · 甲方：铭轩
> 本文档与 AGENTS.md 同为项目纲领；冲突时以铭轩拍板为准。

## 1. 项目概述

**Personal Hub（铭轩的个人数字基地）**——不是简历页，是可扩展的个人数字资产中心。

- 核心价值：作品集展示（求职主战场）+ 个人资源库 + 笔记沉淀 + 联系入口
- v1 目标：MVP 上线（本地 Docker 可跑），内容由铭轩后续填充
- 扩展预留：搜索、在线工具、API、访问统计、Agent 展示、AI 实验室（Spring AI）

## 2. 目标用户

1. 铭轩本人（管理内容）
2. 招聘方/面试官（看作品集 → 判断技术能力）
3. 同学/朋友（拿资源、看笔记）

## 3. v1 功能范围

### 前台页面
| 页面 | 路由 | 内容 |
|---|---|---|
| 首页 | / | Hero 区 + 作品集精选 + 最新笔记 + 联系入口 |
| 关于我 | /about | 自我介绍、技能栈、学习路线、证书（Java 2 级/AI 1 级备考中） |
| 作品集 | /projects | 项目卡片列表（封面/标题/技术栈标签/简介） |
| 项目详情 | /projects/{slug} | 详情 + 技术栈 + 仓库/演示链接 + 亮点 |
| 资源库 | /resources | 分类筛选 + 标签 + 搜索（标题模糊） |
| 资源详情 | /resources/{id} | 描述 + 版本/大小/下载次数 + 下载按钮 |
| 笔记 | /posts | 文章列表 |
| 文章详情 | /posts/{slug} | Markdown 渲染内容 |
| 联系方式 | /contact | 邮箱/微信/GitHub（静态配置） |

### 管理后台（/admin/**，单管理员）
- 登录/登出（Session + BCrypt）
- 项目管理 CRUD（封面图上传、技术栈标签、置顶排序、发布/草稿）
- 资源管理 CRUD（文件上传、分类、标签、可见性、下载计数、SHA-256 校验）
- 文章管理 CRUD（Markdown 编辑）
- 分类管理 CRUD

### 非目标（v1 明确不做）
搜索全文、评论、用户注册、在线工具、API、统计面板、AI 实验室、多管理员、国际化

## 4. 技术架构（定案）

| 项 | 决策 | 理由 |
|---|---|---|
| 后端 | Java 17 + Spring Boot 3 + Maven | 铭轩求职主线；Spring AI 预留 |
| 视图 | Thymeleaf（SSR，无 Node 构建链） | JSP/Servlet 经验无缝衔接；一个 jar 部署 |
| 样式 | Tailwind CSS（standalone CLI 编译静态 CSS） | 现代审美 + 零 Node 运行时 |
| 持久层 | Spring Data JPA | 主流面试点；防注入 |
| 数据库 | MySQL 8（Docker Compose） | 铭轩主学 MySQL；直接练手 |
| 迁移 | Flyway（只增不改） | 表结构版本化 |
| 部署 | Docker Compose（app + mysql 两容器） | 一键起环境；服务器通用 |

**包结构**：`com.tianshi.hub`（config/controller/service/repository/entity/dto/exception/util）

## 5. 数据模型（MySQL 8，snake_case）

```
users           # 管理员
  id BIGINT PK AUTO_INCREMENT · username UNIQUE · password(BCrypt) · real_name
  created_at · updated_at

categories      # 资源分类
  id · name UNIQUE · slug UNIQUE · sort_order INT

tags            # 标签（项目/文章/资源共用）
  id · name UNIQUE · slug UNIQUE

projects        # 作品
  id · title · slug UNIQUE · summary · description TEXT · cover_image
  repo_url · demo_url · tech_stack(逗号分隔) · status ENUM(draft,published)
  featured TINYINT · sort_order · created_at · updated_at

project_tags    # 项目-标签
  project_id · tag_id (复合主键)

resources       # 资源（核心表，字段按扩展性设计）
  id · title · description · category_id FK · file_path(UUID 文件名)
  original_name · file_size BIGINT · version VARCHAR(32)
  download_count BIGINT DEFAULT 0 · visibility ENUM(public,private)
  checksum CHAR(64) SHA-256 · created_at · updated_at

resource_tags   # 资源-标签
  resource_id · tag_id (复合主键)

posts           # 笔记/Blog
  id · title · slug UNIQUE · summary · content MEDIUMTEXT(Markdown)
  status ENUM(draft,published) · published_at · created_at · updated_at

post_tags       # 文章-标签
  post_id · tag_id (复合主键)

site_config     # 站点配置（联系方式、站点名、首页介绍）
  k VARCHAR(64) PK · v TEXT
```

## 6. 非功能需求

- **响应式**：手机/平板/桌面三档可用
- **性能**：页面 < 2s；图片压缩/懒加载；列表分页
- **安全**：BCrypt、参数化查询、上传白名单+UUID 重命名+大小上限、后台鉴权、CSRF、Thymeleaf 默认转义、生产 HTTPS
- **SEO 基础**：语义化标签、meta description、sitemap.xml（后续）
- **可维护**：三层清晰、注释适度、Flyway 迁移、测试覆盖核心

## 7. 里程碑（Phase 0-5）

| Phase | 内容 | 状态 |
|---|---|---|
| 0 规划 | AGENTS.md 宪法 + 本规格书 | ✅ 2026-08-16 |
| 1 骨架 | Docker 环境 + Spring Boot 工程 + V1 迁移 + 首页/关于我 + docker compose 跑通 | ⏳ |
| 2 MVP | 作品集 + 资源库 + 笔记 + 后台 + 上传下载 | ⏳ |
| 3 Review | 语轩按 code-review-checklist 全面审查 → cc 修复 | ⏳ |
| 4 验收 | 婉萤 QA：多浏览器/手机/404/下载/速度/安全头 | ⏳ |
| 5 部署 | 服务器选型 → Docker 部署 → 域名/备案/HTTPS | ⏳ |

## 8. 部署方案（现状：本地优先）

- **v1**：本地 Docker Compose 跑通（app:8080 + mysql:3306）
- **上线**：待定——服务器无、域名无、备案未做。选项：国内轻量云（需 ICP 备案 1-3 周）/ 海外 VPS + Cloudflare。**须铭轩拍板，花钱前必报**
- CI/CD：v1 不做，git push + 服务器 compose up

## 9. 待决事项清单

1. Docker 安装方式（Windows Desktop vs WSL engine）——待铭轩拍板
2. 服务器/域名/备案 —— 待铭轩
3. 作品集项目素材（截图/说明/技术栈）——铭轩提供，模板婉萤出
4. 语轩 AGENTS.md 人设 —— 铭轩撰写
5. 联系方式的公开程度 —— 铭轩定（邮箱可公开、微信酌情）
