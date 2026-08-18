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
mvn spring-boot:run
```

## 修改管理员密码

管理员账号密码通过环境变量 `ADMIN_PASSWORD` 轮换，不把明文写入迁移文件或代码。

1. 在部署环境或本地 `.env` 中设置新的 `ADMIN_PASSWORD`。
2. 重启应用。
3. 应用启动时会读取 `ADMIN_PASSWORD`：如果它与当前管理员 BCrypt hash 不匹配，会自动更新 `users` 表中 `admin` 账号的密码 hash；如果未设置或已匹配，则跳过。

注意：不要把 `.env` 或明文密码提交到 Git。

## 数据说明

- `site_config` 表保留给 E3 首页动态化使用，用于站点名、联系方式、首页介绍等可配置内容。
- `projects.content` 是早期 Markdown 正文字段遗留列，应用代码不再使用；Flyway 只增不改，数据库列暂时保留。
