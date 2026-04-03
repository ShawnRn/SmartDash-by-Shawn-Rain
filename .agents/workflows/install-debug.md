---
description: 构建并安装最新 Debug APK 到 adb 设备
---

本工作流用于本地真机联调，默认先构建再安装。

> [!IMPORTANT]
> 若当前手机上已安装 release 签名版本，请不要使用本工作流覆盖安装。
> 真机高频联调请优先改用 `.agents/scripts/install-dev-release.sh`。

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
