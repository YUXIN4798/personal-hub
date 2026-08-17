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
