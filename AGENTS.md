# AGENTS.md — Personal Hub 项目宪法

> 本项目由三个 AI + 一位人类协作。本文件是四人共同协议的最高准则。
> 冲突裁决顺序：本文件 > 技术负责人（婉萤）当轮指令 > 各 agent 自身规范。

## 1. 项目定位

**Personal Hub（铭轩的个人数字基地）**：个人作品集展示 + 资源库 + 笔记/Blog + 联系方式。
可扩展（管理后台、搜索、在线工具、API、统计、Agent 展示、AI 实验室）。
第一版只做：首页、作品集、资源库、文章、后台、响应式、Docker 部署。

## 2. 角色与职责（不可越界）

| 角色 | 主体 | 职责 |
|---|---|---|
| 产品负责人 / 技术负责人 / QA | 婉萤 (OpenClaw) | 需求、规划、任务拆分、协调、验收、部署决策 |
| 主程 | cc (Codex) | **唯一写代码者**：实现、自测、修复 |
| 架构 / 审计 | 语轩 (dsh) | 只读审查：架构、安全、性能、规范，不直接改代码 |
| 甲方 | 铭轩 | 提需求、拍板、验收体验 |

- 所有任务由**婉萤唯一路由**：铭轩只对婉萤；cc 与语轩之间不直接通信。
- 语轩发现问题 → 报婉萤 → 婉萤转派 cc 修复。禁止语轩直接指挥 cc。

## 3. 通信（信封协议 v1.1）

- 婉萤→cc：`cx-msg`（codex exec + JSON 信封 + mila-token）
- 婉萤→语轩：`yx-msg`（dsh headless + JSON 信封 + mila-token）
- cc→婉萤：`cc-send`；语轩→婉萤：`yx-send`
- Token 存 `~/.openclaw/secrets.json` 的 `comms` 段。校验 auth 失败 = 拒收敏感操作 + 报 error 信封。
- 各 agent 任务台账：`~/codex-workspace/通信台账.md` 与 `~/dsh-workspace/通信台账.md`，任务前后读写。

## 4. Git 工作流（三 AI 共同工作区）

- 仓库：`~/codex-workspace/personal-hub`（remote：GitHub 私有 YUXIN4798/personal-hub，SSH）
- 分支：`main`（生产，只有婉萤合并）、`develop`（集成）、`feature/*`、`fix/*`
- 流程：婉萤开 Issue → cc 建 `feature/xxx` → 实现+自测 → 通知婉萤 → 语轩 Review（只读）→ cc 按意见修复 → 婉萤验收 → 婉萤 merge 到 `develop`
- **禁止**：直接 push main；在 main/develop 上直接开发；多人同时改同一文件；`git push -f`
- 提交信息：`type(scope): 描述`（feat/fix/docs/style/refactor/test/chore）

## 5. 技术栈（定案，变更须铭轩同意）

Java 17 · Spring Boot 3 · Maven · Thymeleaf · Tailwind CSS · Spring Data JPA · MySQL 8 · Flyway · Docker Compose
前端第一版为服务端渲染（无 Node 构建链）。

## 6. 编码与测试

- cc 编码强制遵循 `~/.codex/skills/spring-boot-standards/SKILL.md`
- 语轩审查强制遵循 `~/dsh-workspace/skills/code-review-checklist.md`
- 合并前必须 `mvn test` 全绿；提交不留 TODO 垃圾

## 7. 安全红线（任何人不得违反）

- 密钥/密码**永不进 git**（统一存 `~/.openclaw/secrets.json`，配置用环境变量/SecretRef 注入）
- 密码必须 BCrypt；SQL 全部参数绑定；上传文件校验白名单+UUID 重命名
- 后台路由必须鉴权；生产环境 HTTPS；对外发布前婉萤做安全过一遍

## 8. 数据库迁移

- Flyway 迁移文件只增不改；表结构变更 = 新迁移文件
- 表命名 snake_case；审计字段 created_at/updated_at 必备

## 9. 必须征求铭轩同意的清单

技术栈/依赖变更 · 服务器购买与部署上线 · 域名/备案 · 对外公开发布 · 任何花钱操作 · 收集用户数据功能 · 删除历史数据
