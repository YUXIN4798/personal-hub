# 审查问题清单（语轩 Phase 2 审查 · 15 项一般问题）

> 来源：2026-08-16 语轩审查报告（phase2_review_001）。2026-08-17 铭轩提供完整清单并授权修复，双主题切换同步推进。
> 状态标记：✅ 已修 | 🚧 进行中 | 📋 待修 | ⏸ 暂缓（理由）

## 一、性能与前端动效

1. 📋 **光斑 rAF 永续循环**（layout.html:209-216）：renderGlow 无条件每帧续订 rAF，光标静止/不可见时仍每帧写 left/top 触发布局。修：目标接近 epsilon 停帧、空闲超时暂停、改 transform: translate3d。
2. ⏸ **IntersectionObserver 不 disconnect**（layout.html:186-194）：MPA 页面卸载即回收可接受；转 SPA 时再清理。低优先级。

## 二、数据与并发

3. 📋 **download_count 并发丢更新**（ResourceServiceImpl.java:84）：实体内存 ++ 读改写。修：@Modifying UPDATE ... SET download_count = download_count + 1。
4. ⏸ **file_path 可空 + DB 绝对路径风险**（V3 未加 NOT NULL；normalize 不阻 DB 内绝对路径）：当前无上传入口、file_path 仅 DB 可控，风险低。后续上传功能必须按 SPEC：UUID 文件名 + 上传根目录白名单 + V4 加 NOT NULL。
5. 📋 **资源列表显示「分类 #id」**（resources/list.html:33）：显示 ID 而非分类名；null 时显示「分类 #null」。修：映射分类名。

## 三、前端页面

6. 📋 **projects/list.html 缺空态提示**：与 resources 列表不对称。补空态。
7. 📋 **分页无次排序键**：单字段排序同值行分页不稳定；sort_order/visibility 无索引（数据量小暂可，索引随 V4 加）。
8. 📋 **死代码**：ResourceRepository.findByCategoryId、ProjectRepository.findByStatusOrderBySortOrderAsc 未使用。删。
9. 📋 **400 页面割裂**：/resources?category=abc 走 Spring 默认 400（Whitelabel）。修：error/400.html 或 handler。
10. 📋 **错误页风格不一致**：error/404.html、error/500.html 用 CDN Tailwind + slate/emerald，与主站青绿设计语言不一致、无 layout 导航。重设计。
11. 📋 **Tailwind CDN → standalone**（layout.html:8）：与 PROJECT_SPEC「standalone CLI 编译静态 CSS」定案不符，生产不宜。单独任务。
12. 📋 **硬编码 href**（index.html）：href="/projects"、"/resources" 非 th:href，context-path 部署会断；Notes 卡片 href="#" 死链。修。

## 四、工程与测试

13. 📋 **测试覆盖为零**：分页 clamp、visibility 过滤、prepareDownload 零单元测试；mvn test 依赖本地 MySQL。补 Service 单测。
14. 📋 **重复代码**：分页 clamp 两份（ProjectServiceImpl:24-26 / ResourceServiceImpl:38-40）抽公共方法；pom lombok 声明未使用（删或用）。
15. ⏸ **下载不区分 type**：link 型资源也渲染下载按钮；detail 页对占位 file_path 无感知（种子阶段可接受）。顺手修：link 型不显示下载按钮。

## 排期

- **批次 A（polish，cc）**：1、3、5、6、7、8、9、10、12、13、14、15 —— 小修+补测
- **批次 B（tailwind standalone，cc）**：11 —— 独立工程任务
- **批次 C（双主题，cc）**：颜色变量重构 + 亮色唯美柔和主题 + data-theme 切换 + localStorage（铭轩 2026-08-17 授权同步推进）
- 2、4 暂缓（转 SPA/上传功能时一并处理）
