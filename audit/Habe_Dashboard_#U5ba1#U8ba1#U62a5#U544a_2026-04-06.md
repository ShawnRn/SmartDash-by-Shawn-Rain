# Habe Dashboard - 综合项目审计报告

**日期：** 2026 年 4 月 6 日  
**审计人：** Qwen Code AI  
**审计范围：** 架构设计、代码质量、BLE 实现、安全性、构建配置  

---

## 执行摘要

Habe Dashboard Android 项目是一个结构良好的原生 Android 应用，成功实现了 BLE 连接的电动车控制器仪表板功能。代码库展示了优秀的现代 Android 开发实践（Compose、Coroutines、DataStore），但在可测试性、BLE 可靠性和代码组织方面存在若干关键问题需要重点关注。

**整体健康评分：6.5/10**

| 类别 | 评分 | 状态 |
|----------|-------|--------|
| 架构设计 | 6/10 | ⚠️ 需要改进 |
| 代码质量 | 6/10 | ⚠️ 需要改进 |
| BLE 实现 | 7/10 | ✅ 良好但存在问题 |
| 协议处理 | 8/10 | ✅ 优秀 |
| 安全性 | 7/10 | ✅ 良好但存在漏洞 |
| 构建配置 | 6/10 | ⚠️ 需要改进 |
| 可测试性 | 3/10 | ❌ 严重缺陷 |
| 文档完整性 | 8/10 | ✅ 优秀 |

---

## 目录

1. [架构分析](#1-架构分析)
2. [代码质量问题](#2-代码质量问题)
3. [BLE 实现深度分析](#3-ble-实现深度分析)
4. [协议处理架构](#4-协议处理架构)
5. [安全性评估](#5-安全性评估)
6. [构建配置审查](#6-构建配置审查)
7. [UI/Compose 架构](#7-uicompose-架构)
8. [数据层分析](#8-数据层分析)
9. [关键问题汇总](#9-关键问题汇总)
10. [建议行动计划](#10-建议行动计划)

---

## 1. 架构分析

### 1.1 整体结构 ✅ 良好

项目遵循清晰的领域驱动结构：

```
com.shawnrain.habe/
  ├── ble/                    # BLE 引擎与协议层
  │   ├── BleManager.kt
  │   ├── ProtocolParser.kt
  │   ├── protocols/          # 控制器协议
  │   └── bms/                # BMS 协议
  ├── data/                   # 数据层
  │   ├── SettingsRepository.kt
  │   ├── VehicleProfile.kt
  │   ├── AutoCalibrator.kt
  │   └── gps/                # GPS 追踪
  ├── ui/                     # Compose 界面
  │   ├── dashboard/
  │   ├── settings/
  │   ├── bms/
  │   └── navigation/         # 共享工具函数
  └── debug/                  # 日志系统
```

**优势：**
- 职责分离清晰：BLE、数据、UI 层边界明确
- Single-Activity 架构配合 Compose 导航，符合现代 Android 开发最佳实践
- 共享工具函数（`PredictiveBackMotion`、`DialogWindowEffects`）合理抽取

**问题：**
- **目录拼写错误：** `ble/bms/protcols/` 应为 `ble/bms/protocols/`

---

### 1.2 MVVM / 状态管理 ⚠️ 需要改进

**当前方案：** 集中式单体 `MainViewModel`（2272 行）

**优势：**
- 使用 `combine()` 进行响应式组合，确保指标一致性
- 正确使用 `StateFlow.asStateFlow()` 暴露只读流
- `SharingStarted.WhileSubscribed(5000)` 防止不必要的计算

**关键问题：**

1. **上帝对象反模式：** `MainViewModel.kt` 长达 2272 行，处理 BLE 状态、骑行会话、能耗估算、GPS 校准、历史管理、局域网备份、测速、海报生成等。违反单一职责原则。

2. **纯函数中存在可变副作用：** 内部可变状态（`_tripDistanceMeters`、`_maxSpeed`、`_totalEnergyUsedWh`）在 `calculateVehicleMetrics()` 中作为副作用被修改，破坏了纯函数语义，使流变得非确定性。

3. **死亡状态：** `isDrivingMode` 被定义为 `MutableStateFlow(false).asStateFlow()` 但从未改变 — 死代码。

---

### 1.3 依赖注入 ❌ 严重缺陷

**当前方案：** 仅手动 DI — 无框架支持（Hilt/Koin/Dagger）

```kotlin
// MainViewModel.kt - 所有依赖项内联构建
private val bleManager = BleManager(application)
private val bmsBleManager = BleManager(application)
private val settingsRepository = SettingsRepository(application)
val gpsTracker = GpsTracker(application)
private val rideEnergyEstimator = RideEnergyEstimator()
```

**问题：**
- **零可测试性：** 无法 mock 依赖项进行单元测试
- **资源重复：** 两个独立的 `BleManager` 实例（控制器 + BMS）使 BLE 开销翻倍
- **紧耦合：** 每个依赖项都硬编码绑定到 `MainViewModel`
- **生命周期不透明：** `AutoCalibrator` 直接接收 `viewModelScope`，无法独立测试

**建议：** 引入 Hilt 或 Koin 进行依赖注入。最低限度应为所有依赖项提取接口层。

---

### 1.4 导航架构 ✅ 良好但存在问题

**优势：**
- `PredictiveBackMotion.kt` 是精心打造的可复用组件，提供跟手驱动的动画
- `DialogWindowEffects.kt` 提供模糊背景与 proper 动画
- 路由请求通过 `SharedFlow` 处理，使用 `DROP_OLDEST` 防止导航风暴

**问题：**
- 导航设置直接嵌入 `MainActivity.kt`（956 行），而非独立的导航图文件
- 无类型安全的路由定义 — 使用原始字符串如 `Screen.Dashboard.route`

---

## 2. 代码质量问题

### 2.1 文件大小分析 ⚠️ 文件过大

| 文件 | 行数 | 问题 |
|------|-------|-------|
| `MainViewModel.kt` | 2272 | 上帝对象 — 应拆分为多个 ViewModel |
| `SettingsScreen.kt` | 1554 | 过大 — 应抽取子组件 |
| `ZhikeParameterCatalog.kt` | 664 | 可接受，声明式定义 |
| `DashboardScreen.kt` | 1348 | 过大 — 应抽取卡片/网格组件 |
| `MainActivity.kt` | 956 | 混合生命周期、PiP、权限、导航和 UI |
| `SettingsRepository.kt` | 695 | 处理设置 + 车辆 + 历史 + 备份 — 应拆分 |

### 2.2 已识别的代码异味

1. **全局可变单例：**
   - `ProtocolParser` 是带有可变 `activeProtocol` 状态的单例对象 — 难以测试，隐藏依赖
   - `AppLogger` 单例可接受，但限制了日志输出的可测试性

2. **硬编码值：**
   - 轮询间隔（100ms）硬编码在 `BleManager` 中
   - MTU 大小（20 字节）硬编码 — 应请求 MTU 协商
   - 帧解析中的魔术数字未使用命名常量

3. **错误处理缺口：**
   - `writeBytes()` 在 characteristic 为 null 时静默返回 — 调用者无反馈
   - `runCatching { gatt.close() }` 静默吞掉错误
   - 服务发现无超时 — 可能无限挂起

4. **内存泄漏风险：**
   - `pollingHandler.postDelayed` 递归调用即使在应用后台化后仍继续
   - 进行中的分包写入无取消机制
   - `MainViewModel.onCleared()` 未断开 BLE — 连接可能残留

---

## 3. BLE 实现深度分析

### 3.1 连接生命周期 ⚠️ 发现问题

**优势：**
- 正确的状态机使用密封类（`Disconnected`、`Connecting`、`Connected`）
- 在 `STATE_CONNECTED` 时调用 `discoverServices()` — 正确的 BLE 流程
- 断开连接时清理 characteristic 引用

**关键问题：**

1. **不对称的断开连接：** `bluetoothGatt?.close()` 在 `connect()` 前无条件调用，而未先调用 `disconnect()`。`disconnect()` 方法仅调用 `bluetoothGatt?.disconnect()` 但从不调用 `close()`。这可能导致外设处于不一致状态。

2. **`connect(address, ...)` 中的竞态条件：** 方法设置 `pendingDeviceNameHint` 后调用 `connect(device)`，后者会用可能过时的 `safeDeviceName()` 结果覆盖提示。

3. **BleManager 中无重连逻辑：** 如果 `connectGatt` 失败，无重试机制。自动重连逻辑完全位于 `MainViewModel` 的看门狗扫描中 — 效率低下。

---

### 3.2 扫描实现 ⚠️ 需要改进

**问题：**

1. **🔴 无扫描超时：** `startScan()` 无超时设置。如果从不调用 `stopScan()`（应用后台化、Activity 销毁、异常），扫描将无限继续 — 电池耗尽风险。

2. **无扫描过滤器或设置：** 使用基础 `startScan(callback)` 而无 `ScanFilter` 或 `ScanSettings`。范围内的每个 BLE 广播都会触发回调。应使用：
   - Service UUID 过滤器
   - `ScanSettings` 配合适当的扫描模式
   - 回调类型过滤器

3. **低效的去重：** 使用 `any { it.address == device.address }` — 每次扫描结果 O(n)。应使用 `Set<String>` 实现 O(1) 查找。

4. **无权限检查：** 与 `connect()` 不同，`startScan()` 未检查 `BLUETOOTH_SCAN` 权限。

---

### 3.3 Characteristic 读/写 🔴 严重问题

**优势：**
- 正确处理 API 33+ 与废弃的 pre-33 路径
- 写入时尊重 `PROPERTY_WRITE_NO_RESPONSE` 与 `PROPERTY_WRITE`
- 智科大包写入时采用分包延迟写入

**严重问题：**

1. **🔴 严重：`writeBytes` 静默丢弃写入：** characteristic 为 null 时立即返回 — 调用者零反馈。对智科轮询循环（`AA13FF01AA130001` 每 100ms）尤其危险 — 如果 characteristic 尚未发现，所有写入都会静默丢失。

2. **🔴 严重：无 `onCharacteristicWrite` 回调：** Android BLE 栈返回状态码（`GATT_SUCCESS`、`GATT_WRITE_NOT_PERMITTED` 等），但未实现此回调。对写入成功/失败零可见性。对智科参数写入等关键操作，这是重大缺口。

3. **无 MTU 协商：** 硬编码 20 字节 MTU。Android 支持高达 512 字节；许多现代控制器支持 247 字节。服务发现后缺少 `requestMtu(247)`。

4. **递归分包调度：** `writeInPackets` 使用 `pollingHandler.postDelayed` 递归 — 即使应用后台化或连接中断，分包仍继续。

---

### 3.4 并发与线程安全 ⚠️ 存在问题

**当前状态：** 所有 BLE 回调运行在主 Looper（未向 `startScan` 或 `connectGatt` 传递显式执行器）。

**分析：**
- ✅ 所有回调在主线程串行化 — 当前无并发访问
- ⚠️ 这是**偶然安全**而非**显式安全**
- ⚠️ 类级别的 `@SuppressLint("MissingPermission")` — 隐藏潜在权限问题

**建议：**
- 为回调显式指定 `ContextCompat.getMainExecutor(context)`
- 或使用专用的单线程 `HandlerThread` 进行 BLE 操作
- 在代码注释中记录线程模型
- 用逐调用检查替换 `@SuppressLint("MissingPermission")`

---

### 3.5 错误处理 ⚠️ 不完整

**缺失的错误处理：**

1. **未处理 `onServicesDiscovered` 失败：** 仅处理成功路径。如果 `status != BluetoothGatt.GATT_SUCCESS`，连接会停留在"已连接"状态但无服务。

2. **无 `onCharacteristicRead` 失败处理：** 两个读回调重载都记录状态但不处理失败。

3. **服务发现无超时：** Android 的服务发现在某些设备上可能无限挂起。

4. **失败操作无重试：** 如果读/写失败，无重试逻辑。

---

### 3.6 断开连接时的清理 ⚠️ 不完整

**优势：**
- 在 `STATE_DISCONNECTED` 时清除 characteristic 引用
- 调用 `stopPolling()` 移除 100ms 轮询回调
- 通过 `ProtocolParser.reset()` 重置协议状态

**问题：**

1. **`disconnect()` 仅调用 `disconnect()`，未调用 `close()`：** 实际 `close()` 在回调中异步触发（当 `STATE_DISCONNECTED` 时）。如果回调从未触发（罕见但可能），GATT 对象会泄漏。

2. **断开前未禁用通知：** 断开前未调用 `setCharacteristicNotification(char, false)` 或未向 CCCD 写入 `DISABLE_NOTIFICATION_VALUE`。

3. **无 ViewModel 清理：** `MainViewModel.onCleared()` 未调用 `bleManager.disconnect()` — 如果 ViewModel 被销毁，连接可能残留。

---

## 4. 协议处理架构

### 4.1 策略模式 ✅ 设计优秀

**方案：** `ControllerProtocol` 接口配合评分协议选择

**优势：**
- 扩展性清晰 — 添加协议只需实现 `ControllerProtocol`
- 评分选择配合首选协议记忆机制稳健
- `ZhikeParameterCatalog`（664 行）设计极佳：
  - 声明式参数定义
  - 位范围、缩放比例、类型（UINT/INT/FLOAT/BOOL/LIST/TEXT）
  - 预设和固件版本门控
  - 类型安全的读/写操作，支持位级操作

**弱点：**

1. **协议选择仅发生一次：** 仅在 `onServicesDiscovered` 时选择。如果设备固件更新后广播不同，无运行时重新评估。

2. **无协议版本控制：** 智科协议不跟踪固件/硬件版本，尽管 `ZhikeParamDefinition` 有 `minFirmwareVersion` 字段。

3. **BMS 协议选择仅基于名称：** `BmsParser.selectProtocol` 仅检查设备名称 — 无评分或 Service UUID 分析。

---

### 4.2 智科协议 ✅ 最成熟

**优势：**
- 帧缓冲正确处理碎片化 BLE 通知
- 校验和验证（32 个 word 的和 == 0xFFFF）
- 清理函数过滤异常数据（`sanitizeSpeedCandidate`、`sanitizePhaseCurrent`）
- 设置编码/解码使用正确的小端 word 提取

**问题：**

1. **帧长度启发式：** 命令 `0x13` 的 `getFrameLength` 检查 `buffer[2]` 和 `buffer[3]` 是否匹配 4 字节 ACK 模式 — 如果合法帧恰好在这些位置有匹配字节，则很脆弱。

2. **校验和失败时返回空列表：** `extractWords` 返回空列表 — 调用代码对大多数偏移量处理了这种情况，但应更明确。

---

### 4.3 APT 与远驱协议 ⚠️ 成熟度较低

**问题：**

1. **无 CRC 验证：** 两个协议都跳过校验和/CRC，注释称"暂时跳过复杂 CRC 以避免因不匹配而丢弃帧"。损坏的帧可能发出无效指标。

2. **YuanquProtocol 使用模拟映射：** 注释称"基于典型远驱遥测的模拟映射" — 非生产就绪。

---

### 4.4 BMS 协议 ⚠️ 并行路径

**问题：**

1. **JkBmsProtocol 使用魔术偏移量：** 如 `118 - 4`、`126 - 4` 的偏移量来自小程序源码，但未充分文档化或验证。

2. **与控制器协议策略分离：** BMS 解析器位于并行路径（`BmsParser`），而非集成到相同的 `ControllerProtocol` 策略中 — 架构不一致。

---

## 5. 安全性评估

### 5.1 密钥管理 ✅ 良好

**发现：**
- ✅ 源代码中无硬编码密钥、API 密钥或密码
- ✅ 签名凭据通过 macOS Keychain 从环境变量加载
- ✅ 脚本主动阻止签名材料被提交
- ✅ 纵深防御：`fail_if_staged_signing_files()` + `warn_if_repo_contains_signing_files()`

**关注点：**
- ⚠️ `create-user-owned-release-signing.sh` 在磁盘上创建明文 `signing.env`（chmod 600，但如果 iCloud Drive 被攻破仍可见）
- ⚠ 密钥库可分辨名称硬编码为 `CN=Shawn Rain, OU=Habe Dashboard, O=Shawn Rain, L=Shanghai, ST=Shanghai, C=CN` — 泄露个人身份信息

---

### 5.2 Git 安全性 ✅ 良好

**发现：**
- ✅ `.gitignore` 排除签名材料（`*.jks`、`*.keystore`、`*.p12`、`signing.properties`）
- ✅ 排除逆向源码（`zhike_source/`、`habe_miniprogram/`）
- ✅ 排除构建产物和日志

**问题：**
- ⚠️ 使用 `*.jks`（仅根目录）而非 `**/*.jks` 进行递归匹配。`*.keystore`、`*.p12` 同理。

---

### 5.3 网络安全 ⚠️ 缺少配置

**发现：**
- ⚠️ 不存在 `network_security_config.xml`
- ⚠️ 应用声明了 `INTERNET` 权限但无网络安全配置
- ✅ Android 9+（API 28+）默认强制明文流量限制

**建议：** 添加 `res/xml/network_security_config.xml` 并显式声明 `<base-config cleartextTrafficPermitted="false">`，即使应用仅使用 BLE — 记录意图并防止未来错误。

---

### 5.4 Release 签名 ✅ 方案良好

**优势：**
- 使用 macOS Keychain 管理密码
- 条件化应用签名配置 — 如果材料缺失，回退到 debug 签名
- RSA 4096 位、100 年有效期适合长期使用的个人应用

**关注点：**
- ⚠️ 如果设置了 `releaseStoreFile` 但密码错误，构建会在签名阶段失败而非配置阶段 — 可提供更好的错误信息
- ⚠️ `debug` buildType 未显式配置 — 默认为 Android debug 密钥库（可接受但应明确声明）

---

## 6. 构建配置审查

### 6.1 根 `build.gradle.kts` ⚠️ 需要更新

**发现：**
- AGP `8.5.0` 来自 2024 年中 — AGP 8.8+ 已可用，包含安全补丁
- Kotlin `2.0.0` — Kotlin 2.1.x 已可用
- ❌ 无版本目录（`libs.versions.toml`）— 所有依赖项硬编码，使更新变得手动且易错

---

### 6.2 App `build.gradle.kts` 🔴 严重问题

**🔴 严重：Release 构建上 `isMinifyEnabled = false`**

```kotlin
release {
    isMinifyEnabled = false  // 第 41 行
    proguardFiles(...)
}
```

**影响：**
- Release APK 所有代码未混淆未压缩
- 攻击面增加 — 完整的方法/类名可见
- APK 体积更大
- 引用的 `proguard-rules.pro` 文件**不存在** — 如果启用混淆，将是潜在缺陷

**建议：** 为 release 构建启用压缩：
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

---

### 6.3 SDK 版本漂移 ⚠️ 文档不匹配

**问题：** 代码中 `compileSdk = 36` 和 `targetSdk = 36`，但 `AGENTS.md` 文档记录 `compileSdk = 35`

**风险：** Android API 36（Android 16）截至 2026 年初仍处于预览/beta 阶段。`gradle.properties` 中有 `android.suppressUnsupportedCompileSdk=36` 来压制 AGP 警告。

**建议：** 
- 回退到 `compileSdk = 35` 保证生产稳定性
- 或者如果有意针对预览版，更新 `AGENTS.md` 记录 API 36

---

### 6.4 版本管理 ⚠️ 从未递增

```kotlin
versionCode = 1  // 第 19 行
versionName = "1.0"
```

**问题：** 这些从未递增。对于任何生产交付，应该妥善管理。

**建议：** 从 git 提交计数或 CI 构建号实现自动 versionCode 生成。

---

### 6.5 依赖版本分析 ⚠️ 过时

| 依赖项 | 当前版本 | 最新稳定版 | 风险 |
|---|---|---|---|
| AGP | 8.5.0 | 8.8+ | 中 |
| Kotlin | 2.0.0 | 2.1.x | 低-中 |
| Compose BOM | 2024.11.00 | 2025.x | 低 |
| Lifecycle | 2.8.7 | 2.9.x | 低 |
| Navigation Compose | 2.8.4 | 2.9.x | 低 |
| Coroutines | 1.9.0 | 1.10.x | 低 |
| CameraX | 1.3.4 | 1.4.x | 低 |
| **Accompanist Permissions** | **0.36.0** | **维护模式** | **中** |
| ZXing Core | 3.5.3 | 3.5.3 | 无 |
| ML Kit Barcode | 17.2.0 | 17.3+ | 低 |

**关键：** Accompanist Permissions 处于维护模式。应迁移到 AndroidX 权限 API（`androidx.activity:activity-ktx` 或 `com.google.accompanist:accompanist-permissions` → `androidx.core:core-ktx` 权限助手）。

**注：** 当前依赖集中无已知关键 CVE，但整个技术栈落后 6 个月以上。

---

### 6.6 无效依赖 ⚠️ 潜在膨胀

**可能未使用：**
- `com.google.zxing:core:3.5.3`
- `androidx.camera:*`（camera-core、camera-camera2、camera-lifecycle、camera-view）
- `com.google.mlkit:barcode-scanning:17.2.0`

这些暗示了二维码/条形码扫描能力，但不清楚是否正在使用。无效依赖增加 APK 体积和潜在攻击面。

**建议：** 审计实际使用情况。如果未使用，请移除。如果用于局域网备份二维码，请在代码注释中记录。

---

## 7. UI/Compose 架构

### 7.1 优势 ✅ 优秀

1. **预测性返回动画：** `PredictiveBackMotion.kt` 提供真正的跟手驱动动画 — 缩放、位移、透明度与返回手势进度绑定。

2. **模糊背景对话框：** `DialogWindowEffects.kt` 提供 `BlurredAlertDialog` 和 `BlurredModalBottomSheet`，配合适当的模糊和动画。

3. **拖放：** 仪表卡片重新排序采用中点交换逻辑 — 精致的用户体验。

4. **横屏模式：** 仪表在横屏时隐藏底部导航并聚焦核心指标。

5. **画中画模式：** 正确通过生命周期检查和路由感知进行保护。

---

### 7.2 弱点 ⚠️ 需要重构

1. **无每页面 ViewModel 模式：** 所有页面观察单个 `MainViewModel` — UI 页面与单体 ViewModel 紧耦合。

2. **ViewModel 作为参数传递：** `MainViewModel` 直接传递给所有 composable，而非在每个页面边界使用 `viewModel()` — 阻止测试隔离。

3. **过大的 Composable：** `DashboardScreen.kt`（1348 行）、`SettingsScreen.kt`（1554 行）— 应将子组件抽取到单独文件。

4. **硬编码字符串：** 部分 UI 字符串未抽取到 `strings.xml` — 限制本地化能力。

---

## 8. 数据层分析

### 8.1 设置仓库 ⚠️ 单体

**文件：** `SettingsRepository.kt`（695 行）

**处理：**
- 设置（轮径、速度源、电池源）
- 车辆档案 CRUD
- 测速历史
- 骑行历史
- 日志级别
- 覆盖层偏好
- 备份/导出

**优势：**
- 基于 Flow 的响应式设置，使用 `flatMapLatest`
- 车辆级偏好设置（`vKey(id, key)`）设计良好
- `safeGet()` 防止损坏偏好设置导致崩溃
- 所有写入都有清理/强制转换

**问题：**
- **单个单体仓库** — 应拆分为：
  - `VehicleRepository`（车辆档案）
  - `SettingsRepository`（应用设置）
  - `HistoryRepository`（骑行/测速历史）
  - `BackupRepository`（导入/导出）

- **DataStore 与 Flow 之间无缓存层** — 每次 Flow 发射都触发完整 DataStore 读取

- **大量使用 `@OptIn(ExperimentalCoroutinesApi::class)`** 用于 `flatMapLatest` — 必要但增加噪音

---

### 8.2 AutoCalibrator ✅ 设计良好

**文件：** `AutoCalibrator.kt`

**优势：**
- 基于 GPS 的轮径校准，带抖动分析
- 步长 delta 分析和延迟补偿
- 找到稳定巡航窗口
- 使用累积样本中位数进行稳健建议
- 清晰的状态机 `GpsCalibrationState`

---

### 8.3 骑行会话追踪 ⚠️ 缺少逻辑

**文件：** `RideSession.kt`

**问题：** 仅是简单数据类，无会话管理逻辑。实际会话追踪逻辑完全位于 `MainViewModel` 中。

**建议：** 将会话管理抽取为独立的 `RideSessionManager` 类，具备 start/stop/pause/resume 能力。

---

## 9. 关键问题汇总

### 🔴 严重（生产前必须修复）

| # | 问题 | 位置 | 影响 |
|---|-------|----------|--------|
| 1 | **无 `onCharacteristicWrite` 回调** | `BleManager.kt` | BLE 写入成功/失败零可见性 — 参数写入可能静默失败 |
| 2 | **`writeBytes` 静默丢弃写入** | `BleManager.kt` | 写入被丢弃时调用者无反馈 — 轮询循环写入静默丢失 |
| 3 | **无扫描超时** | `BleManager.kt` | 如果扫描无限继续，可能导致电池耗尽 |
| 4 | **无 ViewModel `onCleared()` 清理** | `MainViewModel.kt` | ViewModel 销毁后 BLE 连接可能残留 |
| 5 | **release 构建 `isMinifyEnabled = false`** | `app/build.gradle.kts` | 未混淆 APK，攻击面增加 |
| 6 | **`proguard-rules.pro` 不存在** | `app/build.gradle.kts` | 潜在缺陷 — 如果启用混淆，构建将失败 |

---

### ⚠️ 高优先级（应尽快修复）

| # | 问题 | 位置 | 影响 |
|---|-------|----------|--------|
| 7 | **单体 MainViewModel（2272 行）** | `MainViewModel.kt` | 难以维护、测试或理解 |
| 8 | **无依赖注入框架** | 全局 | 零可测试性 — 无法 mock 依赖项 |
| 9 | **两个独立 BleManager 实例** | `MainViewModel.kt` | BLE 扫描/连接开销翻倍 |
| 10 | **未处理 `onServicesDiscovered` 失败** | `BleManager.kt` | 连接可能停留在"已连接"状态但无服务 |
| 11 | **无 MTU 协商** | `BleManager.kt` | 大包写入缓慢 — 卡在 20 字节 MTU |
| 12 | **compileSdk = 36 vs 文档 35** | `app/build.gradle.kts` | 文档漂移；API 36 可能存在破坏性变更 |
| 13 | **Accompanist Permissions 处于维护模式** | `app/build.gradle.kts` | 应迁移到 AndroidX 权限 API |
| 14 | **`startScan()` 中无权限检查** | `BleManager.kt` | Android 12+ 上无权限可能崩溃 |

---

### 💡 中优先级（建议优化）

| # | 问题 | 位置 | 影响 |
|---|-------|----------|--------|
| 15 | **过大的 Composable（1300-1500 行）** | UI 文件 | 难以维护和审查 |
| 16 | **无每页面 ViewModel 模式** | UI 架构 | 紧耦合阻止测试隔离 |
| 17 | **无类型安全导航路由** | `MainActivity.kt` | 基于字符串的路由易出错 |
| 18 | **单体 SettingsRepository（695 行）** | `SettingsRepository.kt` | 处理过多关注点 |
| 19 | **DataStore 无缓存层** | `SettingsRepository.kt` | 每次 Flow 触发完整读取 |
| 20 | **依赖项落后 6 个月以上** | `app/build.gradle.kts` | 缺少安全补丁和改进 |
| 21 | **无网络安全配置** | 缺失文件 | 应显式禁用明文流量 |
| 22 | **`versionCode = 1` 从未递增** | `app/build.gradle.kts` | 非生产就绪版本管理 |
| 23 | **Apt/Yuanqu 协议跳过 CRC** | 协议文件 | 损坏帧可能发出无效指标 |
| 24 | **拼写错误：`protcols/` 目录** | `ble/bms/protcols/` | 应为 `protocols/` |
| 25 | **`.gitignore` 使用 `*.jks` 而非 `**/*.jks`** | `.gitignore` | 子目录中的密钥库可能被提交 |

---

## 10. 建议行动计划

### 第一阶段：关键修复（立即）

1. **在 `BleManager.kt` 中实现 `onCharacteristicWrite` 回调**
   - 通过 `Channel` 或 `SharedFlow` 向调用者传播写入结果
   - 记录写入状态码用于调试

2. **修复 `writeBytes` 以提供反馈**
   - 返回 `Boolean` 或 `Result<Unit>` 而非静默返回
   - characteristic 为 null 时记录日志

3. **添加扫描超时**
   - 10 秒后自动停止扫描
   - 提供按需重新启动扫描的机制

4. **添加 ViewModel 清理**
   - 在 `MainViewModel.onCleared()` 中调用 `bleManager.disconnect()` 和 `bmsBleManager.disconnect()`

5. **为 release 构建启用压缩**
   - 创建带有适当规则的 `proguard-rules.pro`
   - 设置 `isMinifyEnabled = true` 和 `isShrinkResources = true`
   - 彻底测试 release 构建

---

### 第二阶段：架构改进（下一个迭代周期）

6. **引入依赖注入**
   - 从 Hilt 或 Koin 开始
   - 为 `BleManager`、`SettingsRepository`、`GpsTracker` 等提取接口
   - 支持使用 mock 依赖进行单元测试

7. **拆分 MainViewModel**
   - 抽取 `BleViewModel` 处理 BLE 连接/状态管理
   - 抽取 `RideSessionViewModel` 处理会话追踪
   - 抽取 `SettingsViewModel` 处理设置管理
   - 抽取 `HistoryViewModel` 处理骑行历史
   - 使用 `SavedStateHandle` 支持配置变更存活

8. **合并 BleManager 实例**
   - 单个 `BleManager` 支持多个 GATT 连接
   - 协调扫描以避免与活跃连接冲突

---

### 第三阶段：代码质量（后续迭代周期）

9. **从大文件中抽取子组件**
   - `DashboardScreen.kt`：抽取指标卡片、网格布局、连接状态
   - `SettingsScreen.kt`：抽取车辆列表、速度源选择器、备份 UI
   - `MainActivity.kt`：抽取导航图、PiP 逻辑、权限处理

10. **添加类型安全导航**
    - 使用带有 `@Serializable` 的密封类路由
    - 从基于字符串的路由迁移到类型安全路由

11. **拆分 SettingsRepository**
    - `VehicleRepository`：车辆档案 CRUD
    - `SettingsRepository`：应用设置
    - `HistoryRepository`：骑行/测速历史
    - `BackupRepository`：导入/导出

---

### 第四阶段：BLE 可靠性（持续）

12. **处理 `onServicesDiscovered` 失败**
    - 记录失败状态
    - 尝试重新发现或转换到错误状态

13. **添加 MTU 协商**
    - 服务发现后为智科设备请求 MTU 247
    - 处理 MTU 变更回调

14. **为 `startScan()` 添加权限检查**
    - 扫描前检查 `BLUETOOTH_SCAN`
    - 如果未授权则请求权限

15. **断开前添加通知禁用**
    - 向 CCCD 写入 `DISABLE_NOTIFICATION_VALUE`
    - 调用 `setCharacteristicNotification(char, false)`

16. **添加扫描过滤器和设置**
    - 按 Service UUID 过滤（0000FFE0）
    - 使用 `ScanSettings` 配合 `SCAN_MODE_LOW_LATENCY` 进行连接
    - 后台扫描使用 `SCAN_MODE_LOW_POWER`

---

### 第五阶段：维护与打磨（持续）

17. **更新依赖项**
    - AGP 升级到 8.8+
    - Kotlin 升级到 2.1.x
    - Compose BOM 升级到 2025.x
    - 从 Accompanist Permissions 迁移到 AndroidX

18. **实现版本管理**
    - 从 git 提交计数自动生成 `versionCode`
    - 通过 CI/CD 或手动流程管理 `versionName`

19. **添加网络安全配置**
    - 创建 `res/xml/network_security_config.xml`
    - 显式禁用明文流量

20. **修复 `.gitignore` 模式**
    - 将 `*.jks` 改为 `**/*.jks`
    - 将 `*.keystore` 改为 `**/*.keystore`
    - 将 `*.p12` 改为 `**/*.p12`

21. **修复拼写错误：`protcols/` → `protocols/`**

22. **审计并移除无效依赖**
    - 验证 CameraX/ZXing/ML Kit 是否在使用
    - 如果未使用则移除

---

## 附录 A：文件清单

### 核心应用文件
- `MainActivity.kt`（956 行）— Activity 生命周期、导航、PiP
- `MainViewModel.kt`（2272 行）— 上帝对象，所有应用状态
- `HabeApplication.kt` — Application 类

### BLE 层
- `ble/BleManager.kt` — BLE 连接/扫描/轮询
- `ble/ProtocolParser.kt` — 协议路由和指标
- `ble/protocols/ControllerProtocol.kt` — 接口契约
- `ble/protocols/ZhikeProtocol.kt` — 智科控制器协议
- `ble/protocols/AptProtocol.kt` — APT/AUSI 协议
- `ble/protocols/YuanquProtocol.kt` — 远驱协议
- `ble/protocols/ZhikeParameterCatalog.kt`（664 行）— 参数定义
- `ble/bms/BmsParser.kt` — BMS 协议路由器
- `ble/bms/protcols/JkBmsProtocol.kt` — JK BMS 协议
- `ble/bms/protcols/AntBmsProtocol.kt` — ANT BMS 协议

### 数据层
- `data/SettingsRepository.kt`（695 行）— 所有数据持久化
- `data/VehicleProfile.kt` — 车辆数据模型
- `data/AutoCalibrator.kt` — GPS 轮径校准
- `data/RideEnergyEstimator.kt` — 能耗/续航估算
- `data/RideSession.kt` — 骑行会话数据模型
- `data/gps/GpsTracker.kt` — GPS 速度追踪
- `data/gps/HeadingTracker.kt` — 指南针航向追踪

### UI 层
- `ui/dashboard/DashboardScreen.kt`（1348 行）— 主仪表集群
- `ui/settings/SettingsScreen.kt`（1554 行）— 设置页面
- `ui/settings/zhike/ZhikeSettingsScreen.kt` — 智科参数编辑器
- `ui/bms/BmsScreen.kt` — BMS 监控
- `ui/connect/ConnectScreen.kt` — BLE 连接 UI
- `ui/speedtest/SpeedTestScreen.kt` — 测速 UI
- `ui/poster/PosterScreen.kt` — 骑行总结海报
- `ui/navigation/PredictiveBackMotion.kt` — 预测性返回动画
- `ui/navigation/DialogWindowEffects.kt` — 模糊背景对话框
- `ui/navigation/P2PageHeader.kt` — P2 页面头部组件
- `ui/theme/` — Material 3 主题
- `ui/text/` — 文本显示工具

### 调试与工具
- `debug/AppLogger.kt` — 应用日志

### 构建与脚本
- `build.gradle.kts` — 根构建配置
- `app/build.gradle.kts` — App 构建配置
- `gradle.properties` — Gradle 属性
- `.agents/scripts/*.sh` — 自动化脚本
- `.agents/workflows/*.md` — 工作流文档

---

## 附录 B：测试状态

**当前状态：** NO-SOURCE — 仓库中不存在单元测试。

**建议：**
1. 从纯函数测试开始：
   - `ZhikeProtocol.parse()` 使用已知帧字节
   - `AutoCalibrator` 状态转换
   - `RideEnergyEstimator` 计算
   - `ZhikeParameterCatalog` 编码/解码

2. 添加 ViewModel 测试（需 DI 框架）配合 mock 依赖：
   - BLE 状态转换
   - 设置读/写
   - 骑行会话生命周期

3. 添加集成测试：
   - BLE 连接流程（需 mock BLE 外设或硬件在环）
   - 使用真实控制器的协议解析

---

## 结论

Habe Dashboard 项目展示了优秀的现代 Android 开发实践，采用 Compose、Coroutines 和清晰的领域分离。智科协议实现和参数目录设计尤为出色。

然而，该项目在 BLE 可靠性（无写入反馈、静默丢弃、无超时）、可测试性（无 DI、单体 ViewModel）和生产就绪性（无压缩、版本管理）方面存在关键缺口。在公开发布或交接前，应优先解决这些问题。

建议行动计划提供了分阶段方法，在保持开发速度的同时系统地改进代码库。从第一阶段的关键修复开始，然后逐步处理架构改进和代码质量。

---

**审计报告结束**

*生成时间：2026 年 4 月 6 日*  
*下次审查：第一阶段完成后*
