---
description: SmartDash 遥测估算链路重构与回放验证流程
---

本工作流用于处理以下需求：

- `SoC / range / Wh / Wh/km` 估算不稳定
- `CSV` 导出与仪表页数据口径不一致
- `BLE` 数据因重复帧、抖动、断流而污染累计量
- 需要把当前代码重构为「样本驱动的估算链路」

## 1. 先读规范

优先阅读：

- `AGENTS.md`
- `.agents/skills/ble-telemetry-estimation/SKILL.md`

## 2. 当前状态（已重构完成）

以下组件已就位：

| 组件 | 文件 | 状态 |
|------|------|------|
| 遥测流入口 | `ProtocolParser.kt` | ✅ `_metrics` 为 `SharedFlow`，`_latestMetrics` 为 `StateFlow` |
| 流处理器 | `TelemetryStreamProcessor.kt` | ✅ isFinite 守卫、质量分类、dt 门控放宽 |
| 积分器 | `RideAccumulator.kt` | ✅ dtMs 钳位 50-500ms |
| SoC 估算 | `BatteryStateEstimator.kt` | ✅ Ah+OCV 融合 + 内阻学习 |
| 续航估算 | `RangeEstimator.kt` | ✅ 滑动窗口效率模型 |
| 样本质量 | `SampleQuality.kt` | ✅ GOOD/DUPLICATE/TOO_DENSE/GAP_RESET/OUTLIER |
| NaN 防护 | `VehicleProfile.kt` / `SettingsRepository.kt` | ✅ finiteOr + runCatching |
| 全 0 帧抑制 | `ZhikeProtocol.kt` | ✅ 连续全 0 不再 emit |

## 3. 锁定当前风险点

如需进一步调试，先定位这些代码：

- `MainViewModel.metrics = combine(...)` — UI 指标展示
- `MainViewModel.stableDirectionDegrees` — 方向稳定化流
- `ProtocolParser.metrics.onEach { ... }` — 遥测积分链路
- `TelemetryStreamProcessor.process(...)` — 样本质量分类
- `RideAccumulator.accumulate(...)` — 物理积分
- `BatteryStateEstimator.estimate(...)` — SoC 估算
- `RangeEstimator.estimate(...)` — 续航估算

重点确认是否存在：

- 在 UI 聚合流里做 `Wh` 积分（已被禁止）
- 同一控制器样本被 `GPS / BMS / 设置变更` 重复使用
- `range` 与 `remainingEnergyWh` 口径不统一
- `SoC` 过度依赖车辆档案容量先验
- NaN 值未被拦截传入 `toJson()`

## 4. 能量口径

已明确并落盘：

- `tractionEnergyWh` — 牵引能耗
- `regenEnergyWh` — 回收能量
- `netBatteryEnergyWh = tractionWh - regenWh` — 净电池能量
- `avgEfficiencyWhKm` — 基于 `Net Wh` 的平均能耗
- `estimatedRangeKm = remainingUsableEnergy / predictedEfficiency`

## 5. BatteryStateEstimator 状态

已实现：

- EMA 平滑（voltage alpha=0.12, current alpha=0.12）
- 内阻学习：电流阶跃 > 10A 时触发 `R = |dV/dI|`，85%/15% 指数平滑
- OCV-based SoC：锂离子放电曲线表映射
- Ah-integration SoC：从初始 SoC 减去 `netBatteryAh / capacityAh`
- 融合：动态权重（静止/低速/中速/高速不同比例）
- 输出：`socPercent`, `socByAhPercent`, `socByOcvPercent`, `confidence`

## 6. RangeEstimator 状态

已实现：

- 滑动窗口：2.0km 目标窗口，0.2km 最小新鲜阈值
- `trimRangeWindow()`：窗口超限时剔除最老样本，部分样本处理
- 效率：`avgEfficiencyWhKm = windowEnergyWh / windowDistanceKm`
- 剩余能量：`SoC% * capacityAh * series * 3.7V * usableEnergyRatio`
- 续航：`remainingWh / avgEfficiencyWhKm`

## 7. 最低验收清单

### 必过

- `GPS` 刷新不再增加 `Wh`
- `BMS` 刷新不再增加 `Wh`
- `range` 与 `remainingEnergyWh` 可通过公式互相解释
- 断流后不会跨 `5 s+` 长间隔硬积分
- `SoC` 在短途骑行中不再频繁上下跳
- `CSV` / 仪表页 / 行程详情三者口径一致
- NaN 不再导致 `JSONException` 崩溃

### 最好补齐

- `sampleQuality` 分布监控
- `streamState` 健康度
- `socConfidence`
- `rangeConfidence`
- `normalizedSagPerAmp`

## 8. 实车验证

```bash
# 安装 devRelease 到手机
.agents/scripts/install-dev-release.sh

# 开启 VERBOSE 日志
# 设置页 → 日志级别 → VERBOSE

# 抓取骑行日志
.agents/scripts/logcat-habe.sh --clear
# 复现问题后从设置页分享日志
```

## 9. 改完后执行

```bash
./gradlew :app:compileDebugKotlin --console plain
```

如涉及安装联调：

```bash
.agents/scripts/install-dev-release.sh
```
