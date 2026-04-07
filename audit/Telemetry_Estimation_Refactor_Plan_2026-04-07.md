# SmartDash 遥测估算链路改进文档与执行清单

更新时间：2026-04-07

适用范围：

- `BLE` 控制器实时遥测
- `SoC / range / Wh / Wh/km / voltage sag` 算法
- 仪表页、行程记录、`CSV` 导出
- `.agents` 内部技能与工作流

---

# 1. 背景结论

基于当前项目代码与最近一次骑行 `CSV`，可以把问题分成两类：

## 1.1 目前比较可靠的部分

- 控制器基础协议解析链路已经基本成型
- `powerW ≈ voltage × busCurrent` 的关系总体自洽
- `speed / distance / voltage / current` 这些原始量大体可用
- `BLE` 写入、协议识别、智科实时帧基础映射已具备继续演进的条件

## 1.2 当前最核心的结构性问题

### 问题 A：物理积分挂在 UI 聚合流上

`MainViewModel.metrics` 当前由多个流 `combine(...)` 后统一进入 `calculateVehicleMetrics(...)`。但这个函数内部又直接做了：

- `_rideEnergyWh += ...`
- `_rideRecoveredEnergyWh += ...`
- `_ridePeakRegenKw = ...`
- `_maxSpeed / _totalSpeedSum / _speedSamples` 更新

这意味着：

- 新控制器样本会触发一次
- 新 `GPS` 速度也会再触发一次
- 新 `BMS` 样本也会再触发一次
- 设置项变化也可能再触发一次

对 `BLE` 项目来说，这是最危险的设计点，因为它会把**界面刷新**误当成**物理样本到达**。

### 问题 B：`SoC` 与容量档案耦合过深

当前 `RideEnergyEstimator` 会把 `batteryCapacityAh`、`fallbackSocPercent`、`OCV` 与 `Ah` 积分混合使用。若车辆档案本身写的是 `48 V 13s 50 Ah`，那么 `SoC` 很可能不是独立测量值，而是带强先验的模型输出。

### 问题 C：`range` 与能量口径未完全统一

当前代码中至少同时存在这些口径：

- `_rideEnergyWh`
- `_rideRecoveredEnergyWh`
- `consumedPositiveEnergyWh`
- `remainingEnergyWh`
- `avgEfficiencyWhKm`
- `estimatedRangeKm`

如果这些字段分属不同口径，最终就会出现：

- `SoC` 看起来很高
- `remainingEnergyWh` 看起来很多
- 但 `range` 又偏低或偏飘

### 问题 D：`voltageSag` 基线过于脆弱

当前只要 `低流 + 低速` 就重写 `_restingVoltageBaseline`，很容易被：

- 短暂收油
- 下坡滑行
- 回生切换
- `BLE` 抖动

污染基准，导致压降值可视化上看似合理，实际却难以作为健康度判断依据。

---

# 2. 总体重构目标

目标不是简单修几个公式，而是把当前实现升级为一条清晰的估算链：

```text
BLE frame
  -> parser
  -> TelemetryStreamProcessor
  -> TelemetrySample
  -> RideAccumulator
  -> BatteryStateEstimator
  -> RangeEstimator
  -> DashboardProjector
  -> UI / RideHistory / CSV
```

其中：

- **parser**：只负责协议正确解析
- **TelemetryStreamProcessor**：只负责把 `BLE` 帧变成“可用于积分的样本”
- **RideAccumulator**：只负责累计量
- **BatteryStateEstimator**：只负责 `SoC / remainingEnergy / 内阻`
- **RangeEstimator**：只负责预测续航
- **DashboardProjector**：只负责输出 UI 可显示的值

---

# 3. 分层改进方案

## 3.1 第 0 层：字段语义冻结

在动代码前，先把以下字段的定义写死，不要一边改一边猜：

| 字段 | 必须定义清楚的问题 |
|---|---|
| `totalEnergyWh` | 是毛放电、净放电，还是仅本次骑行驱动能量 |
| `recoveredEnergyWh` | 是否只累计回生回收到电池的能量 |
| `avgEfficiencyWhKm` | 分子是毛能量还是净能量 |
| `estimatedRangeKm` | 分子是 `remainingEnergyWh` 还是其他派生量 |
| `voltageSag` | 参考基准电压怎么来 |
| `soc` | 内部估算值还是展示值 |

### 建议动作

- 在 `VehicleMetrics` 和 `RideHistorySample` 字段定义旁边补注释
- 在 `CSV` 表头导出文档里注明口径
- 在本文件第 `8` 节的清单中把这些作为硬性验收项

---

## 3.2 第 1 层：统一控制器样本 `TelemetrySample`

## 目标

把「原始控制器帧」和「上层估算样本」隔离开。

## 建议新增文件

- `app/src/main/java/com/shawnrain/habe/data/telemetry/TelemetrySample.kt`
- `app/src/main/java/com/shawnrain/habe/data/telemetry/SampleQuality.kt`
- `app/src/main/java/com/shawnrain/habe/data/telemetry/TelemetryStreamProcessor.kt`

## 推荐数据结构

```kotlin
enum class SampleQuality {
    GOOD,
    DUPLICATE,
    TOO_DENSE,
    STALE,
    OUTLIER,
    GAP_RESET
}

data class TelemetrySample(
    val timestampMs: Long,
    val sourceFrameId: Long? = null,
    val voltageV: Float,
    val busCurrentA: Float,
    val phaseCurrentA: Float,
    val rpm: Float,
    val controllerSpeedKmH: Float,
    val motorTempC: Float,
    val controllerTempC: Float,
    val braking: Boolean,
    val cruise: Boolean,
    val quality: SampleQuality,
    val dtMs: Long,
    val allowIntegration: Boolean,
    val allowLearning: Boolean
)
```

## 处理规则

### 去重

以下情况直接标记为 `DUPLICATE` 或 `TOO_DENSE`：

- `timestampMs <= lastTimestampMs`
- `dtMs < 50`
- 关键字段完全一致且短时间内重复到达

### 断流

以下情况标记为 `GAP_RESET`：

- `dtMs > 3000`

断流后：

- 允许更新 UI 当前值
- **但不要**把这段长间隔用于 `Wh / Ah / distance` 积分

### 异常值

出现下列情况时标 `OUTLIER`：

- 电压、速度、转速、电流超物理范围
- `power` 与 `V × I` 显著矛盾
- 速度跳变超过合理门槛

---

## 3.3 第 2 层：把累计量从 `MainViewModel.calculateVehicleMetrics(...)` 中剥离

## 当前问题

`calculateVehicleMetrics(...)` 同时承担：

- 读取多路流
- 计算当前显示值
- 更新累计能量
- 更新峰值
- 更新平均速度统计

职责过重，而且容易重复执行。

## 目标拆分

### A. `onNewControllerTelemetrySample(sample)`

只负责：

- `Wh` 积分
- `regen Wh` 积分
- 峰值功率 / 峰值回收 / 最高温
- 距离、时间、均速统计

### B. `calculateVehicleMetrics(...)`

只负责：

- 读当前状态
- 融合 `BMS / GPS / 设置项`
- 输出 `VehicleMetrics`

### 具体改法

把这些逻辑从 `calculateVehicleMetrics(...)` 移出：

- `_rideEnergyWh += ...`
- `_rideRecoveredEnergyWh += ...`
- `_ridePeakRegenKw = ...`
- `_rideMaxControllerTemp = ...`
- `_maxSpeed / _totalSpeedSum / _speedSamples` 更新

并迁移到专门的累计器，例如：

- `RideAccumulator.kt`

---

## 3.4 第 3 层：重写 `RideAccumulator`

## 建议新增文件

- `app/src/main/java/com/shawnrain/habe/data/telemetry/RideAccumulator.kt`
- `app/src/main/java/com/shawnrain/habe/data/telemetry/RideAccumulatorState.kt`

## 最低职责

- `tractionEnergyWh`
- `regenEnergyWh`
- `netBatteryEnergyWh`
- `peakDrivePowerKw`
- `peakRegenPowerKw`
- `maxControllerTempC`
- `movingSpeedSum`
- `movingSamples`
- `controllerDistanceMeters` 或与外部里程源对齐后的累计值

## 建议积分规则

```kotlin
if (!sample.allowIntegration) return

val powerW = sample.voltageV * sample.busCurrentA
val dtHours = sample.dtMs / 3600000.0

if (powerW >= 0f) {
    tractionEnergyWh += powerW * dtHours
} else {
    regenEnergyWh += -powerW * dtHours
}

netBatteryEnergyWh = tractionEnergyWh - regenEnergyWh
```

## 注意

- `GPS` 数据只能参与显示或辅助校准，不应直接驱动控制器能量积分
- 若要累计距离，优先用同一来源保持口径一致；如控制器速度不稳，则通过单独策略切到 `GPS`，但不能隐式混用

---

## 3.5 第 4 层：重写 `BatteryStateEstimator`

当前 `RideEnergyEstimator` 职责太多，建议拆成：

- `BatteryStateEstimator`
- `RangeEstimator`

## 建议新增文件

- `app/src/main/java/com/shawnrain/habe/data/estimation/BatteryStateEstimator.kt`
- `app/src/main/java/com/shawnrain/habe/data/estimation/BatteryEstimate.kt`

## 目标输出

```kotlin
data class BatteryEstimate(
    val socByAhPercent: Float,
    val socByOcvPercent: Float,
    val fusedSocPercent: Float,
    val displaySocPercent: Float,
    val remainingEnergyWh: Float,
    val learnedInternalResistanceOhm: Float,
    val socConfidence: Float
)
```

## 核心策略

### 行驶中

以电流 / 能量积分为主：

- 优点：连续、动态、不会被瞬时回弹带乱
- 风险：长时间漂移

### 静置时

只在满足以下条件时，才允许 `OCV` 纠偏：

- `abs(current) < 1 A`
- `speed < 0.5 km/h`
- `rpm` 很低或接近 `0`
- 持续 `30~60 s`
- 最近一段电压波动很小，例如 `stddev < 0.15 V`

### 展示值

`displaySocPercent` 必须单独限速：

- 放电时允许较快下降
- 回升时更慢
- 长时间静置后才允许明显上修

## 重要修正

不要把 `fallbackSocPercent` 当作强真值。它只能是：

- 初始化参考
- `BMS SoC` 存在时的弱参考
- 控制器电压估算时的弱参考

---

## 3.6 第 5 层：重写 `RangeEstimator`

## 建议新增文件

- `app/src/main/java/com/shawnrain/habe/data/estimation/RangeEstimator.kt`
- `app/src/main/java/com/shawnrain/habe/data/estimation/RangeEstimate.kt`

## 目标输出

```kotlin
data class RangeEstimate(
    val predictedEfficiencyWhKm: Float,
    val rawRangeKm: Float,
    val displayRangeKm: Float,
    val rangeConfidence: Float
)
```

## 正确公式

```text
estimatedRangeKm = remainingUsableEnergyWh / predictedEfficiencyWhKm
```

这里的：

- `remainingUsableEnergyWh` 必须与 `SoC` 来自同一模型
- `predictedEfficiencyWhKm` 必须是单独定义好的预测能耗，而不是随便抓一个当前值

## 推荐预测能耗来源

至少维护 3 层：

- `shortWindowEfficiencyWhKm`
- `midWindowEfficiencyWhKm`
- `tripAverageEfficiencyWhKm`

再根据工况加权：

- 起步：冻结或极低权重更新
- 巡航：提高短窗权重
- 激烈加速：降低短窗权重，更多参考中窗或全程平均
- 低速挪车：不更新或弱更新

## 为什么不用当前的 `1 km` 台阶冻结

它虽然能「稳」，但副作用太大：

- 短途几乎不更新
- 用户会觉得卡住
- `range` 与当前状态反馈脱节

更好的方案是：

- 起步冻结 `20~30 s`
- `EMA` 平滑
- 每秒最大变化量限制，例如 `1.5 km/s`
- 当 `rangeConfidence` 低时，弱化显示或显示 `≈`

---

## 3.7 第 6 层：重做 `voltageSag`

## 当前问题

现在的算法：

- 低流低速就把当前电压记为 `rest baseline`
- 随后 `baseline - voltage = sag`

这会使基准被频繁重写。

## 建议拆成两个量

### `instantSagV`

用于 UI 显示：

- 基线慢速上移
- 不要瞬间重置
- 支持一定程度追踪长期电压平台变化

### `normalizedSagPerAmp`

用于分析与健康判断：

```text
normalizedSagPerAmp = instantSagV / max(absCurrentA, 1)
```

未来可以进一步做：

- 分 `SoC` 区间记录
- 分温度区间记录
- 分车型档案记录

---

## 3.8 第 7 层：加入 `confidence` 与 `stream state`

这一步对产品观感提升很大。

## 建议新增枚举

```kotlin
enum class StreamState {
    LIVE,
    JITTERY,
    PAUSED,
    RECOVERING
}
```

## 建议输出字段

- `sampleQuality`
- `streamState`
- `socConfidence`
- `rangeConfidence`

## 使用规则

### `socConfidence` 低的场景

- 刚连接
- 刚恢复断流
- 长时间无静置校准
- 档案容量只是估值

### `rangeConfidence` 低的场景

- 起步前 `300 m`
- 低速挪车
- 激烈加减速
- `BLE` 抖动
- 断流恢复后短时间内

UI 可以做：

- 数值前加 `≈`
- 小字提示 `低置信度`
- 图表点位颜色变淡

---

# 4. 建议的落地文件改动清单

## 必改现有文件

### `app/src/main/java/com/shawnrain/habe/MainViewModel.kt`

### 改什么

- 从 `calculateVehicleMetrics(...)` 中移除所有累计副作用
- 新增对 `TelemetryStreamProcessor / RideAccumulator / BatteryStateEstimator / RangeEstimator` 的持有与调度
- `metrics` 组合流只负责投影，不做物理积分

### 验收

- 调整 `GPS / BMS / 设置项` 时，不再让 `Wh` 变化

---

### `app/src/main/java/com/shawnrain/habe/data/RideEnergyEstimator.kt`

### 改什么

- 建议拆分为 `BatteryStateEstimator.kt` + `RangeEstimator.kt`
- 若暂时不拆文件，也至少拆方法与状态对象
- `consumedPositiveEnergyWh` 改成显式区分 `traction / regen / net`
- `updateLearnedResistance(...)` 只在干净阶跃段学习

### 验收

- `remainingEnergyWh`、`SoC`、`estimatedRangeKm` 三者口径统一

---

### `app/src/main/java/com/shawnrain/habe/ble/ProtocolParser.kt`

### 改什么

- 尽量保留现有协议输出
- 但为上层样本链提供“新鲜控制器帧”的稳定入口
- 如有可能，补一个帧序号或 `frameTimestamp`

---

### `app/src/main/java/com/shawnrain/habe/ble/BleManager.kt`

### 改什么

- 保留轮询，但明确承认到包时间不严格恒定
- 新增流状态辅助信息，例如最后一次有效实时帧时间

---

### `app/src/main/java/com/shawnrain/habe/data/history/RideHistoryModels.kt`

### 改什么

- 补充更清晰的能量口径字段注释
- 如决定升级导出格式，可考虑增补：
  - `tractionEnergyWh`
  - `regenEnergyWh`
  - `netEnergyWh`
  - `rangeConfidence`
  - `socConfidence`

---

## 建议新增文件

- `app/src/main/java/com/shawnrain/habe/data/telemetry/TelemetrySample.kt`
- `app/src/main/java/com/shawnrain/habe/data/telemetry/SampleQuality.kt`
- `app/src/main/java/com/shawnrain/habe/data/telemetry/TelemetryStreamProcessor.kt`
- `app/src/main/java/com/shawnrain/habe/data/telemetry/RideAccumulator.kt`
- `app/src/main/java/com/shawnrain/habe/data/telemetry/RideAccumulatorState.kt`
- `app/src/main/java/com/shawnrain/habe/data/estimation/BatteryStateEstimator.kt`
- `app/src/main/java/com/shawnrain/habe/data/estimation/BatteryEstimate.kt`
- `app/src/main/java/com/shawnrain/habe/data/estimation/RangeEstimator.kt`
- `app/src/main/java/com/shawnrain/habe/data/estimation/RangeEstimate.kt`
- `app/src/main/java/com/shawnrain/habe/data/replay/TelemetryReplayRunner.kt`

---

# 5. 分阶段执行清单

## Phase 1：止血

目标：先阻止重复积分。

### 任务

- [ ] 把 `_rideEnergyWh / _rideRecoveredEnergyWh` 从 `calculateVehicleMetrics(...)` 中迁出
- [ ] 引入 `TelemetrySample`
- [ ] 只允许新控制器样本驱动积分
- [ ] 增加 `dt` 合法性判断
- [ ] `dt > 3000 ms` 时断段，不跨段积分

### 验收

- [ ] 打开 app，不动控制器，仅 `GPS` 更新时 `Wh` 不变
- [ ] `BMS` 有新数据时 `Wh` 不变
- [ ] 设置项变化时 `Wh` 不变

### 可以直接拿去 vibe 的提示词

> 请重构 `MainViewModel` 的实时遥测处理链。目标是把 `_rideEnergyWh`、`_rideRecoveredEnergyWh`、`_ridePeakRegenKw`、`_rideMaxControllerTemp` 等累计状态从 `calculateVehicleMetrics(...)` 中迁出，改为仅由新的控制器遥测样本触发。新增 `TelemetrySample`、`SampleQuality` 与 `TelemetryStreamProcessor`，对 `dt < 50 ms`、`dt > 3000 ms`、重复帧、异常值进行处理。要求 `metrics = combine(...)` 只做 UI 投影，不再承担物理积分副作用。

---

## Phase 2：统一能量口径

### 任务

- [ ] 明确 `traction / regen / net` 三本账
- [ ] `avgEfficiencyWhKm` 明确采用哪一本账
- [ ] `CSV` 导出补充或修正文档说明
- [ ] `RideHistory` 概览与仪表页口径对齐

### 验收

- [ ] `CSV totalEnergyWh / recoveredEnergyWh / avgEfficiencyWhKm` 可互相解释
- [ ] 行程详情概览与仪表页最终值一致

### vibe 提示词

> 请统一 SmartDash 的能量口径。新增 `tractionEnergyWh`、`regenEnergyWh`、`netBatteryEnergyWh`，明确 `avgEfficiencyWhKm` 与 `estimatedRangeKm` 各自使用的口径，并同步更新 `RideHistoryModels`、`CSV` 导出与 `VehicleMetrics` 注释。要求 UI、历史记录与导出字段完全同口径。

---

## Phase 3：重做 `BatteryStateEstimator`

### 任务

- [ ] 将 `RideEnergyEstimator` 拆分或重构为 `BatteryStateEstimator`
- [ ] 静置校准条件变严格
- [ ] `displaySocPercent` 与内部估算值分离
- [ ] 增加 `socConfidence`

### 验收

- [ ] 短途骑行中 `SoC` 不再频繁上下跳
- [ ] 静置后 `SoC` 可以缓慢回修，但不会瞬间飙升
- [ ] 断流恢复后 `SoC` 不会大跳

### vibe 提示词

> 请重写 `RideEnergyEstimator` 的 `SoC` 估算部分，拆成 `BatteryStateEstimator`。要求同时输出 `socByAhPercent`、`socByOcvPercent`、`fusedSocPercent`、`displaySocPercent` 与 `socConfidence`。只在 `abs(current) < 1 A`、`speed < 0.5 km/h`、`rpm` 很低、持续 `30~60 s` 且电压波动足够小的情况下，才允许 `OCV` 显著修正 `SoC`。显示值需要单独限速、防抖。

---

## Phase 4：重做 `RangeEstimator`

### 任务

- [ ] 起步冻结窗口
- [ ] 引入短窗 / 中窗 / 全程平均 3 层能耗
- [ ] `rangeConfidence`
- [ ] 移除 `1 km` 台阶式刷新
- [ ] 改为 `EMA + 限速更新`

### 验收

- [ ] 起步前 `20~30 s` 续航不乱跳
- [ ] 巡航时续航平稳收敛
- [ ] 激烈加减速时不会一秒大起大落

### vibe 提示词

> 请重写 SmartDash 的 `estimatedRangeKm` 估算逻辑，新增 `RangeEstimator`。要求 `estimatedRangeKm = remainingUsableEnergyWh / predictedEfficiencyWhKm`，其中预测能耗来自短窗、中窗和全程平均的动态加权。删除按 `1 km` 才更新显示值的策略，改为起步冻结、`EMA` 平滑、每秒最大变化量限制，并输出 `rangeConfidence`。

---

## Phase 5：回放与回归测试

### 任务

- [ ] 新增 `TelemetryReplayRunner`
- [ ] 可读取历史 `CSV`
- [ ] 输出重算后的 `SoC / range / Wh / sag`
- [ ] 准备 5 类测试日志样本

### 验收

- [ ] 算法更新前后可对比结果
- [ ] 可快速验证某次提交是否把累计逻辑搞坏

### vibe 提示词

> 请为 SmartDash 新增一个离线回放器 `TelemetryReplayRunner`，能够按时间顺序重放历史 `CSV` 样本，把样本喂给新的 `TelemetryStreamProcessor`、`RideAccumulator`、`BatteryStateEstimator` 和 `RangeEstimator`。输出总能耗、回收能量、平均能耗、`SoC`、续航与压降统计，用于回归测试。

---

# 6. `AGENTS / .agents` 更新建议

## 6.1 新增 skill

已新增：

- `.agents/skills/ble-telemetry-estimation/SKILL.md`

用途：

- 约束所有 `BLE` 遥测、估算、导出、回放相关改动必须遵循样本驱动原则

## 6.2 新增 workflow

已新增：

- `.agents/workflows/telemetry-refactor.md`

用途：

- 给后续 agent 或 `vibe coding` 过程一个固定执行顺序

## 6.3 推荐你后续再补的内容

- 在 `.agents/scripts/` 下增加一个简单的 replay 脚本入口
- 在 `.agents/examples/` 下增加一个最小测试 `CSV`
- 在 `audit/` 下持续追加每轮改动后的对比报告

---

# 7. 对当前代码的具体建议摘要

## `MainViewModel.kt`

### 现在最需要改

- 让 `calculateVehicleMetrics(...)` 成为纯投影函数
- 不再在里面更新累计副作用

### 改完后的判断标准

- 同一个控制器样本只处理一次
- `GPS / BMS / 设置项` 变更不会改变累计量

## `RideEnergyEstimator.kt`

### 现在最需要改

- 切分职责
- 停止混用毛放电与净放电口径
- 静置校准条件变严格
- 内阻学习不要使用单次脏阶跃

## `calculateVoltageSag(...)`

### 现在最需要改

- 不要短时间内反复重写静置基线
- 补一个归一化压降指标

## `stabilizeRangeForDisplay(...)`

### 现在最需要改

- 废弃基于固定里程步长的显示更新
- 改为时间与置信度驱动

---

# 8. 最终验收清单

## 算法正确性

- [ ] `Wh` 只由新控制器样本驱动累计
- [ ] `GPS / BMS / 设置项` 变更不会重复积分
- [ ] `SoC` 与 `remainingEnergyWh` 口径统一
- [ ] `estimatedRangeKm` 与 `remainingEnergyWh / predictedEfficiencyWhKm` 一致
- [ ] `avgEfficiencyWhKm` 在 UI、历史、导出中口径一致
- [ ] `voltageSag` 不再因短时低流瞬间反复重置

## BLE 现实适配

- [ ] 去重样本不会参与积分
- [ ] `dt < 50 ms` 不积分
- [ ] `dt > 3000 ms` 断段不积分
- [ ] 断流恢复后不会一次性补算超长时间窗口
- [ ] `streamState` 能区分 `LIVE / JITTERY / PAUSED / RECOVERING`

## 产品表现

- [ ] 起步时 `range` 不乱跳
- [ ] 巡航时 `range` 平稳收敛
- [ ] `SoC` 看起来连续且可信
- [ ] `CSV` 更容易解释
- [ ] 行程详情图表与首页数值不再互相打架

## 开发效率

- [ ] 新增 replay 回放器后可快速回归
- [ ] 每次改算法都能用旧日志重放验证
- [ ] `.agents` 里已有固定 skill 与 workflow 可复用

---

# 9. 推荐的执行顺序

如果你打算直接开始 `vibe`，推荐顺序是：

1. **先做 Phase 1**：止血，切断重复积分
2. **再做 Phase 2**：统一口径
3. **再做 Phase 3**：重做 `SoC`
4. **再做 Phase 4**：重做 `range`
5. **最后做 Phase 5**：补 replay 与回归

原因很简单：

- 不先切掉重复积分，后面所有算法都建立在污染数据上
- 不先统一口径，`SoC / range / CSV` 永远讲不通
- 不先整理样本层，后面再高级的滤波都会变成补锅

---

# 10. 一句话版结论

你这个项目当前最值得优先做的，不是再加一个新指标，而是把它从「界面聚合驱动的仪表 App」升级成「由新鲜控制器样本驱动的状态估算系统」。

第一刀一定是：

**把 `Wh / Ah / range / SoC` 的物理累计与估算，全部从 `combine(...)` 的 UI 聚合流里剥离出来。**
