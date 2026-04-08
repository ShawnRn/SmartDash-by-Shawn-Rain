---
name: ble-telemetry-estimation
description: Use when modifying SmartDash BLE telemetry ingestion, ride energy accumulation, SoC estimation, range estimation, ride CSV export, replay tooling, or any code that mixes controller/BMS/GPS streams. Enforces the rule that physical integration must be driven only by fresh controller telemetry samples instead of generic UI combine flows.
---

# SmartDash BLE Telemetry Estimation

本 skill 用于所有涉及以下主题的修改：

- `MainViewModel.kt` 中的实时指标聚合
- `RideAccumulator.kt` 中的 `Wh / Ah / distance / peak` 物理积分
- `BatteryStateEstimator.kt` 中的 `SoC / 内阻` 估算
- `RangeEstimator.kt` 中的 `range / 窗口效率` 估算
- `TelemetryStreamProcessor.kt` 中的去重、质量分类、有限值守卫
- 控制器 `BLE` 遥测样本解析、去重、时间戳与积分
- 行程记录、`CSV` 导出、平均能耗与估计续航
- 回放器、算法回归测试、历史日志再演算

## 核心前提

SmartDash 的控制器数据来自 `BLE notify / poll`，它**不是**严格等间隔、严格同步、严格无抖动的理想采样源。

因此：

1. **物理积分只能由"新鲜控制器样本"驱动**。
2. `GPS / BMS / 设置项 / UI 状态` 的变更，**不能**重复触发 `Wh / Ah / distance / peak` 等累计量积分。
3. 所有 `SoC / range / Wh/km` 算法都必须显式区分：
   - 原始观测层 `raw`
   - 物理导出层 `derived`
   - 状态估计层 `estimation`
   - 展示层 `presentation`

## 必守规则

### 0. ProtocolParser._metrics 必须是 SharedFlow

`ProtocolParser._metrics` 使用 `MutableSharedFlow<VehicleMetrics>(extraBufferCapacity = 64)`。

**绝对不能改回 `MutableStateFlow`**，因为 StateFlow 的 `equals` 去重会在连续两帧相同数值时静默丢弃，导致 `TelemetryStreamProcessor` 漏帧、`dtMs` 断裂、`allowIntegration` 永远为 false、里程/能耗/平均能耗全部为 0。

UI 展示层的 `combine` 使用 `ProtocolParser.latestMetrics`（StateFlow 快照），遥测积分链路使用 `ProtocolParser.metrics`（SharedFlow 每帧必达）。

### 1. 不要在 `combine(...)` 的 UI 聚合流里做物理累计

禁止在这类函数里直接累计：

- `_rideEnergyWh += ...`
- `_rideRecoveredEnergyWh += ...`
- `_ridePeakRegenKw = ...`
- `_maxSpeed = ...`
- `_speedSamples++`

这些动作必须迁移到「新控制器样本到达」的专用处理链：

```kotlin
ProtocolParser.metrics.onEach {
    val sample = telemetryStreamProcessor.process(it)
    if (_isRideActive.value && !isRidePausedForStop()) {
        rideAccumulator.accumulate(sample)
    }
}.launchIn(viewModelScope)
```

### 2. 引入统一 `TelemetrySample`

任何新算法都优先围绕统一样本对象实现：

```kotlin
data class TelemetrySample(
    val timestampMs: Long,
    val sourceFrameId: Long?,
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

### 3. TelemetryStreamProcessor 质量门控

#### 有限值守卫（最高优先级）
所有输入先 `isFinite()` 检查。任一非有限值 → 直接返回 `OUTLIER` 样本（payload 归零，`allowIntegration = false`），**不更新状态跟踪变量**。

#### 去重
`dt < 20ms` 且电压/电流/RPM 变化极小 → `DUPLICATE`

#### TOO_DENSE
`dt < 15ms` → `TOO_DENSE`

#### GAP_RESET
`dt > 5000ms` → `GAP_RESET`（BLE 断流后的第一帧，不积分）

#### OUTLIER
- 电压不在 15-120V
- |母线电流| > 550A
- |RPM| > 20000
- 速度 > 300 km/h
- RPM > 1000 且速度 < 1 km/h（逻辑矛盾）

#### 积分门控
- `allowIntegration = GOOD && dtMs >= 20ms`
- `allowLearning = GOOD && dtMs in 50..2000ms`

### 4. RideAccumulator 物理积分安全

**dtMs 必须钳位到安全范围**：

```kotlin
val safeDtMs = sample.dtMs.coerceIn(50L, 500L)
```

防止 BLE 断流造成 2-3s 间隔导致过度积分，也防止突发回调 20ms 间隔导致欠积分。

距离、能量、移动时间都使用 `safeDtMs` 计算。

### 5. 统一能量标准 (United Energy Standard)

为了防止逻辑复杂化后出现的口径分裂风险，SmartDash 强制执行以下能量口径标准：

- **Display (UI)**: 所有面向用户的能耗 (Efficiency) 显示，**必须默认强制采用 `Net Wh` (Traction - Regen) 口径**。
- **Estimation (Range)**: 续航预测模型**必须**基于 `Net Wh` 能效基准。
- **Analysis**: 仅在后台分析或特定详情页允许区分毛能耗 (Traction) 与回收贡献 (Regen)。
- **Data Model**: 能效字段应显式区分为 `avgNetEfficiencyWhKm` 与 `avgTractionEfficiencyWhKm`。

### 6. `SoC` 必须区分内部估算值与显示值

- 内部值可快速响应
- 显示值必须限速、防抖、缓变
- 仅在满足低流、低速、稳定电压等条件时，才允许 `OCV` 校准显著介入

### 7. `range` 不要按固定 `1 km` 台阶刷新

优先使用：

- 起步冻结窗口
- `EMA` 平滑
- 每秒最大变化量限制

### 8. NaN 防护（P0 Hotfix 级约束）

所有涉及 `VehicleProfile` 浮点字段的学习函数，必须在入口和出口都加 `isFinite()` 守卫：

- `blendLearnedEfficiencyWhKm()`：输入 NaN → 返回上一次有效值
- `calculateLearnedUsableEnergyRatio()`：SoC drop NaN → 返回 profile 默认值
- `blendLearnedUsableEnergyRatio()`：输出 NaN → 返回上一次值
- `toJson()`：所有 Float 字段 `finiteOr(default)` 后再写 JSON
- `fromJson()`：读入后 NaN 清洗 + 二次 coerce 归一化
- `normalizedProfile()`：先 `finiteOr()` 再 `coerceAtLeast/coerceIn`
- `saveVehicleProfiles()`：外层 `runCatching` 防止 crash 传播

### 9. 智科全 0 帧抑制

`ZhikeProtocol` 检测到连续全 0 实时帧（电压 < 2V, 电流 < 0.1A, RPM < 1, 速度 < 0.1）时：

- 不再 `emit` 给上层
- 不覆盖 `lastRealtimeMetrics`
- 连续 10 帧全 0 时记录警告日志
- 恢复正常后记录恢复日志

这防止无效遥测流导致 SoC 像掷骰子、续航乱跳、里程/能耗长期为 0。

## 数据流架构

```
BLE Controller Frame (Zhike/etc)
    │
    ▼
ProtocolParser.parse(data)
    │
    ├── emit() ──→ _metrics.tryEmit(VehicleMetrics)    [SharedFlow, 每帧必达]
    │              _latestMetrics.value = metrics        [StateFlow, UI 快照]
    │
    ▼
MainViewModel: ProtocolParser.metrics.onEach { rawMetrics }
    │
    ▼
TelemetryStreamProcessor.process(rawMetrics)
    │  → isFinite() 守卫 → OUTLIER if non-finite
    │  → Duplicate / TooDense / GapReset / Outlier 分类
    │  → allowIntegration = GOOD && dtMs >= 20ms
    │  → allowLearning = GOOD && dtMs in 50..2000ms
    ▼
TelemetrySample (with quality flags)
    │
    ├── allowIntegration ──→ RideAccumulator.accumulate(sample)
    │                          → safeDtMs = dtMs.coerceIn(50, 500)
    │                          → tractionWh / regenWh / netAh / distance
    │
    ├── BatteryStateEstimator.estimate(sample)
    │     → EMA smoothing (voltage, current)
    │     → Internal resistance learning (dV/dI)
    │     → SoC = Ah integration + OCV fusion
    │
    └── RangeEstimator.estimate(batteryState, accumulatorState)
          → Sliding window efficiency (2.0km target)
          → remainingEnergy = SoC% * capacityAh * series * 3.7V * usableRatio
          → range = remainingWh / avgEfficiencyWhKm
```

## 修改检查清单

修改遥测相关代码后，必须确认：

- [ ] `ProtocolParser._metrics` 仍是 `SharedFlow`
- [ ] `TelemetryStreamProcessor` 有 `isFinite()` 守卫
- [ ] `RideAccumulator` 使用 `safeDtMs = coerceIn(50, 500)`
- [ ] 学习函数有 NaN 输入/输出守卫
- [ ] `VehicleProfile.toJson()` 使用 `finiteOr()`
- [ ] `saveVehicleProfiles()` 外层有 `runCatching`
- [ ] 全 0 帧不再 emit 给上层
- [ ] 编译通过：`./gradlew :app:compileDebugKotlin --console plain`
