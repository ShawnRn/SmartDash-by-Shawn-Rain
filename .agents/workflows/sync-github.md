---
description: 构建、测试、提交并推送当前分支到 GitHub
---

本工作流用于同步当前仓库状态到 GitHub，默认会先执行 Debug 构建与单元测试。

> [!IMPORTANT]
> 执行前请确保 `.gitignore` 已排除本地反编译参考、小程序源码、构建缓存和临时日志。

### 1. 可选：预检
// turbo
```bash
.agents/scripts/preflight.sh
```

### 2. 构建与测试后同步
// turbo
```bash
.agents/scripts/sync-github.sh "chore: update AGENTS and agent workflows"
```

### 3. 默认行为
- 构建 `:app:assembleDebug`
- 运行 `:app:testDebugUnitTest`
- 若检测到 keystore / 签名密码文件被加入 Git，将直接中止
- `git add -A`
- `git commit -m <message>`
- `git push origin <current-branch>`
