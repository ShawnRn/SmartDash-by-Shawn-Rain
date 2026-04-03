---
description: 新 Mac 首次安装 Habe Android 编译依赖
---

适用于全新 macOS 环境，目标是一次性装好 Java 17、Android SDK、adb、sdkmanager 和 shell 环境变量。

### 1. 运行安装脚本
// turbo
```bash
.agents/scripts/bootstrap-mac.sh
```

### 2. 脚本会完成的内容
- 通过 Homebrew 安装 `openjdk@17`
- 安装 `android-commandlinetools`
- 安装 `android-platform-tools`，提供 `adb`
- 创建 `ANDROID_HOME=/Users/shawnrain/android-sdk`
- 安装 `platform-tools`
- 安装 `platforms;android-35`
- 安装 `build-tools;35.0.0`
- 接受 Android SDK licenses
- 写入 `~/.zprofile` 与 `~/.zshrc`

### 3. 安装后验证
```bash
zsh -lic '.agents/scripts/preflight.sh'
zsh -lic './gradlew :app:assembleDebug --console plain'
```

### 4. 预期关键结果
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
- `ANDROID_HOME=/Users/shawnrain/android-sdk`
- `adb version` 正常输出
- `sdkmanager --version` 正常输出
- `assembleDebug` 可以完成
