# Personal Hub

Spring Boot 3 + Thymeleaf + MySQL 的个人数字基地项目。

## 启动步骤

改动 Tailwind class 或自定义 CSS 后，先重新编译静态样式：

```bash
scripts/build-css.sh
```

1. 启动 MySQL（Docker）

```bash
sg docker -c 'docker compose up -d'
```

2. 启动应用

```bash
ADMIN_PASSWORD=你的管理员密码 mvn spring-boot:run
```

## 修改管理员密码

管理员账号密码必须通过环境变量 `ADMIN_PASSWORD` 管理，不把明文写入迁移文件或代码。

1. 在部署环境或本地 `.env` 中设置新的 `ADMIN_PASSWORD`。
2. 重启应用。
3. 应用启动时会读取 `ADMIN_PASSWORD`：如果未设置会 fail-fast 阻止启动；如果它与当前管理员 BCrypt hash 不匹配，会自动更新 `users` 表中 `admin` 账号的密码 hash；如果已匹配，则跳过。

历史迁移 V4-V7 曾写入过开发期管理员 BCrypt hash；V13 已将这些历史凭据覆盖为无人知晓的随机死口令 hash。fresh 部署或升级后仍必须设置 `ADMIN_PASSWORD`，由启动轮换逻辑写入真实管理员密码。

注意：不要把 `.env` 或明文密码提交到 Git。

## 数据说明

- `site_config` 表保留给 E3 首页动态化使用，用于站点名、联系方式、首页介绍等可配置内容。
- `projects.content` 是早期 Markdown 正文字段遗留列，应用代码不再使用；Flyway 只增不改，数据库列暂时保留。
