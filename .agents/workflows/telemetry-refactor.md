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
- `audit/Telemetry_Estimation_Refactor_Plan_2026-04-07.md`

## 2. 锁定当前风险点

先定位这些代码：

- `MainViewModel.metrics = combine(...)`
- `calculateVehicleMetrics(...)`
- `calculateVoltageSag(...)`
- `stabilizeRangeForDisplay(...)`
- `RideEnergyEstimator.estimate(...)`
- `updateLearnedResistance(...)`

重点确认是否存在：

- 在 UI 聚合流里做 `Wh` 积分
- 同一控制器样本被 `GPS / BMS / 设置变更` 重复使用
- `range` 与 `remainingEnergyWh` 口径不统一
- `SoC` 过度依赖车辆档案容量先验

## 3. 第一刀：拆出样本驱动链路

最优先完成：

1. 新建统一 `TelemetrySample`
2. 新建 `TelemetryStreamProcessor`
3. 把 `Wh / regen / peak / sample quality` 更新逻辑迁出 `calculateVehicleMetrics(...)`
4. 保证只有「新鲜控制器样本」进入累计与估算链

## 4. 第二刀：统一能量口径

至少要明确并落盘：

- `tractionEnergyWh`
- `regenEnergyWh`
- `netBatteryEnergyWh`
- `avgEfficiencyWhKm` 的分母与分子
- `estimatedRangeKm = remainingUsableEnergy / predictedEfficiency`

## 5. 第三刀：重写 `BatteryStateEstimator`

推荐方向：

- 行驶中以库仑计 / 能量积分为主
- 静置时以 `OCV` 修正为辅
- 输出：
  - `socByAhPercent`
  - `socByOcvPercent`
  - `fusedSocPercent`
  - `displaySocPercent`
  - `remainingEnergyWh`
  - `socConfidence`

## 6. 第四刀：重写 `RangeEstimator`

推荐加入：

- 起步冻结窗口
- 短窗 / 中窗 / 全程平均三层能耗
- `EMA` 平滑
- 每秒最大变化量限制
- `rangeConfidence`
- 低速 / 断流 / 激烈加减速场景降权

## 7. 第五刀：补回放与回归验证

至少做一个可以离线 replay `CSV` 的入口，验证：

- 总 `Wh`
- 回收 `Wh`
- `SoC`
- `range`
- `avgEfficiencyWhKm`
- `voltageSag`

建议覆盖 5 类样本：

- 短途启停
- 中速巡航
- 暴力加速
- 长下坡回收
- 低电后段骑行

## 8. 最低验收清单

### 必过

- `GPS` 刷新不再增加 `Wh`
- `BMS` 刷新不再增加 `Wh`
- `range` 与 `remainingEnergyWh` 可通过公式互相解释
- 断流后不会跨 `3 s+` 长间隔硬积分
- `SoC` 在短途骑行中不再频繁上下跳
- `CSV` / 仪表页 / 行程详情三者口径一致

### 最好补齐

- `sampleQuality`
- `streamState`
- `socConfidence`
- `rangeConfidence`
- `normalizedSagPerAmp`

## 9. 改完后执行

```bash
./gradlew :app:compileDebugKotlin --console plain
```

如涉及安装联调：

```bash
.agents/scripts/install-dev-release.sh
```
