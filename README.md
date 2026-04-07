# SmartDash by Shawn Rain

`SmartDash by Shawn Rain` 是一款面向电动车/电摩骑行场景的 Android 原生仪表应用，聚焦实时仪表、BLE 控制器连接、行程记录、Google Drive 云同步与历史恢复。

当前正式版本：

- `versionName`: `1.0.3`
- `versionCode`: `2026040702`

隐私政策：

- [Privacy Policy](./PRIVACY_POLICY.md)

## 核心能力

- Jetpack Compose + Material 3 原生仪表 UI
- BLE 控制器连接、协议识别与参数读取
- 行程记录、CSV 导出、海报分享
- Google Drive 加密同步、历史版本回看与单条恢复
- GitHub Releases 应用内更新检查、下载与安装

## 本地开发

建议环境：

- JDK 17
- Android SDK + `platform-tools`
- `adb`

常用命令：

```bash
.agents/scripts/preflight.sh
.agents/scripts/build-debug.sh
.agents/scripts/build-dev-release.sh
.agents/scripts/install-dev-release.sh
.agents/scripts/build-release.sh
```

## GitHub Release

GitHub Actions 工作流：

- `.github/workflows/release.yml`

触发方式：

- 推送 tag：`v*`
- 手动触发：`workflow_dispatch`

必需 secrets：

- `HABE_RELEASE_STORE_FILE_BASE64`
- `HABE_RELEASE_STORE_PASSWORD`
- `HABE_RELEASE_KEY_ALIAS`
- `HABE_RELEASE_KEY_PASSWORD`

工作流只会产出已签名 APK，并同时上传：

- GitHub Actions artifact
- GitHub Release 资产
- `.sha256` 校验文件

OAuth 配置可用公开链接：

- `https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/blob/main/PRIVACY_POLICY.md`

## 仓库约束

- 项目规范见 [AGENTS.md](./AGENTS.md)
- 不要提交任何签名文件或密码材料
- 当前包名与 `applicationId` 仍为 `com.shawnrain.habe`，以保持现有签名、覆盖安装与同步兼容性

## License

[MIT](./LICENSE)
