# Habe Dashboard - Android 原生迁移项目 (AGENTS.md)

## 1. 项目目标
`Habe Dashboard` 是对原微信小程序版 “Habe仪表” 的原生 Android 重构，目标是把高频骑行仪表、控制器协议、BMS、测速与调参能力迁到本地端，解决小程序在 BLE 并发、界面刷新、后台能力、权限与商业限制上的短板。

当前仓库定位：
- Android 原生骑行仪表主工程
- 以智科协议为第一优先级落地对象
- 以 Compose + Kotlin 为 UI 与业务主栈
- 以 `.agents/scripts` + `.agents/workflows` 作为本地自动化入口

## 2. 技术栈与构建基线
- `namespace` / `applicationId`: `com.shawnrain.habe`
- Android Gradle Plugin: `8.5.0`
- `compileSdk`: `35`
- `targetSdk`: `35`
- `minSdk`: `26`
- Kotlin JVM target: `17`
- UI: Jetpack Compose + Material 3
- 导航: `androidx.navigation:navigation-compose`
- 存储: `DataStore Preferences`
- BLE: 原生 `BluetoothGatt` / `BluetoothLeScanner`

构建注意：
- 当前 AGP `8.5.0` 对 `compileSdk = 35` 会有官方测试范围警告，但不阻塞 `assembleDebug`
- Java 17 是当前稳定构建前提
- Gradle 已启用 daemon / parallel / build cache / configuration cache，用于缩短本地迭代构建时间

推荐环境变量：
- `ANDROID_HOME=/Users/shawnrain/android-sdk`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
- `ANDROID_SDK_ROOT=/Users/shawnrain/android-sdk`

Release 签名约定：
- keystore 文件默认保存在 iCloud Drive：
  - `~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Secure/habe_android/signing/habe-release.jks`
- release 构建密码默认保存在用户钥匙串：
  - `habe.android.release.store.path`
  - `habe.android.release.store.password`
  - `habe.android.release.key.alias`
  - `habe.android.release.key.password`
- 若开启 iCloud Drive 与 iCloud Keychain，可在多台 Mac 间复用同一套 release 签名
- 若用户明确要求“本人掌握全部签名关键信息”，允许改用：
  - `.agents/scripts/create-user-owned-release-signing.sh`
  - 输出目录默认位于 `~/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/habe-android-signing-时间戳/`
  - 目录内会包含 keystore、明文签名信息、以及恢复 Keychain 的脚本
  - 这会生成一套新的签名身份，无法直接覆盖安装旧签名版本
- 不要把任何 keystore、证书、签名配置文件、密码文件提交到 GitHub
- 签名相关信息只允许存放在：
  - iCloud Drive 中的 keystore 文件
- macOS / iCloud Keychain 中的密码项
- `.agents/scripts/preflight.sh` 会提示仓库内是否存在签名文件
- `.agents/scripts/sync-github.sh` 会阻止任何签名文件被提交或推送

> **⚠️ 重要强约束 (AI Agent 必读)**
> 当用户要求“输出/编译一个 APK 给我测试”或“提供最新安装包”时，**必须**调用 `.agents/scripts/build-release.sh` 生成 Release 签名包！
> 绝对不要默认通过 `build-debug.sh` 输出给用户，因为用户的物理真机已安装了 Release 签名版本，使用 Debug 签名会导致覆盖安装失败或需要清除数据！
>
> `devRelease` 仅用于本地高频联调提速：
> - `.agents/scripts/build-dev-release.sh`
> - `.agents/scripts/install-dev-release.sh`
> - 仍使用 release 签名，可覆盖用户现有安装
> - 但当用户明确要求“最新测试安装包/交付 APK”时，仍必须使用正式 `build-release.sh`
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
- 最后跑 `./gradlew :app:assembleDebug --console plain`

## 3. 当前代码结构
核心目录：
- `app/src/main/java/com/shawnrain/habe/MainActivity.kt`: 主导航与页面装配
- `app/src/main/java/com/shawnrain/habe/MainViewModel.kt`: 应用级状态、BLE 数据汇总、设置与调参动作
- `app/src/main/java/com/shawnrain/habe/ble/`: BLE 引擎、协议路由、控制器解析
- `app/src/main/java/com/shawnrain/habe/data/`: DataStore、GPS、校准、骑行会话
- `app/src/main/java/com/shawnrain/habe/ui/`: 仪表、连接、设置、BMS、测速界面
- `app/src/main/java/com/shawnrain/habe/debug/`: 应用内日志系统
- `app/src/main/java/com/shawnrain/habe/overlay/`: 后台悬浮仪表
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
- 横屏模式切换为专注布局，仅显示顶部 3 个核心指标：
  - 速度
  - 功率
  - 平均能耗
- 仪表页进入后保持亮屏
- 全局圆角已改为连续曲率的贝塞尔 / superellipse 风格
- Dashboard 横屏时隐藏 app 自身底部导航，但不隐藏系统状态栏和导航栏

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

### 4.4 GPS 与速度校准
- 已实现基于手机 GPS 的轮径校准会话
- 校准时持续比较：
  - GPS 速度 / 距离
  - 控制器 RPM 推算速度 / 距离
- 可输出推荐轮径周长并一键应用

### 4.5 日志与后台浮窗
- 已实现应用内日志系统 `AppLogger`
- 设置页可配置日志级别：
  - `VERBOSE`
  - `DEBUG`
  - `INFO`
  - `WARN`
  - `ERROR`
- 设置页可导出 / 分享日志
- 已实现后台自动悬浮仪表
- 悬浮窗内容固定为顶部 3 个指标：
  - 速度
  - 功率
  - 平均能耗
- 悬浮窗当前支持：
  - 点击返回 app 仪表页
  - 拖动位置
  - 更早随回桌面出现
  - 首次显示短暂触摸保护，避免 Home 手势尾巴误触
  - 速度主值显示为整数，单位 `km/h` 与数字右侧底部对齐
- app 切后台时可自动打开；回前台时自动关闭

### 4.6 返回与导航
- `BMS` 与 `智科设置` 二级页面已接入 Compose `PredictiveBackHandler`
- 返回手势过程中支持页面跟手缩放 / 位移预览
- 顶层 Tab 切换与二级页返回动画分离：
  - 顶层 Tab 使用轻量级切页动效
  - 二级页返回保留单独的返回过渡

## 5. 当前迁移状态
- [x] Android 原生基础架构
- [x] Compose 仪表主界面
- [x] BLE 扫描、连接、通知、基础协议识别
- [x] 智科协议实时解析基础版
- [x] 智科参数读取 / 基础写入 / 密码校验
- [x] GPS 轮径校准
- [x] 应用内日志与分享
- [x] 后台悬浮三指标 HUD
- [ ] 智科调参项与小程序完全对齐
- [ ] 智科实车联调闭环验证完全稳定
- [ ] BMS 多品牌迁移完成
- [ ] 更完整的测速 / 制动 / 海报能力迁移
- [ ] 本地单元测试覆盖

## 6. 已知问题与风险
- 智科“可连接但数据全 0”问题仍需依赖真机日志继续定位
- 智科调参页目前只是基础子集，尚未达到小程序同等级别的丰富参数矩阵
- 当前仓库本地单元测试仍为 `NO-SOURCE`
- 后台悬浮窗依赖系统悬浮权限与前台服务通知
- `devRelease` 首次构建或 Gradle 脚本变更后，configuration cache 需要重建，首包时间会明显高于后续热构建
- 当前 AGP 8.5.0 在 `devRelease` / `release` 上偶发 dex 中间产物重复问题；清理对应 `project_dex_archive/<variant>` 后可恢复
- `LocalLifecycleOwner` 目前使用 Compose 旧导入，会给出弃用警告，但不影响构建

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
- `.agents/scripts/build-dev-release.sh`: 使用 release 签名快速构建 `devRelease` APK，适合真机联调
- `.agents/scripts/build-fast-dev-release.sh`: 使用 release 签名快速构建 `fastDevRelease`，默认跳过 `lintVital`
- `.agents/scripts/build-release.sh`: 读取钥匙串中的 release 签名信息并构建已签名 APK
- `.agents/scripts/test-debug.sh`: 运行 Debug 单元测试
- `.agents/scripts/install-debug.sh`: 构建并安装到 adb 设备
- `.agents/scripts/install-dev-release.sh`: 构建并安装 `devRelease` 到 adb 设备
- `.agents/scripts/install-fast-dev-release.sh`: 构建并安装 `fastDevRelease` 到 adb 设备
- `.agents/scripts/logcat-habe.sh`: 抓取 BLE / 协议 / HUD 相关日志
- `.agents/scripts/sync-github.sh`: 构建、测试、提交、推送当前分支

### 8.2 Workflows
- `.agents/workflows/bootstrap-mac.md`
- `.agents/workflows/release-signing.md`
- `.agents/workflows/preflight.md`
- `.agents/workflows/build-debug.md`
- `.agents/workflows/build-dev-release.md`
- `.agents/workflows/build-fast-dev-release.md`
- `.agents/workflows/test-debug.md`
- `.agents/workflows/install-debug.md`
- `.agents/workflows/install-dev-release.md`
- `.agents/workflows/install-fast-dev-release.md`
- `.agents/workflows/collect-logs.md`
- `.agents/workflows/zhike-diagnose.md`
- `.agents/workflows/sync-github.md`

## 9. 推荐日常流程

### 9.1 开发前
1. 新 Mac 首次装机先跑 `.agents/scripts/bootstrap-mac.sh`
2. 跑 `.agents/scripts/preflight.sh`
3. 确认 `gh auth status`、`adb version`、`JAVA_HOME`
4. 首次热身建议先执行一次 `./gradlew :app:compileDebugKotlin --console plain`，让 Gradle daemon 与 configuration cache 建立好

### 9.2 Release 构建前
1. 首次执行 `.agents/scripts/setup-release-signing.sh`
2. 确认 iCloud Drive 已同步 keystore 文件
3. 确认 `preflight.sh` 中 `RELEASE_SIGNING=ready`
4. 跑 `.agents/scripts/build-release.sh`

### 9.3 给用户输出新版本交付或联调
1. 始终跑 `.agents/scripts/build-release.sh` 确保签名能够无缝覆盖安装
2. 跑 `.agents/scripts/test-debug.sh` 执行单元测试拦截回归问题
3. 将生成的 Release `.apk` 路径（位于 `.agents/artifacts/habe-release-xxxx.apk`）指引给用户

### 9.4 本地真机高频联调
1. 优先跑 `.agents/scripts/install-dev-release.sh`
2. `devRelease` 继续使用 release 签名，可直接覆盖手机上的当前安装
3. 首次构建或刚改过 `build.gradle.kts` / `gradle.properties` 时，首包会较慢；第二次开始会明显变快
4. 只有在对外交付 APK、归档版本或最终验收时，再切回 `.agents/scripts/build-release.sh`

### 9.5 本地个人极速迭代
1. 需要最快的真机覆盖安装时，优先跑 `.agents/scripts/install-fast-dev-release.sh`
2. `fastDevRelease` 会跳过 `lintVital`，适合 UI / 交互 / BLE 联调
3. 若脚本检测到 `project_dex_archive/<variant>` 重复类问题，会自动清理脏 dex 并重试一次
4. 对外交付前仍应至少再跑一次正式 `.agents/scripts/build-release.sh`

### 9.6 智科专项排查
1. app 设置页将日志级别切到 `VERBOSE`
2. 跑 `.agents/scripts/logcat-habe.sh --clear`
3. 复现“扫描 / 连接 / 调参 / 全 0”问题
4. 从设置页分享日志，并结合 adb logcat 一起分析

### 9.7 同步 GitHub
1. 确认 `.gitignore` 已排除本地参考与构建产物
2. 跑 `.agents/scripts/sync-github.sh "<commit message>"`

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

## 11. 维护建议
- 后续若继续迁移智科调参项，优先扩展 `ZhikeSettings.rawWords` 的映射表，而不是新增零散硬编码
- 若继续增强日志，应优先往 `AppLogger` 聚合，不要散落成不可导出的 `Log.d`
- 若继续增强后台悬浮窗，保持只读与轻量，避免在 Service 中重复业务计算
