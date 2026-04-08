---
name: github-release
description: Use when the user wants to publish a SmartDash Android release via GitHub Actions. Covers tag-based and manual release triggers, signing secret preparation, version bumping, and release artifact verification.
---

# SmartDash GitHub Actions Release

本 skill 用于所有涉及以下主题的修改或操作：

- 通过 GitHub Actions 发布 Android release APK
- 通过 GitHub Actions 构建“仅验证用”的 release APK，并下载 artifact 安装到手机
- 准备 release 签名 secret（base64 keystore + 钥匙串密码）
- versionCode / versionName 升级
- 创建 release tag 并推送触发 workflow
- 验证 release 构建产物与 GitHub Release 页面

## 默认分流规则

- 用户要“正式发布 / 对外交付 / 发 GitHub Release”：
  - 走 `.github/workflows/release.yml`
- 用户要“编一个 release APK 给当前设备安装验证，但不要发 release”：
  - 默认优先走 `.github/workflows/verify-release.yml`
  - 在远端构建 `:app:assembleRelease`
  - 下载 Actions artifact
  - 本机 `adb install -r`
- 只有在 GitHub Actions 不可用时，才回退到本机 `.agents/scripts/build-release.sh`

## 核心流程

### 1. 升级版本号

```kotlin
// app/build.gradle.kts
versionCode = 2026040802  // YYYYMMDDNN 递增
versionName = "1.1.1"     // 语义化版本
```

### 2. 提交并打 tag

```bash
git add app/build.gradle.kts
git commit -m "bump: version 1.1.1 (2026040802)"
git tag v1.1.1
git push origin main v1.1.1
```

推送 `v*` tag 自动触发 `.github/workflows/release.yml`。

### 3. 签名 Secret 准备

GitHub Actions 需要 4 个 secret：

| Secret | 来源 | 命令 |
|--------|------|------|
| `HABE_RELEASE_STORE_FILE_BASE64` | keystore base64 | `base64 -i habe-release.jks \| pbcopy` |
| `HABE_RELEASE_STORE_PASSWORD` | 钥匙串 | `security find-generic-password -a "$USER" -s habe.android.release.store.password -w` |
| `HABE_RELEASE_KEY_ALIAS` | 钥匙串 | `security find-generic-password -a "$USER" -s habe.android.release.key.alias -w` |
| `HABE_RELEASE_KEY_PASSWORD` | 钥匙串 | `security find-generic-password -a "$USER" -s habe.android.release.key.password -w` |

keystore 默认位置：
`~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Secure/habe_android/signing/habe-release.jks`

### 4. 验证发布

- [GitHub Actions](https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/actions/workflows/release.yml) 查看构建状态
- [Releases](https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/releases) 查看产物 APK + SHA256

## 必守规则

### 1. versionCode 必须递增

`versionCode` 使用日期格式 `YYYYMMDDNN`，每次发版必须大于上次值，否则覆盖安装会失败。

### 2. tag 格式必须以 v 开头

`v1.1.0`、`v2026.04.08-1323` 等，匹配 workflow 的 `v*` 触发条件。

### 3. 不要跳过本地编译验证

发版前务必确认本地 `./gradlew :app:assembleRelease` 或 `:app:compileReleaseKotlin` 通过。

### 4. 不要直接修改 workflow 文件

`.github/workflows/release.yml` 的修改需要单独验证，不要与版本 bump 混在同一个 commit。

## 相关文件

- `.github/workflows/release.yml` — workflow 定义
- `.github/workflows/verify-release.yml` — 构建验证版 release APK 并上传 artifact
- `app/build.gradle.kts` — versionCode / versionName / signingConfig
- `.agents/workflows/github-release.md` — 详细工作流文档
- `.agents/workflows/github-verify-release-apk.md` — GitHub Actions 验证 APK 下载与安装流程
- `.agents/workflows/release-signing.md` — 本地签名配置
