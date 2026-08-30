#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

if should_route_remote_build; then
  ensure_dirs
  ensure_remote_build_tools
  REMOTE_OUTPUT_FILE="$(mktemp)"
  REMOTE_TARGET="$(resolve_remote_ssh_target)"
  run_remote_script_capture "$REMOTE_TARGET" ".agents/scripts/build-fast-dev-release.sh" "$REMOTE_OUTPUT_FILE"

  REMOTE_FAST_DEV_RELEASE_APK_PATH="$(extract_output_var "$REMOTE_OUTPUT_FILE" "FAST_DEV_RELEASE_APK_PATH")"
  REMOTE_ARCHIVE_APK="$(extract_output_var "$REMOTE_OUTPUT_FILE" "ARCHIVE_APK")"
  REMOTE_BUILD_LOG="$(extract_output_var "$REMOTE_OUTPUT_FILE" "BUILD_LOG")"
  REMOTE_NORMAL_APK="$(extract_output_var "$REMOTE_OUTPUT_FILE" "FAST_NORMAL_APK")"
  REMOTE_VIVO_APK="$(extract_output_var "$REMOTE_OUTPUT_FILE" "FAST_VIVO_APK")"

  if [[ -z "$REMOTE_FAST_DEV_RELEASE_APK_PATH" || -z "$REMOTE_ARCHIVE_APK" || -z "$REMOTE_BUILD_LOG" || -z "$REMOTE_NORMAL_APK" || -z "$REMOTE_VIVO_APK" ]]; then
    echo "Remote build output was missing expected paths." >&2
    exit 1
  fi

  LOCAL_ARCHIVE_APK="$ARTIFACT_DIR/$(basename "$REMOTE_ARCHIVE_APK")"
  LOCAL_BUILD_LOG="$LOG_DIR/$(basename "$REMOTE_BUILD_LOG")"

  mkdir -p "$(dirname "$FAST_DEV_RELEASE_APK_PATH")"
  mkdir -p "$APP_BUILD_DIR/outputs/apk/vivo/fastDevRelease"

  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_NORMAL_APK" "$FAST_DEV_RELEASE_APK_PATH"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_VIVO_APK" "$APP_BUILD_DIR/outputs/apk/vivo/fastDevRelease/app-vivo-fastDevRelease.apk"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_ARCHIVE_APK" "$LOCAL_ARCHIVE_APK"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_BUILD_LOG" "$LOCAL_BUILD_LOG"

  SHA="$(shasum -a 256 "$FAST_DEV_RELEASE_APK_PATH" | awk '{print $1}')"
  SIZE="$(ls -lh "$FAST_DEV_RELEASE_APK_PATH" | awk '{print $5}')"

  echo "REMOTE_BUILD_HOST=$REMOTE_TARGET"
  echo "REMOTE_FAST_DEV_RELEASE_APK_PATH=$REMOTE_FAST_DEV_RELEASE_APK_PATH"
  echo "REMOTE_ARCHIVE_APK=$REMOTE_ARCHIVE_APK"
  echo "REMOTE_BUILD_LOG=$REMOTE_BUILD_LOG"
  echo "FAST_NORMAL_APK=$FAST_DEV_RELEASE_APK_PATH"
  echo "FAST_VIVO_APK=$APP_BUILD_DIR/outputs/apk/vivo/fastDevRelease/app-vivo-fastDevRelease.apk"
  echo "FAST_DEV_RELEASE_APK_PATH=$FAST_DEV_RELEASE_APK_PATH"
  echo "ARCHIVE_APK=$LOCAL_ARCHIVE_APK"
  echo "APK_SIZE=$SIZE"
  echo "APK_SHA256=$SHA"
  echo "BUILD_LOG=$LOCAL_BUILD_LOG"
  exit 0
fi

ensure_base_env
ensure_release_signing

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/build-fast-dev-release-$STAMP.log"
ARCHIVE_NORMAL_APK="$ARTIFACT_DIR/habe-fast-dev-release-$STAMP-normal.apk"
ARCHIVE_VIVO_APK="$ARTIFACT_DIR/habe-fast-dev-release-$STAMP-vivo.apk"

./gradlew --stop
run_gradle_logged_with_dex_retry \
  "$LOG_FILE" \
  fastDevRelease \
  :app:assembleFastDevRelease

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null
FAST_NORMAL_APK="$APP_BUILD_DIR/outputs/apk/normal/fastDevRelease/app-normal-fastDevRelease.apk"
FAST_VIVO_APK="$APP_BUILD_DIR/outputs/apk/vivo/fastDevRelease/app-vivo-fastDevRelease.apk"

test -f "$FAST_NORMAL_APK"
test -f "$FAST_VIVO_APK"
cp "$FAST_NORMAL_APK" "$ARCHIVE_NORMAL_APK"
cp "$FAST_VIVO_APK" "$ARCHIVE_VIVO_APK"

SHA="$(shasum -a 256 "$FAST_NORMAL_APK" | awk '{print $1}')"
SIZE="$(ls -lh "$FAST_NORMAL_APK" | awk '{print $5}')"

echo "FAST_NORMAL_APK=$FAST_NORMAL_APK"
echo "FAST_VIVO_APK=$FAST_VIVO_APK"
echo "FAST_DEV_RELEASE_APK_PATH=$FAST_NORMAL_APK"
echo "ARCHIVE_NORMAL_APK=$ARCHIVE_NORMAL_APK"
echo "ARCHIVE_VIVO_APK=$ARCHIVE_VIVO_APK"
echo "ARCHIVE_APK=$ARCHIVE_NORMAL_APK"
echo "APK_SIZE=$SIZE"
echo "APK_SHA256=$SHA"
echo "BUILD_LOG=$LOG_FILE"
