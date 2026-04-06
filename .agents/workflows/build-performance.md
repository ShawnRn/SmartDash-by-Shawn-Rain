# Build Performance Guide

## 典型编译耗时

| 场景 | 命令 | 热启动耗时 |
|------|------|-----------|
| 仅编译 Kotlin | `:app:compileDebugKotlin` | ~3-5s |
| Debug APK | `:app:assembleDebug` | ~8-15s |
| devRelease APK | `.agents/scripts/install-dev-release.sh` | ~15-30s |
| fastDevRelease APK | `.agents/scripts/install-fast-dev-release.sh` | ~10-20s |
| Release APK | `.agents/scripts/build-release.sh` | ~1-2min |

## 关键优化配置

```properties
# gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.workers.max=4
kotlin.incremental=true
kotlin.incremental.useClasspathSnapshot=true
kotlin.incremental.usePreciseJavaTracking=true
kotlin.daemon.jvmargs=-Xmx2048m
android.enableR8.fullMode=true
android.enableR8.desugaring=true
```

## AI Agent 编译决策

| 用户语义 | 执行命令 |
|---------|---------|
| "装到手机上" | `.agents/scripts/install-dev-release.sh` |
| "出个包" | `.agents/scripts/build-release.sh` |
| "编译验证" | `./gradlew :app:compileDebugKotlin --console plain` |
| "改 UI 看效果" | `./gradlew :app:assembleDebug && adb install -r ...` |
| "先编一个 devRelease 包，别刷太多输出" | `cd ".../habe_android" && export JAVA_HOME=... && set -o pipefail && .agents/scripts/build-dev-release.sh 2>&1 | tail -5` |

## 环境变量

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/Users/shawnrain/android-sdk
```

> **重要：** JAVA_HOME 必须指向 JDK 17，否则 JdkImageTransform 会失败。

## 低干扰构建模式

```bash
cd "/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/habe_android" \
  && export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  && set -o pipefail \
  && .agents/scripts/build-dev-release.sh 2>&1 | tail -5
```

- 适用于 Codex / 桌面代理 / 远程终端等容易被大量日志拖慢的环境
- `tail -5` 仅显示尾部摘要，减少终端绘制开销
- `set -o pipefail` 防止构建失败被 `tail` 掩盖

## 性能排查

```bash
# 检查 Daemon 状态
./gradlew --status

# 查看详细耗时
./gradlew :app:assembleDebug --profile
open build/reports/profile/profile-*.html

# 强制清理 configuration cache
rm -rf .gradle/configuration-cache/
```
