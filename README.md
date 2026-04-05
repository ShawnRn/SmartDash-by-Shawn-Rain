# Habe Dashboard (Android)

`Habe Dashboard` 是 “Habe 仪表” 的 Android 原生版本，聚焦骑行仪表、BLE 控制器连接、BMS 数据、行程记录与迁移能力。

## 技术栈

- Kotlin + Jetpack Compose + Material 3
- Android SDK 36（`minSdk 26`）
- BLE: `BluetoothGatt` / `BluetoothLeScanner`
- 数据存储: `DataStore Preferences`

## 本地开发

### 环境建议

- JDK 17
- Android SDK（含 `platform-tools`）
- `adb` 可用

建议先执行：

```bash
.agents/scripts/preflight.sh
```

### 常用命令

```bash
# Debug 构建
.agents/scripts/build-debug.sh

# 使用 release 签名的高频联调包（可覆盖真机已安装 release 版本）
.agents/scripts/build-dev-release.sh
.agents/scripts/install-dev-release.sh

# 正式 release 构建
.agents/scripts/build-release.sh
```

## GitHub Actions 发布

工作流文件：

- `.github/workflows/release.yml`

触发方式：

- 推送 tag：`v*`
- 手动触发：`workflow_dispatch`

### 必需 Secrets

工作流会强制校验以下 secrets，缺失则直接失败：

- `HABE_RELEASE_STORE_FILE_BASE64`
- `HABE_RELEASE_STORE_PASSWORD`
- `HABE_RELEASE_KEY_ALIAS`
- `HABE_RELEASE_KEY_PASSWORD`

### 产物保证

工作流只允许产出“已签名 APK”：

- 仅选择 `app/build/outputs/apk/release` 下非 `*-unsigned.apk` 文件
- 上传 `actions artifact`（签名 APK + `.sha256`）
- 同时附加到 GitHub Release 资产

## 仓库说明

- 项目约束和脚本规范见 [AGENTS.md](./AGENTS.md)
- 不要提交任何签名文件（`*.jks` / `*.keystore` / `*.p12` 等）
