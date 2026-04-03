# 新 Mac 装机技能

目标：在全新 macOS 环境中，为本项目装好编译依赖并验证构建链路。

## 推荐顺序
1. 运行 `.agents/scripts/bootstrap-mac.sh`
2. 运行 `.agents/scripts/preflight.sh`
3. 运行 `./gradlew :app:assembleDebug --no-daemon --console plain`

## 关键基线
- Java: `openjdk@17`
- Android SDK Root: `/Users/shawnrain/android-sdk`
- 必装 SDK 包：
  - `platform-tools`
  - `platforms;android-35`
  - `build-tools;35.0.0`

## 关键环境变量
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
- `ANDROID_HOME=/Users/shawnrain/android-sdk`
- `ANDROID_SDK_ROOT=/Users/shawnrain/android-sdk`

## 安装完成判断
- `java -version` 输出 17
- `adb version` 可用
- `sdkmanager --version` 可用
- `app/build/outputs/apk/debug/app-debug.apk` 能生成
