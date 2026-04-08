---
description: SmartDash GitHub Actions 自动化发版流程
---

本工作流用于通过 GitHub Actions 自动构建、签名并发布 SmartDash Android Release APK。

如果目标只是“构建一份 release APK 给当前设备验证安装，但不要发 GitHub Release”，不要走本文档，改走 `.agents/workflows/github-verify-release-apk.md`。

## 触发方式

### 方式 1：推送 tag（推荐）

```bash
# 1. 更新 app/build.gradle.kts 中的 versionCode / versionName
# 2. 提交并打 tag
git tag v1.1.0
git push origin main v1.1.0
```

推送 `v*` 格式的 tag 会自动触发 release workflow。

### 方式 2：手动触发

前往 [GitHub Actions → SmartDash Android Release → Run workflow](https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/actions/workflows/release.yml)

- `tag`：留空自动生成（格式 `vYYYY.MM.DD-HHmm`），或手动指定如 `v1.1.0`
- `prerelease`：是否标记为预发布（默认 false）

## 前置条件

### GitHub Secrets（必需）

在 [Repository Settings → Secrets and variables → Actions](https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/settings/secrets/actions) 中配置：

| Secret | 说明 | 获取方式 |
|--------|------|----------|
| `HABE_RELEASE_STORE_FILE_BASE64` | keystore 文件的 base64 编码 | `base64 -i habe-release.jks \| pbcopy` |
| `HABE_RELEASE_STORE_PASSWORD` | keystore 密码 | 从本地钥匙串获取 |
| `HABE_RELEASE_KEY_ALIAS` | 签名 key alias | 从本地钥匙串获取 |
| `HABE_RELEASE_KEY_PASSWORD` | key 密码 | 通常与 store password 一致 |

### 从本地钥匙串读取密码

```bash
# 读取 keystore 路径
security find-generic-password -a "$USER" -s habe.android.release.store.path -w

# 读取 keystore 密码
security find-generic-password -a "$USER" -s habe.android.release.store.password -w

# 读取 key alias
security find-generic-password -a "$USER" -s habe.android.release.key.alias -w

# 读取 key 密码
security find-generic-password -a "$USER" -s habe.android.release.key.password -w
```

### 从本地 keystore 生成 base64

```bash
# 假设 keystore 在 iCloud Drive 默认位置
STORE_PATH=~/Library/Mobile\ Documents/com~apple~CloudDocs/Shawn\ Rain/Secure/habe_android/signing/habe-release.jks
base64 -i "$STORE_PATH" | pbcopy
# 粘贴到 GitHub Secret: HABE_RELEASE_STORE_FILE_BASE64
```

## 构建流程

workflow 执行以下步骤：

1. **Checkout** — 拉取代码（含完整 git history）
2. **Set up Java 17** — 配置 Temurin JDK 17 + Gradle 缓存
3. **Resolve tag** — 解析 tag / versionName / versionCode
4. **Validate secrets** — 验证 4 个签名 secret 是否齐全
5. **Prepare signing** — base64 解码 keystore 到 runner temp 目录
6. **Build release APK** — `./gradlew :app:assembleRelease`
7. **Collect artifact** — 提取签名 APK + SHA256
8. **Upload artifact** — 上传到 GitHub Actions（保留 30 天）
9. **Create GitHub release** — 自动创建 release，附 APK + SHA256 + release notes

## 输出产物

| 产物 | 位置 |
|------|------|
| 签名 APK | `smartdash-v1.1.0-release.apk` |
| SHA256 | `smartdash-v1.1.0-release.sha256` |
| GitHub Release | [Releases](https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/releases) |
| Actions Artifact | [Artifacts](https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/actions)（30 天） |

## 版本管理约定

- `versionCode`：日期格式 `YYYYMMDDNN`（如 `2026040801`），递增保证覆盖安装
- `versionName`：语义化版本 `MAJOR.MINOR.PATCH`（如 `1.1.0`）
- `tag`：以 `v` 开头，如 `v1.1.0`

## 故障排查

### 构建失败：Missing required secret

检查 [Repository Settings → Secrets](https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/settings/secrets/actions) 中 4 个 secret 是否全部配置且名称正确。

### 构建失败：No signed APK found

通常是签名配置问题。确认：
- `HABE_RELEASE_STORE_FILE_BASE64` 解码后是有效的 .jks 文件
- keystore 密码 / alias / key 密码正确
- `app/build.gradle.kts` 中 signingConfig 配置正确

### Tag 已存在被拒绝

```bash
# 删除本地 tag 并重建
git tag -d v1.1.0
git tag v1.1.0
git push origin v1.1.0 --force
```

### 并发构建

workflow 使用 `concurrency` 组防止同版本并发构建。不同 tag 可以并行构建。

## 相关文件

- `.github/workflows/release.yml` — workflow 定义
- `app/build.gradle.kts` — versionCode / versionName / signingConfig
- `.agents/scripts/build-release.sh` — 本地 release 构建脚本（参考）
- `.agents/workflows/release-signing.md` — 本地签名配置
