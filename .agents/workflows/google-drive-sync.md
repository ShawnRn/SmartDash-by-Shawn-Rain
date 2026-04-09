# Google Drive 云同步配置与使用

## 前置条件
- 设备已安装 Google Play Services
- 用户有 Google 账号
- OAuth 客户端 ID 已配置到代码中

## OAuth 客户端 ID 配置

### 当前客户端 ID（com.shawnrain.sdash）
`8447150714-s2l193jktl69tpc4ja7o9q0squijoj7r.apps.googleusercontent.com`

位于 `GoogleAccountAuth.kt`：
```kotlin
private const val CLIENT_ID = "8447150714-s2l193jktl69tpc4ja7o9q0squijoj7r.apps.googleusercontent.com"
```

### 旧客户端 ID（com.shawnrain.habe，仍保留以支持旧版本用户）
`8447150714-6g8ef28e8n2k7n01ek1816kqulpngu45.apps.googleusercontent.com`

如需创建新的客户端 ID，前往 [Google Cloud Console](https://console.cloud.google.com/apis/credentials) 创建 OAuth 2.0 客户端 ID（Android 类型），填入：
- **Package name**: `com.shawnrain.sdash`
- **SHA-1 fingerprint**: `9A:A1:A9:6F:2A:DB:C8:A2:62:42:04:8E:1B:5C:7D:4A:4F:A9:C5:F6`

## 加密方案

### v2（当前，跨设备兼容）
- 密钥派生：Google 账号 email → SHA-256 哈希 → AES-256 密钥
- 同 Google 账号的所有设备可互相解密
- 位置：`EncryptionService.encryptWithPassword()`

### v1（已废弃，设备绑定）
- 密钥：Android KeyStore 设备绑定密钥
- 仅创建设备可解密
- 旧备份仍兼容，新备份默认使用 v2

## 同步行为

### 鉴权结构
- `GoogleAccountAuth`：通过 Credential Manager 获取当前 Google 账号，并在本地持久化当前账号 email
- `GoogleDriveAuthorization`：向 Google Play Services 请求 `drive.appdata` scope，并为 REST API 提供 access token
- `GoogleDriveSyncManager`：继续保留 OkHttp + Drive REST 上传/下载逻辑，只替换 token 来源

### 系统备份
- `AndroidManifest.xml` 中固定使用 `android:allowBackup="false"`
- 卸载重装后不会依赖 Android 系统自动恢复旧设置
- 所有恢复路径都必须来自：
  - Google Drive 云同步
  - LAN 迁移 / Restore

### 上传
1. 用户点击"上传备份"
2. `SettingsRepository.exportBackupJson()` 导出 DataStore 快照
3. `EncryptionService.encryptWithPassword()` 使用账号 email 派生密钥加密
4. `GoogleDriveSyncManager.uploadBackup()` 上传到 Drive `appDataFolder`
5. 文件名：`habe_backup_<timestamp>.json.enc`

### 检测更新
- App 启动时检查一次 Drive 是否有新备份
- 不后台轮询，不打扰用户
- 发现更新时：上传按钮显示小圆点角标 + 小字提示

### 合并
- 下载远程备份并解密
- 车辆档案：union 合并（新增 + 按 lastModified 更新时间戳更新）
- 设置：last-write-wins（远程覆盖本地）
- 不清空本地数据

## 权限范围

使用 `drive.appdata`（应用数据文件夹）：
- 仅访问应用自己的数据
- 用户无法在 Drive 界面直接看到文件（但可通过 Drive API 管理）
- 卸载 app 不会自动删除数据
- 其他 app 无法访问

## 故障排查

### 下载失败：AEADBadTagException
- 原因：v1 设备绑定密钥备份在另一台设备无法解密
- 解决：在新设备上重新上传备份（生成 v2 密码派生密钥备份）

### 登录无反应
- 检查 Google Play Services 是否可用
- 检查 OAuth 客户端 ID 的 SHA-1 指纹是否匹配当前签名
- 检查 Google 账号是否已添加到 OAuth 同意屏幕的测试用户列表

### 同步状态卡住
- Synced/Error 状态会在 3/5 秒后自动恢复到已登录状态
- 也可切换到其他 tab 再返回刷新
