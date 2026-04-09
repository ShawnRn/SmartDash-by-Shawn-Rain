# Pro Performance Monitor

## 目标

- 在主编译机 `shawn-rains-macbook-pro` 上常驻一套轻量性能监控
- 允许当前机器直接通过浏览器访问 Pro 的实时 CPU / 内存 / swap / top 进程页面
- 用于快速判断远端构建是否真的跑在 Pro，以及 Pro 当前是否过载

## 固定位置

- 浏览器地址：`http://100.103.86.124:8765/`
- Swift 采样器：`~/smartdash_perf_monitor.swift`
- 启动脚本：`~/smartdash_perf_monitor_start.sh`
- 页面目录：`~/smartdash-perf-monitor/`
- LaunchAgents：
  - `~/Library/LaunchAgents/com.shawnrain.smartdash-perf-sampler.plist`
  - `~/Library/LaunchAgents/com.shawnrain.smartdash-perf-http.plist`

## 运行机制

- `smartdash_perf_monitor.swift` 每 2 秒采样一次 Pro 本机状态，并写入 `~/smartdash-perf-monitor/state.json`
- `smartdash_perf_monitor_start.sh` 负责重装并 kickstart 两个 LaunchAgent
- `com.shawnrain.smartdash-perf-sampler.plist` 以 `RunAtLoad + KeepAlive` 方式常驻 Swift 采样器
- `com.shawnrain.smartdash-perf-http.plist` 以 `RunAtLoad + KeepAlive` 方式常驻 HTTP 服务

## AI Agent 强约束

- 不要把这套监控改成只在本地 Air 前台运行
- 不要随手停掉 Pro 上的 LaunchAgent，除非用户明确要求
- 当用户怀疑“没用 Pro 编译”或“Pro 是否过载”时，先打开这个页面再做判断
- 如果监控失效，优先修复 Pro 上的三个文件，不要新起一套平行方案

## 手动操作

```bash
# 在 Pro 上手动重启监控
~/smartdash_perf_monitor_start.sh

# 在 Pro 上重装 LaunchAgent
launchctl bootout gui/$(id -u) ~/Library/LaunchAgents/com.shawnrain.smartdash-perf-sampler.plist || true
launchctl bootout gui/$(id -u) ~/Library/LaunchAgents/com.shawnrain.smartdash-perf-http.plist || true
launchctl bootstrap gui/$(id -u) ~/Library/LaunchAgents/com.shawnrain.smartdash-perf-sampler.plist
launchctl bootstrap gui/$(id -u) ~/Library/LaunchAgents/com.shawnrain.smartdash-perf-http.plist
launchctl kickstart -k gui/$(id -u)/com.shawnrain.smartdash-perf-sampler
launchctl kickstart -k gui/$(id -u)/com.shawnrain.smartdash-perf-http

# 在当前机器打开浏览器
open "http://100.103.86.124:8765/"
```

## 验证

```bash
curl -sf http://100.103.86.124:8765/state.json
curl -sf http://100.103.86.124:8765/index.html
```
