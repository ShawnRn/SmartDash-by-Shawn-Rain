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

推荐环境变量：
- `ANDROID_HOME=/Users/shawnrain/android-sdk`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`

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
- app 切后台时可自动打开；回前台时自动关闭

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
- `.agents/scripts/preflight.sh`: 检查 Java / Android SDK / adb / gh
- `.agents/scripts/build-debug.sh`: 构建 Debug APK、校验、归档
- `.agents/scripts/test-debug.sh`: 运行 Debug 单元测试
- `.agents/scripts/install-debug.sh`: 构建并安装到 adb 设备
- `.agents/scripts/logcat-habe.sh`: 抓取 BLE / 协议 / HUD 相关日志
- `.agents/scripts/sync-github.sh`: 构建、测试、提交、推送当前分支

### 8.2 Workflows
- `.agents/workflows/preflight.md`
- `.agents/workflows/build-debug.md`
- `.agents/workflows/test-debug.md`
- `.agents/workflows/install-debug.md`
- `.agents/workflows/collect-logs.md`
- `.agents/workflows/zhike-diagnose.md`
- `.agents/workflows/sync-github.md`

## 9. 推荐日常流程

### 9.1 开发前
1. 跑 `.agents/scripts/preflight.sh`
2. 确认 `gh auth status`、`adb version`、`JAVA_HOME`

### 9.2 提交前
1. 跑 `.agents/scripts/build-debug.sh`
2. 跑 `.agents/scripts/test-debug.sh`
3. 如涉及真机联调，再跑 `.agents/scripts/install-debug.sh`

### 9.3 智科专项排查
1. app 设置页将日志级别切到 `VERBOSE`
2. 跑 `.agents/scripts/logcat-habe.sh --clear`
3. 复现“扫描 / 连接 / 调参 / 全 0”问题
4. 从设置页分享日志，并结合 adb logcat 一起分析

### 9.4 同步 GitHub
1. 确认 `.gitignore` 已排除本地参考与构建产物
2. 跑 `.agents/scripts/sync-github.sh "<commit message>"`

## 10. Git 与版本控制约束
- 不要提交 `zhike_source/`、`habe_miniprogram/`、`tools/unveilr/`
- 不要提交 `app/build/`、`.agents/logs/`、`.agents/artifacts/`
- 可提交：
  - `AGENTS.md`
  - `.agents/scripts/`
  - `.agents/workflows/`
  - `app/src/main/` 下实际功能代码

## 11. 维护建议
- 后续若继续迁移智科调参项，优先扩展 `ZhikeSettings.rawWords` 的映射表，而不是新增零散硬编码
- 若继续增强日志，应优先往 `AppLogger` 聚合，不要散落成不可导出的 `Log.d`
- 若继续增强后台悬浮窗，保持只读与轻量，避免在 Service 中重复业务计算
