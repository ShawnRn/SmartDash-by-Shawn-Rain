---
description: 构建并安装最新 devRelease APK 到 adb 设备
---

本工作流用于本地真机高频联调，默认先构建 `devRelease`，再安装到连接设备。

说明：
- `devRelease` 保持 release 签名，适合高频覆盖安装
- 若用户已经明确说“装到手机上”或“adb 安装”，就直接执行本工作流，不再二次确认
- 若系统弹出安装确认页，需在手机侧完成确认后 `adb install` 才会结束
- 若当前机器不是主编译机，脚本会自动通过 Tailscale SSH 到 `shawn-rains-macbook-pro` 编译，并把 APK 回传到当前机器后再执行本地安装

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
- 若经过远端编译，输出里还会带上 `REMOTE_BUILD_HOST=...`
- 安装后建议立刻拉起 `com.shawnrain.habe/.MainActivity` 并检查一次 `adb logcat -b crash -d`，确认没有启动期崩溃
