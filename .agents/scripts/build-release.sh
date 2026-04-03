#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_base_env
ensure_release_signing

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/build-release-$STAMP.log"
ARCHIVE_APK="$ARTIFACT_DIR/habe-release-$STAMP.apk"

run_gradle_logged_with_dex_retry "$LOG_FILE" release :app:assembleRelease

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null
test -f "$RELEASE_APK_PATH"
cp "$RELEASE_APK_PATH" "$ARCHIVE_APK"

SHA="$(shasum -a 256 "$RELEASE_APK_PATH" | awk '{print $1}')"
SIZE="$(ls -lh "$RELEASE_APK_PATH" | awk '{print $5}')"

echo "RELEASE_APK_PATH=$RELEASE_APK_PATH"
echo "ARCHIVE_APK=$ARCHIVE_APK"
echo "APK_SIZE=$SIZE"
echo "APK_SHA256=$SHA"
echo "BUILD_LOG=$LOG_FILE"
