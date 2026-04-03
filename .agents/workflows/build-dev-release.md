---
description: 使用 release 签名快速构建 devRelease APK，适合真机高频联调
---

本工作流用于生成 Habe Android 的 `devRelease` APK。它沿用 release 签名，可直接覆盖用户手机上的现有安装，但比正式 `release` 构建更适合日常快速迭代。

说明：
- 首次构建、Gradle daemon 尚未热身、或刚修改过 `build.gradle.kts` / `gradle.properties` 时，速度仍可能接近正式包
- 同一轮开发里的后续热构建会明显更快，因为会复用 daemon、build cache 与 configuration cache
- 若遇到 `project_dex_archive/devRelease` 重复类报错，可清理该 variant 的 dex 中间产物后重试

### 1. 运行构建脚本
// turbo
```bash
.agents/scripts/build-dev-release.sh
```

### 2. 验证输出
- 确认终端中出现 `DEV_RELEASE_APK_PATH=...`
- 确认终端中出现 `APK_SHA256=...`
- 确认终端中出现 `BUILD_LOG=...`

### 3. 默认产物
- 主 APK: `app/build/outputs/apk/devRelease/app-devRelease.apk`
- 归档副本: `.agents/artifacts/habe-dev-release-<timestamp>.apk`
- 构建日志: `.agents/logs/build-dev-release-<timestamp>.log`
