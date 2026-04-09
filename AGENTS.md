# SmartDash by Shawn Rain - Android 原生项目 (AGENTS.md)

## 1. 项目目标
`SmartDash by Shawn Rain` 是一款 Android 原生骑行仪表项目，目标是把高频骑行仪表、控制器协议、BMS、测速、调参、行程记录与跨设备同步能力稳定落到本地端。

当前仓库定位：
- Android 原生骑行仪表主工程
- 以智科协议为第一优先级落地对象
- 以 Compose + Kotlin 为 UI 与业务主栈
- 以 `.agents/scripts` + `.agents/workflows` 作为本地自动化入口

## 2. 技术栈与构建基线
- `namespace` / `applicationId`: `com.shawnrain.sdash`（2026-04-08 从 `com.shawnrain.habe` 迁移）
- 品牌名 / 桌面显示名: `SmartDash`
- Android Gradle Plugin: `8.5.0`
- `compileSdk`: `36`（`gradle.properties` 中 `android.suppressUnsupportedCompileSdk=36` 压制警告）
- `targetSdk`: `36`
- `minSdk`: `26`
- Kotlin JVM target: `17`
- UI: Jetpack Compose + Material 3
- 导航: `androidx.navigation:navigation-compose`
- 存储: `DataStore Preferences`
- BLE: 原生 `BluetoothGatt` / `BluetoothLeScanner`
- 云同步: Google Drive REST API + Google Sign-In
- 加密: AES-256-GCM（密码派生密钥，跨设备兼容）

构建注意：
- 当前 AGP `8.5.0` 对 `compileSdk = 36` 会有官方测试范围警告，但不阻塞 `assembleDebug`
- Java 17 是当前稳定构建前提
- **编译前务必确认 `JAVA_HOME` 指向 JDK 17**，指向其他版本（如 AGP 内置 JDK 21）会导致 `JdkImageTransform` 失败
- Gradle 已启用 daemon / parallel / build cache / configuration cache / R8 fullMode，用于缩短本地迭代构建时间
- Release 构建已启用 `isMinifyEnabled = true` 和 `isShrinkResources = true`（R8 压缩）
- `proguard-rules.pro` 位于 `app/` 根目录，保留 Compose、BLE、协议类、云同步类免于混淆
- `devRelease` 构建可能因 configuration cache 序列化问题失败，需加 `--no-configuration-cache` 参数

### 主编译机约定

- SmartDash 的唯一主编译机固定为这台 `MacBook Pro M1`
  - 设备名：`shawn-rains-macbook-pro`
  - Tailscale IP：`100.103.86.124`
- 所有 `Gradle compile / assemble / test` 任务默认优先在主编译机执行：
  - 若当前就是主编译机，脚本继续本地执行
  - 若当前不是主编译机，脚本通过 Tailscale SSH 自动路由到主编译机执行
- `adb install` 默认仍在当前发起命令的机器执行，不跟随远端编译：
  - 在非主编译机上执行“装到手机上”时，标准流程是“远端构建 -> 产物回传到当前机器 -> 当前机器本地 adb 安装”
- 前置条件：
  - 两台 Mac 都已登录同一 Tailscale 网络
  - 主编译机已开启 macOS `Remote Login`
  - 非主编译机到主编译机的 SSH 已免密可用
  - 主编译机已配置 Android SDK、JDK 17、release 签名和仓库工作区
  - 两台机器的仓库路径保持一致，或通过环境变量覆盖远端仓库路径

### 编译速度保障

**关键配置（`gradle.properties`）：**
- `org.gradle.daemon=true` / `parallel=true` / `caching=true` / `configuration-cache=true`
- `org.gradle.workers.max=4`（避免资源争用）
- `kotlin.incremental=true` / `useClasspathSnapshot=true` / `usePreciseJavaTracking=true`
- `kotlin.daemon.jvmargs=-Xmx2048m`（独立进程，避免 OOM）
- `android.enableR8.fullMode=true` / `enableR8.desugaring=true`

**典型编译耗时（M1/M2 Mac）：**

| 场景 | 命令 | 热启动耗时 |
|------|------|-----------|
| 仅编译 Kotlin | `:app:compileDebugKotlin` | ~3-5s |
| Debug APK | `:app:assembleDebug` | ~8-15s |
| devRelease APK | `install-dev-release.sh` | ~15-30s |
| fastDevRelease APK | `install-fast-dev-release.sh` | ~10-20s |
| Release APK | `build-release.sh` | ~1-2min |

**AI Agent 编译决策树：**
- 用户说"装到手机上" → `.agents/scripts/install-dev-release.sh`（不二次确认）
- 用户说"出个包" → `.agents/scripts/build-release.sh`
- 用户说"编译验证" → `.agents/scripts/compile-debug-kotlin.sh`
- 用户说"改 UI 看效果" → `.agents/scripts/build-debug.sh`，需要安装时再接 `adb install`

**推荐的低干扰 devRelease 构建方式：**
- 当目标是"编一个可覆盖安装的联调包"，优先使用输出节流版命令，避免终端刷屏拖慢桌面响应：

```bash
cd "/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/SmartDash" \
  && export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  && set -o pipefail \
  && .agents/scripts/build-dev-release.sh 2>&1 | tail -5
```

- 这条命令的约定含义：
  - `build-dev-release.sh`：继续使用 release 签名，可直接覆盖用户现有安装
  - `JAVA_HOME` 显式固定到 JDK 17，避免 `JdkImageTransform` / `jlink` 走错 JVM
  - `tail -5`：只保留构建尾部关键信息，减少桌面终端渲染压力
  - `set -o pipefail`：即使最后接了 `tail`，也不会吞掉构建失败状态

- 若 `devRelease` 构建报 configuration cache 序列化错误，使用：

```bash
cd "/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/SmartDash" \
  && export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  && ./gradlew :app:assembleDevRelease -Dorg.gradle.java.home=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home --no-configuration-cache --console plain
```

详见：`.agents/workflows/build-performance.md`

推荐环境变量：
- `ANDROID_HOME=/Users/shawnrain/android-sdk`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
- `ANDROID_SDK_ROOT=/Users/shawnrain/android-sdk`
- `SMARTDASH_BUILD_HOST=shawn-rains-macbook-pro`
- `SMARTDASH_BUILD_HOST_IP=100.103.86.124`
- `SMARTDASH_REMOTE_PROJECT_ROOT=/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/SmartDash`
- `SMARTDASH_REMOTE_JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`

Release 签名约定：
- keystore 文件默认保存在 iCloud Drive：
  - `~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Secure/habe_android/signing/habe-release.jks`
- release 构建密码默认保存在用户钥匙串：
  - `habe.android.release.store.path`
  - `habe.android.release.store.password`
  - `habe.android.release.key.alias`
  - `habe.android.release.key.password`
- 若开启 iCloud Drive 与 iCloud Keychain，可在多台 Mac 间复用同一套 release 签名
- 若用户明确要求"本人掌握全部签名关键信息"，允许改用：
  - `.agents/scripts/create-user-owned-release-signing.sh`
  - 输出目录默认位于 `~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/sdash-signing-时间戳/`
  - 目录内会包含 keystore、明文签名信息、以及恢复 Keychain 的脚本
  - 这会生成一套新的签名身份，无法直接覆盖安装旧签名版本
- 不要把任何 keystore、证书、签名配置文件、密码文件提交到 GitHub
- 签名相关信息只允许存放在：
  - iCloud Drive 中的 keystore 文件
  - macOS / iCloud Keychain 中的密码项
- `.agents/scripts/preflight.sh` 会提示仓库内是否存在签名文件
- `.agents/scripts/sync-github.sh` 会阻止任何签名文件被提交或推送

> **⚠️ 重要强约束 (AI Agent 必读)**
> 当用户要求"输出/编译一个 APK 给我测试"或"提供最新安装包"时，**必须**调用 `.agents/scripts/build-release.sh` 生成 Release 签名包！
> 绝对不要默认通过 `build-debug.sh` 输出给用户，因为用户的物理真机已安装了 Release 签名版本，使用 Debug 签名会导致覆盖安装失败或需要清除数据！
> 当用户明确要求"装到手机上""安装到手机""adb 安装"或语义等价的真机安装诉求时，**默认直接执行安装流程，不要再次征求确认**；除非用户明确要求"先别装"或"只出包不安装"。
>
> `devRelease` 仅用于本地高频联调提速：
> - `.agents/scripts/build-dev-release.sh`
> - `.agents/scripts/install-dev-release.sh`
> - 仍使用 release 签名，可覆盖用户现有安装
> - 但当用户明确要求"最新测试安装包/交付 APK"时，仍必须使用正式 `build-release.sh`
>
> `fastDevRelease` 仅用于本地个人快速联调：
> - `.agents/scripts/build-fast-dev-release.sh`
> - `.agents/scripts/install-fast-dev-release.sh`
> - 继续使用 release 签名
> - 默认跳过 `lintVital`
> - 若命中 `project_dex_archive/<variant>` 重复类错误，会自动清理对应 dex 中间产物并重试一次
> - 不要把 `fastDevRelease` 当作最终交付或归档构建

新 Mac 首次装机建议：
- 先跑 `.agents/scripts/bootstrap-mac.sh`
- 再跑 `.agents/scripts/preflight.sh`
- 最后跑 `.agents/scripts/compile-debug-kotlin.sh`

## 3. 当前代码结构
核心目录：
- `app/src/main/java/com/shawnrain/sdash/MainActivity.kt`: 主导航与页面装配
- `app/src/main/java/com/shawnrain/sdash/MainViewModel.kt`: 应用级状态、BLE 数据汇总、设置与调参动作、云同步、方向稳定化
- `app/src/main/java/com/shawnrain/sdash/ble/`: BLE 引擎、协议路由、控制器解析
  - `BleManager.kt`: 扫描、连接、通知、写入、轮询、MTU 协商、写入结果遥测
  - `ProtocolParser.kt`: 协议识别与路由，`_metrics` 使用 `SharedFlow` 保证每帧必达
  - `protocols/`: 控制器协议实现（智科/安能特/远驱）
  - `bms/protocols/`: BMS 协议实现（注意：`ble/bms/protocols/` 为正确拼写）
- `app/src/main/java/com/shawnrain/sdash/data/`: DataStore、GPS、校准、骑行会话、能量估算
  - `telemetry/`: 遥测处理管线
    - `TelemetryStreamProcessor.kt`: BLE 原始指标 → TelemetrySample（去重/质量分类/有限值守卫）
    - `RideAccumulator.kt`: 物理积分（能量/距离/时间），dtMs 钳位 50-500ms
    - `BatteryStateEstimator.kt`: SoC 估算（Ah 积分 + OCV 融合 + 内阻学习）
    - `RangeEstimator.kt`: 续航估算（滑动窗口效率模型）
    - `BatteryState.kt`: 电池状态输出（SoC/置信度/滤波电压电流）
    - `RangeEstimate.kt`: 续航输出（里程/剩余能量/窗口效率）
    - `SampleQuality.kt`: 样本质量枚举（GOOD/DUPLICATE/TOO_DENSE/GAP_RESET/OUTLIER）
    - `TelemetrySample.kt`: 统一遥测样本数据结构
    - `TelemetryCommon.kt`: 公共类型（EnergyWh, EfficiencyWhKm）
  - `gps/`: 方向与位置处理
    - `GpsTracker.kt`: GPS 速度追踪
    - `HeadingTracker.kt`: 传感器 + GPS bearing 原始方向融合，暴露 6 个 StateFlow
    - `DirectionStabilizer.kt`: 5 级方向稳定化管线（冻结/门控/死区/平滑/限速转向）
    - `DirectionLabelFormatter.kt`: 8 方位文字格式化 + 12° 滞回
  - `sync/`: Google Drive 云同步
    - `BackupModels.kt`: 数据模型（EncryptedBackup, BackupMetadata, SyncState）
    - `EncryptionService.kt`: AES-256-GCM 加密（v1 设备绑定 / v2 密码派生）
    - `GoogleDriveAuth.kt`: Google Sign-In OAuth 认证（Client ID 已更新为 sdash 包名）
    - `GoogleDriveSyncManager.kt`: Drive 上传/下载/列表/删除
  - `migration/`: LAN 备份传输
  - `VehicleProfile.kt`: 车辆档案模型，NaN finiteOr 守卫 + JSON 序列化保护
  - `SettingsRepository.kt`: DataStore 持久化层，normalizedProfile finiteOr + runCatching 防崩
  - `RideSession.kt`: 骑行会话摘要
  - `AutoCalibrator.kt`: 自动校准器
  - `WheelPresets.kt`: 轮径预设
- `app/src/main/java/com/shawnrain/sdash/ui/`: 仪表、连接、设置、BMS、测速界面
  - `navigation/PredictiveBackMotion.kt`: 预测性返回动画公共 helper
  - `navigation/DialogWindowEffects.kt`: Dialog / sheet / overlay 窗口模糊与透明系统栏 helper
  - `navigation/P2PageHeader.kt`: 二级页公共头部组件
  - `navigation/SecondaryScreenTopBar.kt`: 二级页顶部导航
  - `dashboard/DashboardScreen.kt`: 仪表首页
  - `settings/SettingsScreen.kt`: 设置页
  - `settings/zhike/ZhikeSettingsScreen.kt`: 智科调参页
  - `bms/BmsScreen.kt`: BMS 监控页
  - `speedtest/SpeedtestScreen.kt`: 测速页
  - `poster/PosterRenderer.kt`: 骑行海报渲染
  - `text/DisplayTextFormatter.kt`: 显示文本格式化
  - `theme/Theme.kt`: Material 主题
  - `theme/BezierRoundedShape.kt`: 贝塞尔圆角形状
- `app/src/main/java/com/shawnrain/sdash/debug/`: 应用内日志系统
  - `AppLogger.kt`: 日志系统核心
- `app/src/main/java/com/shawnrain/sdash/data/history/RideHistoryModels.kt`: 行程记录数据模型
- `app/src/main/java/com/shawnrain/sdash/data/speedtest/`: 测速数据模型与追踪器
- `app/src/main/java/com/shawnrain/sdash/data/update/`: 应用更新管理
- `.agents/skills/smartdash-overlay-dialog/SKILL.md`: SmartDash 统一 overlay / dialog 设计与交互约束
- `.agents/skills/ble-telemetry-estimation/SKILL.md`: BLE 遥测估算约束
- `.agents/skills/direction-stabilization/SKILL.md`: 方向显示稳定化约束
- `.agents/scripts/`: 可执行脚本
- `.agents/workflows/`: 可复用流程说明

本地参考目录：
- `zhike_source/`: 智科相关逆向参考，已忽略版本控制
- `habe_miniprogram/`: 小程序参考源码，默认忽略版本控制
- `tools/unveilr/`: 本地逆向辅助工具，默认忽略版本控制

## 4. 已实现能力

### 4.1 仪表与交互
- 仪表首页已支持速度、功率、平均能耗、电压、母线电流、相电流、温度、里程、电量等基础显示
- 支持拖拽重排仪表卡片
- 顶层底部 Tab 已恢复轻量级切页动画，不再硬切
- 顶部状态带现包含 `SoC / 连接状态 / 估计续航`，中间连接状态固定居中，不再受左右文案长度影响
- 底部导航已移除单独的"连接"Tab，改为点击仪表页顶部"连接状态"弹出连接浮窗
- 横屏模式切换为专注布局，仅显示顶部 3 个核心指标：
  - 速度
  - 功率
  - 平均能耗
- 仪表页进入后保持亮屏
- 全局圆角已改为连续曲率的贝塞尔 / superellipse 风格
- Dashboard 横屏时隐藏 app 自身底部导航，但不隐藏系统状态栏和导航栏
- 右上角方向显示已改为稳定行进方向（非车头朝向），具有低速冻结/死区/平滑/转向限速

### 4.2 BLE 与协议
- 已实现基础 BLE 扫描、连接、通知绑定、写特征选择
- 已接入协议自动识别：
  - `zhike`
  - `apt-ausi`
  - `yuanqu`
- `BleManager` 已为智科加入：
  - `FFE1` 主写特征优先路由
  - `FFE2` 辅助写特征预热
  - `FFF1/FFF2` 握手特征一次性读取
  - 新版 `onCharacteristicChanged(..., value)` 回调支持
- 已在 BLE 链路中加入日志埋点：
  - 扫描设备
  - 连接状态
  - 服务发现
  - 写特征选择
  - 指令发送十六进制预览
  - 通知回包十六进制预览
- `ProtocolParser._metrics` 已从 `MutableStateFlow` 改为 `MutableSharedFlow`，保证每帧必达（修复里程/能耗为 0 的根因）

### 4.3 智科协议
- 已实现智科实时帧解析：
  - 电压
  - 母线电流
  - 相电流
  - RPM
  - 电机温度
  - 控制器温度
  - 故障码
  - 制动 / 巡航 / 倒车状态
- 已实现智科参数帧读写基础：
  - 读取 `AA 11 00 01`
  - 写入 `AA 12 ...`
  - 写前固定解锁帧
- 已支持从参数区解析并校验蓝牙密码
- 已支持从参数区回填极对数，并自动同步到本地设置
- 已实现智科调参二级页，支持基础参数：
  - 母线电流
  - 相线电流
  - 欠压 / 过压
  - 电机方向
  - 极对数
  - 相位角
  - 弱磁电流
  - 反峰制动电流
- 已实现连续全 0 帧检测与抑制（防止无效遥测流污染上层）

### 4.4 GPS 与速度校准
- 已实现基于手机 GPS 的轮径校准会话
- GPS 轮径校准入口保留在设置一级页面
- 校准建议在保存时会直接应用到当前活跃车辆档案
- 校准时持续比较：
  - GPS 速度 / 距离
  - 控制器 RPM 推算速度 / 距离
- 可输出推荐轮径周长并一键应用

### 4.5 行程记录与图表
- 行程详情"统计概览"已改为整段行程统计值，不再把单个采样点误当作概览
- 概览参数卡片可直接切换对应图表
- 行程图表已支持平均值参考线与最高点指示
- 行程 CSV 已补齐电机温度、估计续航、累计平均能耗、总能耗、回收能量、控制器最高温等字段
- 仪表页平均能耗、续航估算、行程记录与导出 CSV 已统一到同一套累计能量 / 里程口径

### 4.6 日志与后台画中画
- 已实现应用内日志系统 `AppLogger`
- 设置页可配置日志级别：
  - `VERBOSE`
  - `DEBUG`
  - `INFO`
  - `WARN`
  - `ERROR`
- 设置页可导出 / 分享日志
- 已将后台小窗能力切换为 Android 原生 `PiP`
- `PiP` 内容固定为顶部 3 个指标：
  - 速度
  - 功率
  - 平均能耗
- app 切后台时可自动进入；回前台时自动退出
- 不再依赖全局悬浮权限或独立前台服务

### 4.7 自动连接与返回导航
- 已加入轻量守护扫描：断开连接后会按短扫描窗口寻找记忆控制器，发现目标信号后自动连接
- 手动断开后会进入自动重连抑制窗口，避免用户主动断开后被立刻抢连
- `BMS` 与 `智科设置` 二级页面已接入 Compose `PredictiveBackHandler`
- 自定义 Dialog、全屏 overlay、页面内自绘详情层已开始统一复用预测性返回动画 helper
- `AlertDialog`、`ModalBottomSheet`、自定义 `Dialog` 默认都要通过公共窗口 helper 铺设背景模糊
- 返回手势过程中支持页面跟手缩放 / 位移预览
- 顶层 Tab 切换与二级页返回动画分离：
  - 顶层 Tab 使用轻量级切页动效
  - 二级页返回保留单独的返回过渡

### 4.8 Google Drive 云同步
- 已实现基于 Google Drive REST API 的跨设备数据同步
- 使用 Google Sign-In OAuth 认证，最小权限 `drive.appdata`（仅访问应用专属文件夹）
- 加密方案：AES-256-GCM，密码派生密钥（Google 账号 email SHA-256），同账号跨设备可解密
- 同步策略：润物细无声的 iCloud 式体验
  - App 启动时静默检查一次 Drive 更新（无后台轮询）
  - 发现其他设备更新时，上传按钮显示小圆点角标 + 小字提示
  - 点击合并不弹窗，直接静默合并
  - 合并逻辑：车辆档案 union（新增+按 lastModified 更新），设置 last-write-wins
  - 上传/合并成功后 3 秒自动恢复初始状态
  - 错误状态 5 秒后自动恢复
- 备份版本管理：文件名含时间戳，Drive 列表按时间倒序
- 降级兼容：v1 设备绑定密钥（旧设备）→ v2 密码派生密钥（新设备）自动识别版本解密
- OAuth Client ID 已更新为 `com.shawnrain.sdash` 包名：`8447150714-s2l193jktl69tpc4ja7o9q0squijoj7r.apps.googleusercontent.com`

### 4.9 BLE 可靠性增强
- `writeBytes()` 不再静默丢弃：返回 `WriteResult`，特征为 null 时记录警告
- 已实现 `onCharacteristicWrite` 回调，写入状态通过 `SharedFlow<WriteResult>` 暴露
- 扫描优化：
  - 10 秒超时自动停止
  - Service UUID 过滤器（`0000FFE0`）减少回调噪声
  - 权限检查：Android 12+ 检查 `BLUETOOTH_SCAN`，Android 11 检查 `ACCESS_FINE_LOCATION`
  - 去重使用 `Set<String>` O(1) 查找
- 服务发现失败路径：转换到 `ConnectionState.Error` 并自动断开
- `MainViewModel.onCleared()` 中断开 BLE 连接，防止 GATT 对象泄漏
- 断开前禁用通知（写 `DISABLE_NOTIFICATION_VALUE` 到 CCCD）
- MTU 协商：服务发现后请求 247 字节 MTU（智科设备）
- 速度异常过滤：
  - 移除固定极速上限（适配多车型）
  - 增强变化率过滤：400ms 内变化不超过 8km/h（低负载）或 25km/h（正常）
  - RPM 与速度不匹配检测：有速度但 RPM 接近 0 时使用前值
  - 防止控制器断连/下电时出现异常高值（如 80km/h 假值）

### 4.10 方向显示稳定化
- 右上角方向已从"偏车头朝向"改为"偏稳定行进方向"
- 5 级处理管线：速度冻结 → 来源质量门控 → 死区 → 速度分层指数平滑 → 转向速率限制
- 低速冻结：速度 < 3 km/h 时方向冻结
- GPS 优先：速度 > 8 km/h 且 GPS bearing 质量合格时以 GPS 为主
- 死区：5° 以内变化不更新
- 平滑：速度分层 alpha（0.00 ~ 0.30）
- 转向限速：35°/s ~ 90°/s 按速度分层
- 文字方位：8 方位 + 12° 滞回防止边界跳字

### 4.11 崩溃防护（P0 Hotfix 已完成）
- `VehicleProfile.toJson()` 所有 Float 字段使用 `finiteOr()` 拦截 NaN
- `VehicleProfile.fromJson()` 读入时 NaN 清洗 + 二次 coerce 归一化
- `SettingsRepository.normalizedProfile()` 先 `finiteOr()` 再 `coerceAtLeast/coerceIn`
- `SettingsRepository.saveVehicleProfiles()` 外层包 `runCatching` 防止 crash 传播
- `TelemetryStreamProcessor` 所有输入先 `isFinite()` 检查，非有限值标记 OUTLIER
- `MainViewModel` 三个学习函数（`blendLearnedEfficiencyWhKm`/`calculateLearnedUsableEnergyRatio`/`blendLearnedUsableEnergyRatio`）全部加 NaN 守卫
- `ZhikeProtocol` 连续全 0 帧不再发射给上层，不覆盖上一次有效遥测

## 5. 当前迁移状态
- [x] Android 原生基础架构
- [x] Compose 仪表主界面
- [x] BLE 扫描、连接、通知、基础协议识别
- [x] 智科协议实时解析基础版
- [x] 智科参数读取 / 基础写入 / 密码校验
- [x] GPS 轮径校准
- [x] 应用内日志与分享
- [x] 后台 `PiP` 三指标 HUD
- [x] Google Drive 云同步（跨设备、合并、加密）
- [x] BLE 写入可观测性、扫描超时、权限检查
- [x] Release 构建启用 R8 压缩与资源压缩
- [x] 包名迁移 `com.shawnrain.habe` → `com.shawnrain.sdash`
- [x] NaN 持久化崩溃防护（P0 Hotfix）
- [x] StateFlow 去重导致里程/能耗为 0 修复（SharedFlow）
- [x] 全 0 帧抑制 + TelemetryStreamProcessor 有限值守卫
- [x] dtMs 积分窗口放宽 + RideAccumulator 物理积分安全钳位
- [x] 方向显示稳定化（5 级管线 + 文字滞回）
- [x] OAuth Client ID 更新为 sdash 包名
- [ ] 智科调参项与小程序完全对齐
- [ ] 智科实车联调闭环验证完全稳定
- [ ] BMS 多品牌迁移完成
- [ ] 更完整的测速 / 制动 / 海报能力迁移
- [ ] 本地单元测试覆盖

## 5.1 遥测估算重构约束 (United Energy Standard)
- SmartDash 的控制器数据来自 `BLE`，到包时间不均匀，不能把 UI 聚合流当成物理采样时钟
- **`Wh / Ah / peak / distance` 等累计量必须只由"新鲜控制器样本"驱动**，不能由 `GPS / BMS / 设置项 / UI` 刷新重复触发
- **统一能量标准 (United Energy Standard)**：
    - **Display (UI)**: 所有面向用户的能耗 (Efficiency) 显示，**默认必须强制采用 `Net Wh` (Traction - Regen) 口径**。
    - **Estimation (Range)**: 续航预测模型必须基于 `Net Wh` 能效基准。
    - **Analysis**: 仅在后台分析或特定详情页允许区分毛能耗 (Traction) 与回收贡献 (Regen)。
- 当前遥测管线（已完成）：
  - `BLE frame → ProtocolParser → SharedFlow<VehicleMetrics>`
  - `→ TelemetryStreamProcessor → TelemetrySample`（去重/质量/有限值守卫）
  - `→ RideAccumulator.accumulate()`（dtMs clamp 50-500ms，仅 allowIntegration 时积分）
  - `→ BatteryStateEstimator.estimate()`（Ah+OCV 融合 SoC）
  - `→ RangeEstimator.estimate()`（滑动窗口效率模型）
- `SoC`、`remainingEnergyWh`、`estimatedRangeKm` 必须统一口径；不要把带强先验的车辆档案百分比再反推成"独立容量证据"
- `voltageSag` 需要区分展示值与分析值，避免在短时低流瞬间频繁重写静置基线
- 任何涉及上述链路的修改，都优先阅读：
  - `.agents/skills/ble-telemetry-estimation/SKILL.md`
  - `.agents/workflows/telemetry-refactor.md`

## 6. 已知问题与风险
- 智科调参页目前只是基础子集，尚未达到小程序同等级别的丰富参数矩阵
- 当前仓库本地单元测试仍为 `NO-SOURCE`
- 后台小窗已改为原生 `PiP`，仅在支持 `Picture-in-Picture` 的系统路径下生效
- `devRelease` 首次构建或 Gradle 脚本变更后，configuration cache 需要重建；若遇序列化错误需加 `--no-configuration-cache`
- 当前 AGP 8.5.0 在 `devRelease` / `release` 上偶发 dex 中间产物重复问题；清理对应 `project_dex_archive/<variant>` 后可恢复
- Google Drive 同步依赖 Google Play Services，在无 GMS 设备（如华为鸿蒙）上不可用
- `VehicleProfile.lastModified` 字段已添加但旧数据默认为 0L，首次合并时可能误判为较新
- SOC / 续航显示仍可能存在一定波动（电压法 + 负载压降 + 动态恢复本质特性），后续可进一步优化平滑参数

## 7. 智科协议归档

### 7.1 关键 UUID / 特征
- Service: `0000FFE0`
- Main Write / Notify: `0000FFE1`
- Aux / Settings: `0000FFE2`
- Handshake probe: `FFF1 / FFF2`

### 7.2 初始化序列
- 连接后读取 `FFF1/FFF2` 之一作为握手探测
- 通过 `FFE2` 发送 `01 02` 预热
- 通过 `FFE1` 发送 `AA 11 00 01` 读取参数
- 通过 `FFE1` 周期发送 `AA 13 FF 01 AA 13 00 01` 拉取实时数据

### 7.3 实时帧
- 实时命令字: `0x13`
- 帧长: `67` 字节
- 关键映射：
  - `Word[6] / 5.46` -> `rpm`
  - `Word[8] / 273.0666667` -> `voltage`
  - `Word[9]` -> `busCurrent`
  - `Word[10]` -> `phaseCurrent`
  - `Word[12] / 100` -> `motorTemp`
  - `Word[18] / 100` -> `controllerTemp`
  - `Word[22]` -> `faultCode`
  - `Word[23]` -> `ioStatus`

### 7.4 参数帧
- 读取响应命令字: `0x11`
- 写入命令字: `0x12`
- 当前已映射参数：
  - `Word[0]` 母线电流
  - `Word[1]` 相线电流
  - `Word[2]` 电机方向
  - `Word[9]` 欠压
  - `Word[10]` 过压
  - `Word[21]` 弱磁电流
  - `Word[23]` 反峰制动电流
  - `Word[30]` 传感器类型
  - `Word[31]` 霍尔序
  - `Word[32]` 相位角
  - `Word[34]` 极对数
  - `Word[60]` 蓝牙密码

## 8. 本地自动化资产

### 8.1 Scripts
- `.agents/scripts/bootstrap-mac.sh`: 新 Mac 一键安装 Java 17 / Android SDK / adb / sdkmanager，并写入 shell 环境变量
- `.agents/scripts/setup-release-signing.sh`: 创建或检查 release keystore，并把路径/密码写入钥匙串
- `.agents/scripts/preflight.sh`: 检查 Java / Android SDK / adb / gh
- `.agents/scripts/build-debug.sh`: 构建 Debug APK、校验、归档
- `.agents/scripts/compile-debug-kotlin.sh`: 编译 `:app:compileDebugKotlin`，支持自动路由到主编译机
- `.agents/scripts/build-dev-release.sh`: 使用 release 签名快速构建 `devRelease` APK，适合真机联调
- `.agents/scripts/build-fast-dev-release.sh`: 使用 release 签名快速构建 `fastDevRelease`，默认跳过 `lintVital`
- `.agents/scripts/build-release.sh`: 读取钥匙串中的 release 签名信息并构建已签名 APK
- `.agents/scripts/test-debug.sh`: 运行 Debug 单元测试
- `.agents/scripts/install-debug.sh`: 构建并安装到 adb 设备
- `.agents/scripts/install-dev-release.sh`: 构建并安装 `devRelease` 到 adb 设备
- `.agents/scripts/install-fast-dev-release.sh`: 构建并安装 `fastDevRelease` 到 adb 设备
- `.agents/scripts/logcat-habe.sh`: 抓取 BLE / 协议 / HUD 相关日志
- `.agents/scripts/sync-github.sh`: 构建、测试、提交、推送当前分支
- `.agents/scripts/create-user-owned-release-signing.sh`: 创建用户自有 release 签名

### 8.2 Workflows
- `.agents/workflows/bootstrap-mac.md`
- `.agents/workflows/release-signing.md`
- `.agents/workflows/user-owned-release-signing.md`
- `.agents/workflows/github-release.md` — GitHub Actions 自动发版流程（tag 推送/手动触发/签名 Secret 配置/产物验证）
- `.agents/workflows/preflight.md`
- `.agents/workflows/build-debug.md`
- `.agents/workflows/build-dev-release.md`
- `.agents/workflows/build-fast-dev-release.md`
- `.agents/workflows/build-performance.md`
- `.agents/workflows/overlay-dialog.md`
- `.agents/workflows/predictive-back.md`
- `.agents/workflows/test-debug.md`
- `.agents/workflows/install-debug.md`
- `.agents/workflows/install-dev-release.md`
- `.agents/workflows/install-fast-dev-release.md`
- `.agents/workflows/collect-logs.md`
- `.agents/workflows/zhike-diagnose.md`
- `.agents/workflows/telemetry-refactor.md`
- `.agents/workflows/sync-github.md`
- `.agents/workflows/google-drive-sync.md`
- `.agents/workflows/direction-stabilization.md`
- `.agents/workflows/mock-zhike-controller.md`

### 8.3 Skills
- `.agents/skills/smartdash-overlay-dialog/SKILL.md`: 统一约束设置页「关于」、应用更新、自定义 dialog、overlay、详情弹层的容器几何、图标适配、模糊、预测返回和 dismiss 动线
- `.agents/skills/ble-telemetry-estimation/SKILL.md`: 统一约束 `BLE` 遥测采样、累计能量、`SoC / range` 估算、`CSV` 导出与回放验证，强调"只由新鲜控制器样本驱动物理积分"
- `.agents/skills/direction-stabilization/SKILL.md`: 约束方向显示稳定化：速度分层冻结、GPS/传感器门控、死区、指数平滑、转向限速、文字滞回
- `.agents/skills/github-release/SKILL.md`: GitHub Actions 自动发版：tag 推送/手动触发、签名 Secret 准备、版本升级、产物验证
- `.agents/skills/material-design/SKILL.md`: Google Material Design 3 实战参考
- `.agents/skills/code-simplifier/SKILL.md`: 代码简化与清晰度提升
- `.agents/skills/wxapp-decompose/SKILL.md`: macOS 微信小程序包获取与反编译

## 9. 推荐日常流程

### 9.1 开发前
1. 新 Mac 首次装机先跑 `.agents/scripts/bootstrap-mac.sh`
2. 跑 `.agents/scripts/preflight.sh`
3. 确认 `gh auth status`、`adb version`、`JAVA_HOME`
4. 首次热身建议先执行一次 `.agents/scripts/compile-debug-kotlin.sh`，让主编译机上的 Gradle daemon 与 configuration cache 建立好

### 9.2 本地 Release 构建
1. 首次执行 `.agents/scripts/setup-release-signing.sh`
2. 确认 iCloud Drive 已同步 keystore 文件
3. 确认 `preflight.sh` 中 `RELEASE_SIGNING=ready`
4. 跑 `.agents/scripts/build-release.sh`

### 9.3 GitHub Actions 自动发版（推荐用于对外交付）

1. 更新 `app/build.gradle.kts` 中的 `versionCode`（递增）和 `versionName`
2. 提交变更：`git commit -m "bump: version X.Y.Z"`
3. 打 tag 并推送：`git tag vX.Y.Z && git push origin main vX.Y.Z`
4. GitHub Actions 自动构建、签名并发布到 [Releases](https://github.com/ShawnRn/SmartDash-by-Shawn-Rain/releases)

**签名 Secret 配置**（Repository Settings → Secrets → Actions）：

| Secret | 获取方式 |
|--------|----------|
| `HABE_RELEASE_STORE_FILE_BASE64` | `base64 -i habe-release.jks \| pbcopy` |
| `HABE_RELEASE_STORE_PASSWORD` | `security find-generic-password -a "$USER" -s habe.android.release.store.password -w` |
| `HABE_RELEASE_KEY_ALIAS` | `security find-generic-password -a "$USER" -s habe.android.release.key.alias -w` |
| `HABE_RELEASE_KEY_PASSWORD` | `security find-generic-password -a "$USER" -s habe.android.release.key.password -w` |

详见：`.agents/workflows/github-release.md`

### 9.4 给用户输出新版本交付或联调
1. 始终跑 `.agents/scripts/build-release.sh` 确保签名能够无缝覆盖安装
2. 跑 `.agents/scripts/test-debug.sh` 执行单元测试拦截回归问题
3. 将生成的 Release `.apk` 路径（位于 `.agents/artifacts/habe-release-xxxx.apk`）指引给用户
4. 若用户同时明确要求安装到手机，继续直接执行 adb 安装，不要停下来等待二次确认

### 9.5 本地真机高频联调
1. 优先跑 `.agents/scripts/install-dev-release.sh`
2. `devRelease` 继续使用 release 签名，可直接覆盖手机上的当前安装
3. 首次构建或刚改过 `build.gradle.kts` / `gradle.properties` 时，首包会较慢；第二次开始会明显变快
4. 若当前机器不是主编译机，脚本会自动通过 Tailscale SSH 到 `shawn-rains-macbook-pro` 编译，并把 APK 回传到当前机器后再执行本地 `adb install`
5. 只有在对外交付 APK、归档版本或最终验收时，再切回 `.agents/scripts/build-release.sh`
6. 用户只要明确表达"装到手机上"，就把安装视为默认动作，不要再额外确认
7. 若只是先产出 `devRelease` 包，不急着安装，优先使用低干扰 shell 方式：

```bash
cd "/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/SmartDash" \
  && export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
  && set -o pipefail \
  && .agents/scripts/build-dev-release.sh 2>&1 | tail -5
```

8. 对 Codex / 桌面代理尤其推荐上面这种"尾部输出"模式，能明显减少终端刷屏导致的系统卡顿感

### 9.7 本地个人极速迭代
1. 需要最快的真机覆盖安装时，优先跑 `.agents/scripts/install-fast-dev-release.sh`
2. `fastDevRelease` 会跳过 `lintVital`，适合 UI / 交互 / BLE 联调
3. 若脚本检测到 `project_dex_archive/<variant>` 重复类问题，会自动清理脏 dex 并重试一次
4. 对外交付前仍应至少再跑一次正式 `.agents/scripts/build-release.sh`

### 9.8 Google Drive 云同步
1. 设置 → 数据迁移 → Google Drive 云同步
2. 首次使用需登录 Google 账号并授权 `drive.appdata` 权限
3. OAuth 客户端 ID（`com.shawnrain.sdash`）：`8447150714-s2l193jktl69tpc4ja7o9q0squijoj7r.apps.googleusercontent.com`
4. 点击"上传备份"上传当前设备数据到 Drive
5. 另一台设备打开 app 时会自动检测更新（启动时一次），显示小圆点角标
6. 点击角标提示或"合并最新备份"按钮即可静默合并
7. 加密方案：使用 Google 账号 email 的 SHA-256 哈希派生密钥，同账号跨设备可解密
8. 旧设备 v1 备份（设备绑定密钥）仍可解密，新备份使用 v2 密码派生密钥

### 9.9 智科专项排查
1. app 设置页将日志级别切到 `VERBOSE`
2. 跑 `.agents/scripts/logcat-habe.sh --clear`
3. 复现"扫描 / 连接 / 调参 / 全 0"问题
4. 从设置页分享日志，并结合 adb logcat 一起分析

### 9.10 同步 GitHub
1. 确认 `.gitignore` 已排除本地参考与构建产物
2. 跑 `.agents/scripts/sync-github.sh "<commit message>"`

### 9.11 编译性能排查
1. 检查 `JAVA_HOME` 是否指向 JDK 17：`echo $JAVA_HOME`
2. 检查 Gradle Daemon 状态：`./gradlew --status`
3. 查看构建耗时报告：`./gradlew :app:assembleDebug --profile`
4. 清理 configuration cache：`rm -rf .gradle/configuration-cache/`
5. 若 `devRelease` 构建报 configuration cache 序列化错误，加 `--no-configuration-cache` 重试

## 10. Git 与版本控制约束
- 不要提交 `zhike_source/`、`habe_miniprogram/`、`tools/unveilr/`
- 不要提交 `app/build/`、`.agents/logs/`、`.agents/artifacts/`
- 不要提交任何签名材料：
  - `*.jks`
  - `*.keystore`
  - `*.p12`
  - `signing.properties`
  - `keystore.properties`
  - `.env*` 中的签名密码
- 可提交：
  - `AGENTS.md`
  - `.agents/scripts/`
  - `.agents/workflows/`
  - `app/src/main/` 下实际功能代码
  - `app/proguard-rules.pro`

## 11. 维护建议
- 后续若继续迁移智科调参项，优先扩展 `ZhikeSettings.rawWords` 的映射表，而不是新增零散硬编码
- 若继续增强日志，应优先往 `AppLogger` 聚合，不要散落成不可导出的 `Log.d`
- 若继续增强后台 `PiP` 仪表，保持只读与轻量，优先在 `MainActivity` 内复用现有遥测状态
- 后续新增二级页面、自定义 Dialog、全屏 overlay、页面内自绘 sheet 时，默认必须适配 `PredictiveBackHandler`，优先复用 `app/src/main/java/com/shawnrain/sdash/ui/navigation/PredictiveBackMotion.kt`
- 若使用 `ModalBottomSheet` 等平台组件且系统默认预测性返回动画不足，应补充应用内跟手缩放 / 位移预览，而不是回退为无预测性返回
- 后续新增任何弹窗、sheet、overlay 时，默认必须复用 `app/src/main/java/com/shawnrain/sdash/ui/navigation/DialogWindowEffects.kt`，为窗口背后铺设模糊并保持透明系统栏
- 后续新增或改造 overlay / dialog / about / update 弹层时，默认先遵循 `.agents/skills/smartdash-overlay-dialog/SKILL.md` 与 `.agents/workflows/overlay-dialog.md`，禁止再临时发明一套新的几何、图标适配或 dismiss 动线
- 设置页中的「关于」类入口默认放在设置列表最底部；品牌图标必须保持原始比例并做受控内边距适配，禁止粗暴塞进固定圆角框导致观感失衡
- 行程详情页点击参数卡片进入横屏全屏图表时，必须提供非线性进入/退出动画（建议 `FastOutSlowIn` 进入 + `FastOutLinearIn` 退出），并保证所有关闭路径（返回手势/遮罩点击/按钮/下拉）走同一条"从哪来回哪去"的回收动画
- 行程详情页概览卡片编辑交互约定为：长按任一卡片直接进入编辑并开始拖拽；正常态不显示"编辑卡片"按钮，仅在编辑态显示"完成/添加卡片"
- Google Drive 同步加密使用密码派生密钥（v2），禁止使用 Android KeyStore 设备绑定密钥（v1 已废弃），确保跨设备兼容
- 云同步 UI 状态切换必须使用 `AnimatedContent`，禁止硬切；成功/失败提示使用 Material Icon（`Icons.Default.Check` / `Icons.Default.Error`），禁止使用 emoji
- 速度过滤逻辑不得添加固定极速上限（`coerceIn(max)`），因项目支持多车型 profile；应使用变化率过滤（`speedDelta > maxAllowedDelta`）
- 方向显示调参优先通过 `DirectionStabilizer` 参数调整，不要修改 `HeadingTracker` 传感器底层逻辑
- `proguard-rules.pro` 更新后需验证 release 构建：跑 `.agents/scripts/build-release.sh` 并安装到真机测试
- 所有涉及 `VehicleProfile` 浮点字段的读写，必须经过 `finiteOr()` 或 `isFinite()` 守卫，防止 NaN 再次写入 DataStore
- `ProtocolParser._metrics` 保持 `SharedFlow`，不得改回 `StateFlow`，否则会导致里程/能耗再次为 0
