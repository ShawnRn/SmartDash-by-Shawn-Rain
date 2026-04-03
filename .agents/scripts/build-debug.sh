#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_base_env

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/build-debug-$STAMP.log"
ARCHIVE_APK="$ARTIFACT_DIR/habe-debug-$STAMP.apk"

run_gradle_logged_with_dex_retry "$LOG_FILE" debug :app:assembleDebug

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null
test -f "$APK_PATH"
cp "$APK_PATH" "$ARCHIVE_APK"

SHA="$(shasum -a 256 "$APK_PATH" | awk '{print $1}')"
SIZE="$(ls -lh "$APK_PATH" | awk '{print $5}')"

echo "APK_PATH=$APK_PATH"
echo "ARCHIVE_APK=$ARCHIVE_APK"
echo "APK_SIZE=$SIZE"
echo "APK_SHA256=$SHA"
echo "BUILD_LOG=$LOG_FILE"
