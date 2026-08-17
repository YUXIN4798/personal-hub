# Personal Hub Phase 3 流水线状态机（自动巡航用）

> 婉萤的 cron 自动巡航任务每 8 分钟读本文件，按状态机推进，执行一步后更新「当前状态」。
> 终态 = 等铭轩验收。到达终态后：`cron remove` 本巡航 job + `cron wake` 主会话（now）通知铭轩。

## 关键路径与命令

- 项目：`~/codex-workspace/personal-hub`（分支 feature/phase3-admin，所有 cc 任务继续在该分支）
- cc 派活：`~/.local/bin/cx-msg <task_id> "<自包含任务>" <cwd>`（同步阻塞，必须用 nohup 后台 + 日志文件，timeout 已改 3600s）
- cc 任务完成判定（任一即可）：`git log --oneline -3` 出现新提交 / `tail ~/codex-workspace/通信台账.md` 出现对应 task_id 行 / 对应 /tmp/cc-phase3-XXX.log 有回执输出
- 语轩派活：`~/.local/bin/yx-msg <task_id> "<任务>"`（同步，同样 nohup 后台）
- 语轩回执判定：`tail ~/dsh-workspace/通信台账.md` 出现 task_id
- 应用重启：先 `pgrep -f "[p]lexus-classworlds" | xargs -r kill`（⚠ 必须用 [p] 字符类，否则 pgrep -f 会匹配外层 shell 自杀）→ `cd 项目 && nohup mvn spring-boot:run -q > /tmp/personal-hub.log 2>&1 &` → sleep 22 → curl 验证
- 验收标准：`mvn test` 全绿 + curl 实测关键路由
- 测试：`cd ~/codex-workspace/personal-hub && mvn test 2>&1 | grep -E "Tests run:|BUILD" | tail -5`

## 状态机

### 状态 1：等批次 B（Tailwind standalone，phase3_003_tailwind）
- 判定：git log 有新提交含 "tailwind" 字样 或 台账出现 phase3_003 行
- 推进：验收（mvn test 绿 + 重启应用 + curl 首页/404 确认 CSS 200 + grep 模板无 cdn.tailwindcss.com 残留）→ 通过则 commit 若无提交则补提交（cc 若没提交，代为提交，注明）→ 状态 2
- 超时保护：若已等待超 40 分钟无结果，检查 /tmp/cc-phase3-003.log 与 codex 进程（`pgrep -af "[c]odex"` 注意排除路径含 codex-workspace 的误报），cc 死了就重派（续干指令：先 git status/diff 盘点再继续）

### 状态 2：派批次 C（双主题切换，phase3_004_themes）
- 派活内容（自包含）：
  - 颜色系统重构：layout.html/index/about/错误页/admin 模板中散落的硬编码色（#0b0f12、#56e3cd、#e5eeee、#a9bfc0 等）收口为 CSS 变量（app.css 里 :root 定义 --bg/--bg-soft/--text/--text-dim/--accent/--border/--card 等语义变量，模板 class 用 var(--x) 或 Tailwind 任意值引用变量）
  - 亮色主题（唯美柔和风）：[data-theme="light"] 变量组——暖白米色底（如 #faf7f2）、深棕/墨绿文字、莫兰迪粉/杏色强调、柔和阴影、去光斑改柔和光晕；保持布局结构不变
  - 切换按钮：nav 或页脚放主题切换（🌙/☀️），JS 切换 data-theme + localStorage('theme') 记忆 + 首次访问跟随 prefers-color-scheme
  - 动效适配：光斑/ghost-mark 在亮色下弱化（降低透明度/改色）
  - 验证：mvn test 绿 + 重启 + 截图（可用 playwright 无头截两张主题对比）或 curl 检查变量生效
  - commit + cc-send phase3_004_themes result 回执
- 推进后 → 状态 3

### 状态 3：等批次 C 完成
- 判定：同上（git log 含 theme 字样 / 台账 phase3_004）
- 推进：验收（mvn test + 重启 + curl 首页 200 + 检查 main.css 含 [data-theme="light"] 变量 + 若有截图用副脑转述确认两主题视觉差异）→ 通过 → 状态 4

### 状态 4：交语轩审查（phase3_review_001）
- 派活内容：审查 feature/phase3-admin 全部新增（管理后台/12 项修复/tailwind standalone/双主题），按 ~/dsh-workspace/skills/code-review-checklist.md 输出（结论+严重+一般+亮点+总结），重点：admin 鉴权绕过、CSRF、上传（如有）、主题切换的 XSS/转义、Tailwind standalone 构建是否合规
- 推进后 → 状态 5

### 状态 5：等语轩审查结果
- 判定：台账出现 phase3_review_001
- 推进：
  - 无严重问题 → merge feature/phase3-admin 到 develop + push + 状态 6
  - 有严重问题 → 派 cc phase3_005_reviewfix 修复 → 状态 6 之前先验收修复（mvn test + curl）再 merge

### 状态 6：终态（等铭轩验收）
- 动作：更新 docs/ROADMAP.md 完成标记 + 台账/daily note 记录 → cron remove 本 job → cron wake 主会话（now，文本说明「Phase 3 全链路完成，等铭轩验收」）

## 当前状态

状态 1（等批次 B / phase3_003_tailwind，13:17 派出）
