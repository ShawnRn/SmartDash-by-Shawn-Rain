# Direction Stabilization Workflow

## 目标

把仪表右上角的「方向」从「偏车头朝向」改成「偏稳定行进方向」，消除轻微修把、低速漂移、传感器抖动造成的 UI 抽动。

## 架构

```
GPS Location + Rotation Vector Sensor
    │
    ▼
HeadingTracker (原始方向提供层)
    ├─ sensorHeadingDegrees: StateFlow<Float?>
    ├─ locationCourseDegrees: StateFlow<Float?>
    ├─ locationCourseAgeMs: StateFlow<Long?>
    ├─ sensorHeadingAgeMs: StateFlow<Long?>
    ├─ locationAccuracyMeters: StateFlow<Float?>
    └─ locationStepDistanceMeters: StateFlow<Float?>
    │
    ▼
DirectionStabilizer (UI 稳定化层)
    │ 5 级管线：
    │   1. 速度分层冻结
    │   2. 来源质量门控
    │   3. 死区过滤
    │   4. 速度分层指数平滑
    │   5. 转向速率限制
    │
    ▼
stableDirectionDegrees: StateFlow<Float>
    │
    ▼
DirectionLabelFormatter (文字格式化)
    ├─ 8 方位：北/东北/东/东南/南/西南/西/西北
    └─ 12° 滞回防止边界跳字
    │
    ▼
rideDirectionLabel: StateFlow<String> → UI 显示
```

## 关键参数

### 速度阈值

| 常量 | 值 | 含义 |
|------|-----|------|
| `FREEZE_SPEED_KMH` | 3.0 km/h | 低于此值方向冻结 |
| `LOW_SPEED_KMH` | 8.0 km/h | 低速区间上限 |
| `MID_SPEED_KMH` | 15.0 km/h | 中速区间上限 |
| `HIGH_SPEED_KMH` | 25.0 km/h | 高速区间上限 |

### 质量门控

| 常量 | 值 | 含义 |
|------|-----|------|
| `GPS_MAX_AGE_MS` | 2500 ms | GPS bearing 最大可接受年龄 |
| `SENSOR_MAX_AGE_MS` | 1500 ms | 传感器 heading 最大可接受年龄 |
| `GPS_MAX_ACCURACY_M` | 25 m | GPS 精度上限 |
| `GPS_MIN_STEP_DISTANCE_M` | 3 m | 低速时要求两点位移 ≥ 3m |

### 稳定化

| 常量 | 值 | 含义 |
|------|-----|------|
| `DEAD_BAND_DEG` | 5° | 死区 |
| `LABEL_HYSTERESIS_DEG` | 12° | 文字方位滞回 |

### 平滑参数 (alpha)

| 速度 | alpha | 效果 |
|------|-------|------|
| < 3 km/h | 0.00 | 完全冻结 |
| 3-8 km/h | 0.06 | 极强平滑 |
| 8-15 km/h | 0.12 | 适度平滑 |
| 15-25 km/h | 0.20 | 常规平滑 |
| ≥ 25 km/h | 0.30 | 快速跟随 |

### 转向速率限制

| 速度 | 最大转向速率 |
|------|-------------|
| < 8 km/h | 35°/s |
| 8-15 km/h | 50°/s |
| 15-25 km/h | 70°/s |
| ≥ 25 km/h | 90°/s |

## 调参指南

### 如果实车还觉得敏感

| 参数 | 当前值 | 建议调整 |
|------|--------|----------|
| `DEAD_BAND_DEG` | 5° | 调到 6-8° |
| `alpha` | 见上表 | 整体降低 0.02 |
| 低速冻结阈值 | 3 km/h | 提高到 4 km/h |

### 如果实车觉得反应太慢

| 参数 | 当前值 | 建议调整 |
|------|--------|----------|
| `DEAD_BAND_DEG` | 5° | 降到 3-4° |
| 15+ km/h alpha | 0.20 | 调到 0.25 |
| `maxTurnRate` | 见上表 | 整体提高 10-15°/s |

## 验证场景

| 场景 | 预期 |
|------|------|
| 静止/等红灯 | 方向冻结不跳 |
| 低速出库/挪车 | 方向极稳 |
| 15-30 km/h 直行 | GPS 主导，稳定反映前进方向 |
| 缓慢转弯 | 平滑过渡，不滞后 |
| GPS 突跳 | UI 不会瞬时抽过去 |
| 文字方位 | 12° 滞回消除北 ↔ 东北等临界抖动 |

## 相关文件

- `data/gps/DirectionStabilizer.kt` — 5 级管线核心
- `data/gps/DirectionLabelFormatter.kt` — 文字格式化 + 滞回
- `data/gps/HeadingTracker.kt` — 原始方向提供层（已拆分为 6 个 StateFlow）
- `MainViewModel.kt` — `stableDirectionDegrees` + `rideDirectionLabel`
