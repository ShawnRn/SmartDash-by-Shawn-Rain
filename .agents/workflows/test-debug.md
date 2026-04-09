---
description: 执行 Debug 单元测试并保留日志
---

本工作流用于运行 `:app:testDebugUnitTest`，适合提交前或 GitHub 同步前做最小回归。

说明：
- 若当前机器不是主编译机，脚本会自动通过 Tailscale SSH 路由到 `shawn-rains-macbook-pro` 执行测试，并把日志回传到当前机器

### 1. 运行测试脚本
// turbo
```bash
.agents/scripts/test-debug.sh
```

### 2. 结果判断
- 若输出 `TEST_NOTE=No local unit tests were present (NO-SOURCE)`，说明当前仓库暂无本地单测。
- 若输出 `TEST_LOG=...`，可回看完整 Gradle 日志。
- 若输出 `REMOTE_BUILD_HOST=...`，说明本次测试实际运行在主编译机上。
