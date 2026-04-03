#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_base_env
ensure_release_signing

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/build-fast-dev-release-$STAMP.log"
ARCHIVE_APK="$ARTIFACT_DIR/habe-fast-dev-release-$STAMP.apk"

run_gradle_logged_with_dex_retry \
  "$LOG_FILE" \
  devRelease \
  :app:assembleDevRelease \
  -x :app:generateDevReleaseLintVitalReportModel \
  -x :app:lintVitalAnalyzeDevRelease \
  -x :app:lintVitalReportDevRelease \
  -x :app:lintVitalDevRelease

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null
test -f "$DEV_RELEASE_APK_PATH"
cp "$DEV_RELEASE_APK_PATH" "$ARCHIVE_APK"

SHA="$(shasum -a 256 "$DEV_RELEASE_APK_PATH" | awk '{print $1}')"
SIZE="$(ls -lh "$DEV_RELEASE_APK_PATH" | awk '{print $5}')"

echo "DEV_RELEASE_APK_PATH=$DEV_RELEASE_APK_PATH"
echo "ARCHIVE_APK=$ARCHIVE_APK"
echo "APK_SIZE=$SIZE"
echo "APK_SHA256=$SHA"
echo "BUILD_LOG=$LOG_FILE"
