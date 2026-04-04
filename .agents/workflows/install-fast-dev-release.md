---
description: 构建并安装最新 fastDevRelease APK 到 adb 设备
---

本工作流用于本地个人真机高频联调，默认先构建 `fastDevRelease`，再安装到连接设备。

说明：
- 若用户已经明确说“装到手机上”或“adb 安装”，就直接执行本工作流，不再二次确认

### 1. 可选：指定设备
// turbo
```bash
export ADB_TARGET=<device-serial>
```

### 2. 构建并安装
// turbo
```bash
.agents/scripts/install-fast-dev-release.sh
```

### 3. 预期结果
- `adb install -r` 成功
- 输出 `INSTALLED_APK=...`
