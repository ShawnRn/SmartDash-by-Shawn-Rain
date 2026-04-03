# Release 签名技能

目标：让 `com.shawnrain.habe` 的 release APK 在多台 Mac 上使用同一套签名。

## 存储策略
- keystore 文件保存在 iCloud Drive
- 密码和别名保存在钥匙串
- Gradle 通过环境变量读取签名配置

## 默认路径与服务名
- keystore: `~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Secure/habe_android/signing/habe-release.jks`
- 钥匙串服务名：
  - `habe.android.release.store.path`
  - `habe.android.release.store.password`
  - `habe.android.release.key.alias`
  - `habe.android.release.key.password`

## 推荐操作
1. 运行 `.agents/scripts/setup-release-signing.sh`
2. 运行 `.agents/scripts/preflight.sh`
3. 运行 `.agents/scripts/build-release.sh`

## 注意
- PKCS12 只使用一套密码，所以 key password 与 store password 相同
- 如果另一台 Mac 上 keystore 已同步，但钥匙串未同步，脚本无法凭空恢复密码
