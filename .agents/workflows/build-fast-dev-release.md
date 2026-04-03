---
description: 使用 release 签名快速构建 fastDevRelease APK，默认跳过 lintVital
---

本工作流用于本地个人真机联调。它继续使用 `devRelease` 的 release 签名输出，但默认跳过 `lintVital` 相关任务，以缩短高频迭代等待时间。

说明：
- 适合你自己本地反复改 UI / 交互 / 协议联调
- 不适合作为最终交付包或正式归档包
- 若命中 `project_dex_archive/devRelease` 重复类问题，脚本会自动清理对应 dex 中间产物并重试一次

### 1. 运行构建脚本
// turbo
```bash
.agents/scripts/build-fast-dev-release.sh
```

### 2. 验证输出
- 确认终端中出现 `DEV_RELEASE_APK_PATH=...`
- 确认终端中出现 `APK_SHA256=...`
- 确认终端中出现 `BUILD_LOG=...`

### 3. 默认产物
- 主 APK: `app/build/outputs/apk/devRelease/app-devRelease.apk`
- 归档副本: `.agents/artifacts/habe-fast-dev-release-<timestamp>.apk`
- 构建日志: `.agents/logs/build-fast-dev-release-<timestamp>.log`
