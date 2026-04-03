---
description: 配置并使用可跨 Mac 同步的 Android release 签名
---

目标是把 keystore 文件保存在 iCloud Drive，把密码保存在钥匙串，并通过脚本自动完成 release APK 签名。

### 1. 首次创建或检查签名
```bash
.agents/scripts/setup-release-signing.sh
```

默认位置：
- keystore 文件：`~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Secure/habe_android/signing/habe-release.jks`
- 钥匙串项：
  - `habe.android.release.store.path`
  - `habe.android.release.store.password`
  - `habe.android.release.key.alias`
  - `habe.android.release.key.password`

说明：
- keystore 文件通过 iCloud Drive 在多台 Mac 间同步
- 钥匙串密码默认写入当前用户钥匙串；若开启 iCloud Keychain，可随 Apple 账号同步到其他 Mac
- PKCS12 仅使用一套密码，因此 `key password` 与 `store password` 保持一致
- 若决定改为“用户自己持有全部关键信息”的模式，可改用：
```bash
.agents/scripts/create-user-owned-release-signing.sh
```
  这会在 `~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/` 下生成新的签名目录与明文说明文件
  注意：新签名无法直接覆盖旧签名安装包

### 2. 构建已签名 release APK
```bash
.agents/scripts/build-release.sh
```

### 3. 换一台 Mac 时
1. 确认 iCloud Drive 已同步 keystore 文件
2. 确认 iCloud Keychain 已同步密码项
3. 运行：
```bash
.agents/scripts/setup-release-signing.sh
.agents/scripts/build-release.sh
```

### 4. 验证项
- `preflight.sh` 中 `RELEASE_SIGNING=ready`
- `app/build/outputs/apk/release/app-release.apk` 存在
