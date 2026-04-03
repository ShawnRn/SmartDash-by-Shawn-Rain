---
description: 构建并安装最新 Debug APK 到 adb 设备
---

本工作流用于本地真机联调，默认先构建再安装。

### 1. 可选：指定设备
// turbo
```bash
export ADB_TARGET=<device-serial>
```

### 2. 构建并安装
// turbo
```bash
.agents/scripts/install-debug.sh
```

### 3. 预期结果
- `adb install -r` 成功
- 输出 `INSTALLED_APK=...`
