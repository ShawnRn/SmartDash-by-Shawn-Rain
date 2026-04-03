---
description: 智科控制器“连接成功但数据不刷新”专项排查流程
---

本工作流用于排查智科控制器扫描/连接后仪表全 0、参数读不到、握手异常等问题。

### 1. 安装最新 APK
// turbo
```bash
.agents/scripts/install-dev-release.sh
```

### 2. 设置 app 内调试级别
- 打开 `设置`
- 将日志级别切到 `VERBOSE`
- 如需后台悬浮窗联调，打开“后台自动打开悬浮窗”

### 3. 开始抓日志
// turbo
```bash
.agents/scripts/logcat-habe.sh --clear
```

### 4. 复现动作
- 扫描并连接智科控制器
- 观察是否打印协议识别、服务发现、`FFF1/FFF2` 读取、`FFE2 0102` 预热、`FFE1 AA13FF01AA130001` 轮询
- 进入“智科控制器调校”并尝试刷新参数

### 5. 导出 app 内日志
- 在 app 设置页点击“分享日志”或“导出日志”
- 与 adb logcat 一起留档

### 6. 核对关键日志
- `BleManager`: 连接 / 服务发现 / 写特征 / 通知回包
- `ProtocolParser`: 协议匹配与参数快照
- `ZhikeProtocol`: 实时帧校验或配置解析
