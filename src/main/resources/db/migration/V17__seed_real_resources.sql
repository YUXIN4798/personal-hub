-- V17: 插入真实 link 型资源种子数据

INSERT INTO resources (
    title, slug, summary, description, url, type, file_size, version, download_count,
    visibility, category_id
)
SELECT 'GitHub 主页',
       'github-home',
       '项目代码仓库与学习足迹',
       '项目代码仓库与学习足迹',
       'https://github.com/YUXIN4798',
       'link',
       0,
       'v1.0',
       0,
       'public',
       c.id
FROM categories c
WHERE c.slug = 'dev-tools' AND c.type = 'resource'
  AND NOT EXISTS (SELECT 1 FROM resources WHERE slug = 'github-home');

INSERT INTO resources (
    title, slug, summary, description, url, type, file_size, version, download_count,
    visibility, category_id
)
SELECT 'Oracle JDK 下载',
       'oracle-jdk',
       'Java 开发环境',
       'Java 开发环境',
       'https://www.oracle.com/java/technologies/downloads/',
       'link',
       0,
       'v1.0',
       0,
       'public',
       c.id
FROM categories c
WHERE c.slug = 'dev-tools' AND c.type = 'resource'
  AND NOT EXISTS (SELECT 1 FROM resources WHERE slug = 'oracle-jdk');

INSERT INTO resources (
    title, slug, summary, description, url, type, file_size, version, download_count,
    visibility, category_id
)
SELECT 'IntelliJ IDEA 下载',
       'intellij-idea',
       '主力开发 IDE',
       '主力开发 IDE',
       'https://www.jetbrains.com/idea/download/',
       'link',
       0,
       'v1.0',
       0,
       'public',
       c.id
FROM categories c
WHERE c.slug = 'dev-tools' AND c.type = 'resource'
  AND NOT EXISTS (SELECT 1 FROM resources WHERE slug = 'intellij-idea');

INSERT INTO resources (
    title, slug, summary, description, url, type, file_size, version, download_count,
    visibility, category_id
)
SELECT 'MySQL Community 下载',
       'mysql-community',
       '数据库环境',
       '数据库环境',
       'https://dev.mysql.com/downloads/',
       'link',
       0,
       'v1.0',
       0,
       'public',
       c.id
FROM categories c
WHERE c.slug = 'dev-tools' AND c.type = 'resource'
  AND NOT EXISTS (SELECT 1 FROM resources WHERE slug = 'mysql-community');
