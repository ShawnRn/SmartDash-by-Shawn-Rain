# Mock Zhike Controller

本工作流用于把当前这台 Mac 模拟成一个智科 BLE 控制器，方便真机 app 在没有实车时联调 UI、连接链路和参数展示。

## 快速开始

默认场景：

```bash
.agents/scripts/mock-zhike-controller.sh
```

指定设备名：

```bash
.agents/scripts/mock-zhike-controller.sh --device-name ZK-MOCK
```

使用自定义场景：

```bash
.agents/scripts/mock-zhike-controller.sh \
  --device-name ZK-MOCK \
  --scenario .agents/examples/zhike-mock-scenario.json
```

## 行为说明

- 广播 BLE 本地名，默认 `ZK-MOCK`
- 广播 `FFE0` 服务，包含：
  - `FFE1`：通知 + 写入
  - `FFE2`：写入
- 提供 `FFF1 / FFF2` 读特征，满足 app 的智科握手探测
- 收到 `AA110001` 后返回参数帧
- 收到 `AA13FF01` / `AA130001` 轮询后返回实时帧
- 订阅成功后也会按 `updateHz` 持续推送实时帧

## 场景文件

场景 JSON 支持：

- `name`
- `updateHz`
- `settings`
- `steps[]`

每个 `step` 支持：

- `name`
- `durationSeconds`
- `voltage`
- `busCurrent`
- `phaseCurrent`
- `speedKmh`
- `controllerTemp`
- `faultCode`
- `braking`
- `cruise`
- `reverse`

## 已知限制

- 当前 app 的智科解析把 `Word[6]` 同时映射为速度和 RPM，所以模拟器优先保证速度显示正确，RPM 会跟随该原始字产生联动值
- 这套脚本主要用于 UI / 展示 / 协议联调，不代表真实控制器全部行为
- 首次运行若 macOS 弹出蓝牙权限提示，需要允许 Terminal / Codex 访问蓝牙
