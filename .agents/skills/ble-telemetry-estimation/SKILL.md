---
name: ble-telemetry-estimation
description: Use when modifying SmartDash BLE telemetry ingestion, ride energy accumulation, SoC estimation, range estimation, ride CSV export, replay tooling, or any code that mixes controller/BMS/GPS streams. Enforces the rule that physical integration must be driven only by fresh controller telemetry samples instead of generic UI combine flows.
---

# SmartDash BLE Telemetry Estimation

本 skill 用于所有涉及以下主题的修改：

- `MainViewModel.kt` 中的实时指标聚合
- `RideEnergyEstimator.kt` 中的 `SoC / range / Wh / 内阻` 估算
- 控制器 `BLE` 遥测样本解析、去重、时间戳与积分
- 行程记录、`CSV` 导出、平均能耗与估计续航
- 回放器、算法回归测试、历史日志再演算

## 核心前提

SmartDash 的控制器数据来自 `BLE notify / poll`，它**不是**严格等间隔、严格同步、严格无抖动的理想采样源。

因此：

1. **物理积分只能由“新鲜控制器样本”驱动**。
2. `GPS / BMS / 设置项 / UI 状态` 的变更，**不能**重复触发 `Wh / Ah / distance / peak` 等累计量积分。
3. 所有 `SoC / range / Wh/km` 算法都必须显式区分：
   - 原始观测层 `raw`
   - 物理导出层 `derived`
   - 状态估计层 `estimation`
   - 展示层 `presentation`

## 必守规则

### 1. 不要在 `combine(...)` 的 UI 聚合流里做物理累计

禁止在这类函数里直接累计：

- `_rideEnergyWh += ...`
- `_rideRecoveredEnergyWh += ...`
- `_ridePeakRegenKw = ...`
- `_maxSpeed = ...`
- `_speedSamples++`

这些动作必须迁移到「新控制器样本到达」的专用处理链。

### 2. 引入统一 `TelemetrySample`

任何新算法都优先围绕统一样本对象实现，例如：

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
    val sampleQuality: SampleQuality
)
```

### 3. 所有积分都必须经过 `dt` 合法性检查

推荐阈值：

- `dt < 50 ms`：视为过密或重复，不积分
- `50 ms <= dt <= 2000 ms`：允许积分
- `dt > 3000 ms`：视为断流，重置积分窗口，不跨段硬积分

### 4. 统一能量口径

内部至少维护 3 本账：

- `tractionEnergyWh`
- `regenEnergyWh`
- `netBatteryEnergyWh`

并明确：

- `SoC / remainingEnergyWh` 主要跟 `netBatteryEnergyWh` 相关
- 驾驶风格分析主要看 `tractionEnergyWh`
- 回收效果主要看 `regenEnergyWh`
- `CSV` 导出中要把字段定义写清楚

### 5. `SoC` 必须区分内部估算值与显示值

- 内部值可快速响应
- 显示值必须限速、防抖、缓变
- 仅在满足低流、低速、稳定电压等条件时，才允许 `OCV` 校准显著介入

### 6. `range` 不要按固定 `1 km` 台阶刷新

优先使用：

- 起步冻结窗口
- `EMA` 平滑
- 每秒最大变化量限制
- 置信度驱动的更新节奏

### 7. `voltageSag` 需要区分显示值与分析值

建议至少区分：

- `instantSagV`
- `normalizedSagPerAmp`

不要在每次低流低速瞬间直接重写静置基线。

## 推荐重构骨架

```text
BLE frame / parser
  -> TelemetryStreamProcessor
  -> TelemetrySample
  -> RideAccumulator
  -> BatteryStateEstimator
  -> RangeEstimator
  -> DashboardProjector
  -> UI / CSV / RideHistory
```

## 代码审查重点

修改完成后，必须优先检查：

1. `GPS / BMS` 刷新是否还会重复增加 `Wh`
2. `CSV totalEnergyWh` 是否只随新控制器样本变化
3. `range` 是否在起步和低速时仍然乱跳
4. `SoC` 是否仍在短时间内频繁上跳 / 下跳
5. 断流恢复后是否会把长间隔一次性算进能量
6. `rideActive = false` 时累计量是否仍被动变化

## 推荐联动文件

- `app/src/main/java/com/shawnrain/habe/MainViewModel.kt`
- `app/src/main/java/com/shawnrain/habe/data/RideEnergyEstimator.kt`
- `app/src/main/java/com/shawnrain/habe/ble/ProtocolParser.kt`
- `app/src/main/java/com/shawnrain/habe/ble/BleManager.kt`
- `app/src/main/java/com/shawnrain/habe/data/history/RideHistoryModels.kt`
- `.agents/workflows/telemetry-refactor.md`
- `audit/Telemetry_Estimation_Refactor_Plan_2026-04-07.md`
