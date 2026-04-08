---
name: direction-stabilization
description: Use when modifying SmartDash direction/heading display, DirectionStabilizer, HeadingTracker, DirectionLabelFormatter, or any code that affects the top-right corner heading indicator. Enforces speed-layered freezing, GPS/sensor quality gating, deadband, exponential smoothing, turn rate limiting, and label hysteresis.
---

# SmartDash Direction Stabilization

本 skill 用于所有涉及以下主题的修改：

- 仪表右上角方向显示
- `DirectionStabilizer.kt` 算法调参
- `HeadingTracker.kt` 原始方向暴露
- `DirectionLabelFormatter.kt` 文字方位格式化
- `MainViewModel.stableDirectionDegrees` / `rideDirectionLabel`

## 核心设计原则

**方向显示不是"车头朝向"，而是"稳定行进方向"。**

两轮车的车头会频繁微调（修把、避让、等红灯），如果 UI 跟着车头走，观感就是持续抽动。

## 5 级处理管线（不可跳过）

```
原始输入 (GPS bearing + sensor heading)
    │
    ▼
1. 速度分层冻结
   speed < 3 km/h → FROZEN
    │
    ▼
2. 来源质量门控
   GPS: age ≤ 2500ms, accuracy ≤ 25m, step ≥ 3m (低速)
   Sensor: age ≤ 1500ms
    │
    ▼
3. 死区过滤
   Δ < 5° → 不更新
    │
    ▼
4. 速度分层指数平滑
   alpha: 0.00 / 0.06 / 0.12 / 0.20 / 0.30 (按速度分层)
    │
    ▼
5. 转向速率限制
   maxTurnRate: 35 / 50 / 70 / 90 °/s (按速度分层)
    │
    ▼
stableDirectionDeg → DirectionLabelFormatter (12° 滞回) → UI
```

## 必守规则

### 1. 不要修改 `HeadingTracker` 传感器底层逻辑

`HeadingTracker` 的职责是原始数据采集和暴露。所有稳定化处理必须在 `DirectionStabilizer` 中完成。

### 2. 速度冻结是硬性约束

`speed < 3 km/h` 时方向必须冻结。这不是可选项，是消除静止抖动的第一道防线。

### 3. GPS 质量门控不可省略

只看 `hasBearing()` 不够。必须同时检查：
- `age` ≤ `GPS_MAX_AGE_MS`
- `accuracy` ≤ `GPS_MAX_ACCURACY_M`
- 低速时 `stepDistance` ≥ `GPS_MIN_STEP_DISTANCE_M`

### 4. 文字方位必须带滞回

8 方位边界附近（如北 ↔ 东北 22.5°）极易跳字。`DirectionLabelFormatter` 的 `hysteresisDeg = 12f` 确保方向进入新扇区足够深才切换。

### 5. 调参只改 `DirectionStabilizer` 参数

不要为了调体验去改 `HeadingTracker` 的传感器注册/解注册逻辑、`alpha` 或者原始 heading 计算。所有体验调整都通过 `DirectionStabilizer` 的常量完成。

## 参数调优指南

### 方向还敏感？

逐步提高以下参数（每次只改一个）：
1. `DEAD_BAND_DEG`: 5° → 6° → 8°
2. 整体 `alpha` 降低 0.02
3. 低速冻结阈值: 3 → 4 km/h

### 方向反应太慢？

逐步放宽以下参数：
1. `DEAD_BAND_DEG`: 5° → 4° → 3°
2. 15+ km/h `alpha`: 0.20 → 0.25
3. `maxTurnRate`: 各档位 +10°/s

## 相关文件

- `data/gps/DirectionStabilizer.kt` — 5 级管线
- `data/gps/DirectionLabelFormatter.kt` — 文字格式化 + 滞回
- `data/gps/HeadingTracker.kt` — 原始方向提供层
- `MainViewModel.kt` — `stableDirectionDegrees` + `rideDirectionLabel`
- `.agents/workflows/direction-stabilization.md` — 详细工作流
