---
description: 抓取 SmartDash 的 adb logcat 调试日志
---

本工作流用于 BLE / 协议 / 后台 `PiP` 联调，重点覆盖 `BleManager`、`ProtocolParser`、`ZhikeProtocol`、`MainActivity` 等标签。

### 1. 可选：清空旧日志
// turbo
```bash
.agents/scripts/logcat-habe.sh --clear
```

### 2. 实时抓取
// turbo
```bash
.agents/scripts/logcat-habe.sh
```

### 3. 使用建议
- 先在 app 设置页把日志级别切到 `VERBOSE`
- 再执行连接智科、刷新参数、切后台进入 `PiP`、预见返回手势等复现动作
- 停止抓取后，到 `.agents/logs/` 找对应日志文件
