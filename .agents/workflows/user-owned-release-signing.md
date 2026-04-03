---
description: 在用户可见目录中创建并保存一套全新的 Android release 签名材料
---

目标是把 keystore 与明文关键信息保存在用户指定目录，确保用户本人始终掌握：
- keystore 文件
- store password
- key alias
- key password

### 1. 创建新的用户自持签名目录
```bash
.agents/scripts/create-user-owned-release-signing.sh
```

默认输出位置：
- `~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/habe-android-signing-时间戳/`

生成内容：
- `habe-release.jks`
- `SIGNING_INFO.txt`
- `signing.env`
- `restore-habe-signing-keychain.sh`

### 2. 作用说明
- `SIGNING_INFO.txt`：用户直接查看和备份的明文信息
- `signing.env`：用于脚本恢复构建环境
- `restore-habe-signing-keychain.sh`：把这套信息重新写回当前 Mac 的登录钥匙串

### 3. 接回仓库 release 构建链路
```bash
bash ~/Library/Mobile\ Documents/com~apple~CloudDocs/Shawn\ Rain/Vibe-Coding/habe-android-signing-时间戳/restore-habe-signing-keychain.sh
bash .agents/scripts/build-release.sh
```

### 4. 重要风险
- 这会创建一套新的签名身份
- 旧签名安装包无法被这套新签名直接覆盖升级
- 若手机中已安装旧签名版本，通常需要先卸载旧 App 再安装新 release APK
- 不要把该目录中的任何文件提交到 GitHub
