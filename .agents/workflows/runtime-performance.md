# Runtime Performance Audit

## 低端机优先级

SmartDash 的性能优化优先级不是跑分，而是：

1. BLE 数据到 UI 的连续性
2. 设置页和历史页的进入流畅度
3. App 回到前台后的恢复速度
4. 同步完成后的无感刷新

## 已验证的高价值优化方向

- 避免在 `SettingsRepository` 的多个 `Flow` 中重复解析 `vehicle_list_json`
- 对车辆级派生状态，优先建立：
  - `vehicleProfiles`
  - `currentVehicleId`
  - `currentVehicleProfile`
  再做轻量组合
- 同步合并完成后，优先做增量落盘和局部派生，不要为了几项设置变化重建整条上层状态链

## 审计顺序

1. 先看数据层
- `SettingsRepository`
- `MainViewModel`
- `DriveSyncCoordinator`

2. 再看 UI 收集范围
- 是否整页收集了过宽的应用级状态
- 是否把高频遥测和低频设置绑在同一 `collectAsState`

3. 最后看日志和调试开关
- 高日志级别在低端机上会放大 I/O 和字符串分配
- 发版默认不要长期停留在过重的调试路径

## 代码约束

- 新增 `Flow` 时，优先共享已解析结果，不要复制解析成本
- 新增同步字段时，避免在 UI 层临时拼凑“恢复态”；应在 `SettingsRepository.applyDriveSyncState()` 一次性落好
- 遥测高频链路和设置低频链路尽量解耦，避免一次设置变化触发整套遥测展示重组
