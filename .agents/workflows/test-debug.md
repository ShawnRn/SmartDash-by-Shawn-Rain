---
description: 执行 Debug 单元测试并保留日志
---

本工作流用于运行 `:app:testDebugUnitTest`，适合提交前或 GitHub 同步前做最小回归。

说明：
- 若当前机器不是主编译机，脚本会先把当前工作区同步到 `shawn-rains-macbook-pro`，再通过 Tailscale SSH 在主编译机执行测试，并把日志回传到当前机器
- **硬约束：不要直接执行 `./gradlew :app:testDebugUnitTest`。AI Agent 必须通过 `.agents/scripts/test-debug.sh` 触发测试。**
- **若当前机器不是主编译机，但输出里没有 `REMOTE_BUILD_HOST=...`，应立即停止并排查，而不是继续在本机消耗 CPU。**
- **若当前机器不是主编译机，但输出里没有 `REMOTE_SYNC_HOST=...`，也不能假定 Pro 上代码已经是最新的。**

### 1. 运行测试脚本
// turbo
```bash
.agents/scripts/test-debug.sh
```

### 2. 结果判断
- 若输出 `TEST_NOTE=No local unit tests were present (NO-SOURCE)`，说明当前仓库暂无本地单测。
- 若输出 `TEST_LOG=...`，可回看完整 Gradle 日志。
- 若输出 `REMOTE_BUILD_HOST=...`，说明本次测试实际运行在主编译机上。
- 若输出 `REMOTE_SYNC_HOST=...`，说明本次测试前已先把当前工作区同步到主编译机。
