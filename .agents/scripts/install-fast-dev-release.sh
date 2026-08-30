#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_android_tools

BUILD_OUTPUT_FILE="$(mktemp)"
"$SCRIPT_DIR/build-fast-dev-release.sh" | tee "$BUILD_OUTPUT_FILE"

ADB_TARGET="${ADB_TARGET:-}"
if [[ -n "$ADB_TARGET" ]]; then
  ADB_PREFIX=(adb -s "$ADB_TARGET")
else
  ADB_PREFIX=(adb)
fi

"${ADB_PREFIX[@]}" get-state >/dev/null 2>&1 || {
  echo "No reachable adb device. Set ADB_TARGET if multiple devices are connected." >&2
  exit 1
}

if "${ADB_PREFIX[@]}" shell pm list packages | grep -q "package:com.vivo.bsptest"; then
  echo "[智能路由] 检测到真机已安装 com.vivo.bsptest，将安装 vivo 渠道的 fastDevRelease APK..."
  INSTALL_APK_PATH="$(extract_output_var "$BUILD_OUTPUT_FILE" "FAST_VIVO_APK")"
else
  echo "[智能路由] 未在真机检测到 com.vivo.bsptest，将安装普通渠道的 fastDevRelease APK..."
  INSTALL_APK_PATH="$(extract_output_var "$BUILD_OUTPUT_FILE" "FAST_NORMAL_APK")"
fi

if [[ -z "$INSTALL_APK_PATH" ]]; then
  echo "Build output did not include targeted fastDevRelease APK path." >&2
  exit 1
fi

"${ADB_PREFIX[@]}" install -r "$INSTALL_APK_PATH"

echo "INSTALLED_APK=$INSTALL_APK_PATH"
