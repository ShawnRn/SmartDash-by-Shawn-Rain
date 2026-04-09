---
description: 使用 release 签名快速构建 devRelease APK，适合真机高频联调
---

本工作流用于生成 SmartDash Android 的 `devRelease` APK。它沿用 release 签名，可直接覆盖用户手机上的现有安装，但比正式 `release` 构建更适合日常快速迭代。

说明：
- 首次构建、Gradle daemon 尚未热身、或刚修改过 `build.gradle.kts` / `gradle.properties` 时，速度仍可能接近正式包
- 同一轮开发里的后续热构建会明显更快，因为会复用 daemon、build cache 与 configuration cache
- 若遇到 `project_dex_archive/devRelease` 重复类报错，可清理该 variant 的 dex 中间产物后重试
- 在桌面代理 / AI Agent 场景下，推荐用“尾部输出”方式减少终端刷屏和界面卡顿
- 若当前机器不是主编译机，脚本会自动通过 Tailscale SSH 路由到 `shawn-rains-macbook-pro` 构建，并回传 APK 与日志

### 1. 运行构建脚本
推荐命令（低干扰、保留失败状态）：
```bash
cd "/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/habe_android" \
  && export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  && set -o pipefail \
  && .agents/scripts/build-dev-release.sh 2>&1 | tail -5
```

仓库内直接运行：
```bash
.agents/scripts/build-dev-release.sh
```

说明：
- `JAVA_HOME` 必须显式指向 JDK 17
- `tail -5` 只展示构建尾部摘要，减少 UI 渲染压力
- `set -o pipefail` 确保构建失败时 shell 仍返回失败

### 2. 验证输出
- 确认终端中出现 `DEV_RELEASE_APK_PATH=...`
- 确认终端中出现 `APK_SHA256=...`
- 确认终端中出现 `BUILD_LOG=...`
- 非主编译机场景下，确认终端中出现 `REMOTE_BUILD_HOST=...`

### 3. 默认产物
- 主 APK: `app/build/outputs/apk/devRelease/app-devRelease.apk`
- 归档副本: `.agents/artifacts/habe-dev-release-<timestamp>.apk`
- 构建日志: `.agents/logs/build-dev-release-<timestamp>.log`
