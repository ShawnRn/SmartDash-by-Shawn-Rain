---
name: wxapp-decompile
description: macOS 微信小程序包（.wxapkg）获取与反编译方法。
---

# macOS 微信小程序反编译指南

此 Skill 记录了在 macOS 环境下定位并解密/反编译微信小程序 `.wxapkg` 包的标准化流程。

## 1. 定位缓存文件

在 macOS 上，微信小程序的 `.wxapkg` 包存储在以下沙盒路径中：

### 路径 A (常用):
`~/Library/Containers/com.tencent.xinWeChat/Data/Library/WechatPrivate/<hash>/appbrand/pkg/`

### 路径 B (部分版本):
`~/Library/Group Containers/group.com.tencent.xinWeChat/Library/WechatPrivate/<hash>/appbrand/pkg/`

> [!TIP]
> `<hash>` 是一串与微信账号关联的 32 位十六进制哈希字符串。如果不确定哪个是目标小程序，可以按修改时间排序：
> `ls -lt ~/Library/Containers/com.tencent.xinWeChat/Data/Library/WechatPrivate/`

## 2. 准备反编译环境

需要安装 **Node.js** 和 **wxappUnpacker** 工具。

### 安装 Node.js (Homebrew):
```bash
brew install node
```

### 克隆并配置工具:
```bash
git clone https://github.com/qwerty472123/wxappUnpacker.git
cd wxappUnpacker
npm install
```

## 3. 执行解包

使用 `node` 运行解包脚本，指向获取到的 `.wxapkg` 文件。

```bash
node wuWxapkg.js /路径/到/你的/小程序.wxapkg
```

## 4. 结果分析

解包完成后，会在指定目录下生成源码文件夹，包含：
- `.js`: 逻辑代码
- `.wxml`: 页面结构
- `.wxss`: 样式文件
- `.json`: 配置文件

> [!IMPORTANT]
> **注意**: 部分小程序使用了分包加载或代码混淆，解包后的代码可能需要进一步手动修复或格式化。
