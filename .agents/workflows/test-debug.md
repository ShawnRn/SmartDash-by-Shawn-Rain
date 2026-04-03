---
description: 执行 Debug 单元测试并保留日志
---

本工作流用于运行 `:app:testDebugUnitTest`，适合提交前或 GitHub 同步前做最小回归。

### 1. 运行测试脚本
// turbo
```bash
.agents/scripts/test-debug.sh
```

### 2. 结果判断
- 若输出 `TEST_NOTE=No local unit tests were present (NO-SOURCE)`，说明当前仓库暂无本地单测。
- 若输出 `TEST_LOG=...`，可回看完整 Gradle 日志。
