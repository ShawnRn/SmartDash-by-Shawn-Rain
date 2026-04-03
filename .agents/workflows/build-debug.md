---
description: 标准化 Debug APK 构建、校验与归档
---

本工作流用于生成 Habe Android 的最新 Debug APK，并输出构建日志、APK 路径与 SHA-256。

### 1. 运行构建脚本
// turbo
```bash
.agents/scripts/build-debug.sh
```

### 2. 验证输出
- 确认终端中出现 `APK_PATH=...`
- 确认终端中出现 `APK_SHA256=...`
- 确认终端中出现 `BUILD_LOG=...`

### 3. 默认产物
- 主 APK: `app/build/outputs/apk/debug/app-debug.apk`
- 归档副本: `.agents/artifacts/habe-debug-<timestamp>.apk`
- 构建日志: `.agents/logs/build-debug-<timestamp>.log`
