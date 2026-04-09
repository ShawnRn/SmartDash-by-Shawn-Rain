#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_android_tools

BUILD_OUTPUT_FILE="$(mktemp)"
"$SCRIPT_DIR/build-dev-release.sh" | tee "$BUILD_OUTPUT_FILE"
INSTALL_APK_PATH="$(extract_output_var "$BUILD_OUTPUT_FILE" "DEV_RELEASE_APK_PATH")"

if [[ -z "$INSTALL_APK_PATH" ]]; then
  echo "Build output did not include DEV_RELEASE_APK_PATH." >&2
  exit 1
fi

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

"${ADB_PREFIX[@]}" install -r "$INSTALL_APK_PATH"

echo "INSTALLED_APK=$INSTALL_APK_PATH"
