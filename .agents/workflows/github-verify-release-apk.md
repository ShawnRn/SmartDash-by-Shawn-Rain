---
description: GitHub Actions 构建验证版 release APK，并下载安装到真机
---

本工作流用于“不要发布 GitHub Release，只要一份正式 release 签名 APK 来给当前设备验证安装”的场景。

## 适用场景

- 用户说“用 GitHub Actions 编译一个 release APK 给我装机试”
- 本机 `build-release.sh` 太慢、太卡，或不想让本机长时间跑 `R8 + lintVital + shrinkReleaseRes`
- 需要在真机上验证接近正式发布态的性能，但又不想创建 GitHub Release

## 默认原则

1. 真机验证 release APK 时，默认优先走 `.github/workflows/verify-release.yml`
2. `verify-release.yml` 负责：
   - `:app:assembleRelease`
   - 校验 APK 输出
   - 上传 artifact
3. `verify-release.yml` 不创建 GitHub Release
4. 正式对外交付或归档，才走 `.github/workflows/release.yml`

## 标准流程

### 1. 推送待验证代码到临时分支

```bash
git checkout -b codex/verify-release-apk-YYYYMMDD
git add <changed-files>
git commit -m "feat: prepare verify release apk"
git push -u origin codex/verify-release-apk-YYYYMMDD
```

### 2. 手动触发 verify-release workflow

```bash
gh workflow run verify-release.yml \
  --ref codex/verify-release-apk-YYYYMMDD \
  -f tag=verify-release-apk-YYYYMMDD
```

也可以在 GitHub Actions 页面手动点 `Run workflow`。

### 3. 观察构建状态

```bash
gh run list --workflow verify-release.yml --limit 5
gh run watch <run-id>
```

### 4. 下载 artifact

```bash
mkdir -p .agents/artifacts/verify-release-apk
gh run download <run-id> \
  --name smartdash-verify-release-<run-number> \
  --dir .agents/artifacts/verify-release-apk
```

### 5. 安装到手机

```bash
adb install -r .agents/artifacts/verify-release-apk/*.apk
```

## 为什么优先用这个流程

- 能得到正式 `assembleRelease` 产物，和最终发布态更接近
- 不会像 `fastDevRelease` 一样偏离真实性能
- 不会在本机长时间占用 `R8 / lintVital / shrinkResources`
- 不会污染 GitHub Releases

## 与 release.yml 的边界

- `verify-release.yml`
  - 构建验证 APK
  - 上传 artifact
  - 不发 Release
- `release.yml`
  - 构建正式产物
  - 上传 artifact
  - 创建 GitHub Release

## 失败时的回退方案

按以下顺序回退：

1. 检查 `gh auth status`
2. 检查分支是否已推到远端
3. 检查 `verify-release.yml` 对当前分支是否可见
4. 若 GitHub Actions 不可用，再回退到本机 `.agents/scripts/build-release.sh`

## 相关文件

- `.github/workflows/verify-release.yml`
- `.github/workflows/release.yml`
- `.agents/workflows/github-release.md`
- `.agents/skills/github-release/SKILL.md`
