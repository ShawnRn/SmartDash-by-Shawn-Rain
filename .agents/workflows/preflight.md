---
description: 检查 Java / Android SDK / adb / gh / git 环境是否就绪
---

在构建、安装或同步 GitHub 前，先跑一次环境检查。

如果是新 Mac、首次拉起仓库，先执行：

```bash
.agents/scripts/bootstrap-mac.sh
```

### 1. 运行检查
// turbo
```bash
.agents/scripts/preflight.sh
```

### 2. 关注项
- `JAVA_HOME`
- `ANDROID_HOME`
- `ANDROID_SDK_ROOT`
- `ADB_VERSION`
- `SDKMANAGER_VERSION`
- `RELEASE_SIGNING`
- `SIGNING_FILES_IN_REPO`
- `GH_AUTH_STATUS`
- 当前分支和远端仓库地址
