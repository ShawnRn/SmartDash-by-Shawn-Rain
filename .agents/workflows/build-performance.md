# Build Performance Guide

## 典型编译耗时

| 场景 | 命令 | 热启动耗时 |
|------|------|-----------|
| 仅编译 Kotlin | `:app:compileDebugKotlin` | ~3-5s |
| Debug APK | `:app:assembleDebug` | ~8-15s |
| devRelease APK | `.agents/scripts/install-dev-release.sh` | ~15-30s |
| fastDevRelease APK | `.agents/scripts/install-fast-dev-release.sh` | ~10-20s |
| Release APK | `.agents/scripts/build-release.sh` | ~1-2min |

## 主编译机路由

- SmartDash 的 `Gradle compile / assemble / test` 默认统一路由到主编译机 `shawn-rains-macbook-pro`（`100.103.86.124`）
- 主编译机常驻性能监控地址：`http://100.103.86.124:8765/`
- 当前机器若就是这台 M1 Pro，则脚本继续本地执行
- 当前机器若不是主编译机，则脚本会先通过 `rsync` 把当前工作区同步到主编译机，再通过 Tailscale SSH 到主编译机执行，并在需要时回传日志或 APK
- `adb install` 默认仍在当前发起命令的机器执行；安装脚本会先完成远端构建，再把 APK 回传到本机安装
- 使用前需先在主编译机开启 macOS `Remote Login`，否则 SSH 会被拒绝
- **硬约束：AI Agent / Codex 不得在非主编译机上直接执行 `./gradlew`。所有编译、打包、测试必须通过 `.agents/scripts/*` 触发远端路由。**
- **若任务输出中没有看到 `REMOTE_BUILD_HOST=...`，且当前机器又不是 `shawn-rains-macbook-pro`，必须立即停止并认定为错误执行路径。**
- **若任务输出中没有看到 `REMOTE_SYNC_HOST=...`，且当前机器又不是主编译机，也不能默认远端代码已更新；应停止并排查同步链路。**
- **若用户怀疑“其实跑在 Air 上”或“Pro 当前是不是满载”，优先打开主编译机性能监控页面核对，不要凭感觉判断。**

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
| "编译验证" | `.agents/scripts/compile-debug-kotlin.sh` |
| "改 UI 看效果" | `.agents/scripts/build-debug.sh`，需要安装时再执行 `adb install` |
| "先编一个 devRelease 包，别刷太多输出" | `cd ".../SmartDash" && export JAVA_HOME=... && set -o pipefail && .agents/scripts/build-dev-release.sh 2>&1 | tail -5` |

## 禁止事项

- 禁止在 MacBook Air 或任何非主编译机上直接执行 `./gradlew`
- 禁止为了“快一点”而绕过 `.agents/scripts/build-*.sh`、`.agents/scripts/compile-debug-kotlin.sh`、`.agents/scripts/test-debug.sh`
- 禁止在没有确认 `REMOTE_BUILD_HOST` 的情况下假定任务已经跑在主编译机
- 禁止在没有确认 `REMOTE_SYNC_HOST` 的情况下假定主编译机工作区已经是最新代码

## 环境变量

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/Users/shawnrain/android-sdk
export SMARTDASH_BUILD_HOST=shawn-rains-macbook-pro
export SMARTDASH_BUILD_HOST_IP=100.103.86.124
export SMARTDASH_REMOTE_PROJECT_ROOT="/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/SmartDash"
export SMARTDASH_REMOTE_JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

> **重要：** JAVA_HOME 必须指向 JDK 17，否则 JdkImageTransform 会失败。

## 低干扰构建模式

```bash
cd "/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/SmartDash" \
  && export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  && set -o pipefail \
  && .agents/scripts/build-dev-release.sh 2>&1 | tail -5
```

- 适用于 Codex / 桌面代理 / 远程终端等容易被大量日志拖慢的环境
- `tail -5` 仅显示尾部摘要，减少终端绘制开销
- `set -o pipefail` 防止构建失败被 `tail` 掩盖

## devRelease configuration cache 问题

`devRelease` 构建可能报 configuration cache 序列化错误：

```
Configuration cache state could not be cached: field `generatedModuleFile` of `JdkImageInput` ...
```

**解决方案**：添加 `--no-configuration-cache` 参数：

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew :app:assembleDevRelease \
  -Dorg.gradle.java.home=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  --no-configuration-cache --console plain
```

或者清理 cache 后重试：

```bash
rm -rf .gradle/configuration-cache/
./gradlew --stop
```

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

## 运行时性能审计心得

- SmartDash 的低端机卡顿，很多时候不是 BLE 本身，而是 `DataStore Preferences` 上层把同一份大 JSON 反复解析
- 当前已知高频热点是 `vehicle_list_json`。如果多个 `Flow` 各自 `map { VehicleProfile.listFromJson(...) }`，前台恢复、设置页、同步后刷新时会出现明显抖动
- 规则：
  - 优先复用已经解析好的 `vehicleProfiles`
  - 再从 `vehicleProfiles + currentVehicleId` 派生 `currentVehicleProfile`
  - `wheelCircumference`、`polePairs`、`lastController*` 这类 flow 尽量基于已解析对象和少量 key 组合，不要重新解析整份车辆列表
  - 新增运行时状态前，先想它是“单 key 热流”还是“整表重算流”；后者要非常克制
- 如果用户反馈“低配机卡”，优先检查：
  - `SettingsRepository` 是否新增了重复 JSON 解析
  - 同步成功后是否触发了过多全局状态重建
  - Compose 页面是否在收集过宽的应用级状态
