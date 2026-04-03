---
description: 构建并安装最新 devRelease APK 到 adb 设备
---

本工作流用于本地真机高频联调，默认先构建 `devRelease`，再安装到连接设备。

说明：
- `devRelease` 保持 release 签名，适合高频覆盖安装
- 若系统弹出安装确认页，需在手机侧完成确认后 `adb install` 才会结束

### 1. 可选：指定设备
// turbo
```bash
export ADB_TARGET=<device-serial>
```

### 2. 构建并安装
// turbo
```bash
.agents/scripts/install-dev-release.sh
```

### 3. 预期结果
- `adb install -r` 成功
- 输出 `INSTALLED_APK=...`
