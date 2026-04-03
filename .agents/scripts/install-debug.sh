#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_android_tools

"$SCRIPT_DIR/build-debug.sh" >/tmp/habe-install-build.out

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

"${ADB_PREFIX[@]}" install -r "$APK_PATH"

echo "INSTALLED_APK=$APK_PATH"
