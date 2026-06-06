#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

if should_route_remote_build; then
  ensure_dirs
  ensure_remote_build_tools
  REMOTE_OUTPUT_FILE="$(mktemp)"
  REMOTE_TARGET="$(resolve_remote_ssh_target)"
  run_remote_script_capture "$REMOTE_TARGET" ".agents/scripts/build-dev-release.sh" "$REMOTE_OUTPUT_FILE"

  REMOTE_DEV_RELEASE_APK_PATH="$(extract_output_var "$REMOTE_OUTPUT_FILE" "DEV_RELEASE_APK_PATH")"
  REMOTE_ARCHIVE_APK="$(extract_output_var "$REMOTE_OUTPUT_FILE" "ARCHIVE_APK")"
  REMOTE_BUILD_LOG="$(extract_output_var "$REMOTE_OUTPUT_FILE" "BUILD_LOG")"

  if [[ -z "$REMOTE_DEV_RELEASE_APK_PATH" || -z "$REMOTE_ARCHIVE_APK" || -z "$REMOTE_BUILD_LOG" ]]; then
    echo "Remote build output was missing expected paths." >&2
    exit 1
  fi

  LOCAL_ARCHIVE_APK="$ARTIFACT_DIR/$(basename "$REMOTE_ARCHIVE_APK")"
  LOCAL_BUILD_LOG="$LOG_DIR/$(basename "$REMOTE_BUILD_LOG")"

  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_DEV_RELEASE_APK_PATH" "$DEV_RELEASE_APK_PATH"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_ARCHIVE_APK" "$LOCAL_ARCHIVE_APK"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_BUILD_LOG" "$LOCAL_BUILD_LOG"

  SHA="$(shasum -a 256 "$DEV_RELEASE_APK_PATH" | awk '{print $1}')"
  SIZE="$(ls -lh "$DEV_RELEASE_APK_PATH" | awk '{print $5}')"

  echo "REMOTE_BUILD_HOST=$REMOTE_TARGET"
  echo "REMOTE_DEV_RELEASE_APK_PATH=$REMOTE_DEV_RELEASE_APK_PATH"
  echo "REMOTE_ARCHIVE_APK=$REMOTE_ARCHIVE_APK"
  echo "REMOTE_BUILD_LOG=$REMOTE_BUILD_LOG"
  echo "DEV_RELEASE_APK_PATH=$DEV_RELEASE_APK_PATH"
  echo "ARCHIVE_APK=$LOCAL_ARCHIVE_APK"
  echo "APK_SIZE=$SIZE"
  echo "APK_SHA256=$SHA"
  echo "BUILD_LOG=$LOCAL_BUILD_LOG"
  exit 0
fi

ensure_base_env
ensure_release_signing

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/build-dev-release-$STAMP.log"
ARCHIVE_NORMAL_APK="$ARTIFACT_DIR/habe-dev-release-$STAMP-normal.apk"
ARCHIVE_VIVO_APK="$ARTIFACT_DIR/habe-dev-release-$STAMP-vivo.apk"

./gradlew --stop
run_gradle_logged_with_dex_retry "$LOG_FILE" devRelease :app:assembleDevRelease

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null

DEV_NORMAL_APK="$PROJECT_ROOT/app/build/outputs/apk/normal/devRelease/app-normal-devRelease.apk"
DEV_VIVO_APK="$PROJECT_ROOT/app/build/outputs/apk/vivo/devRelease/app-vivo-devRelease.apk"

test -f "$DEV_NORMAL_APK"
test -f "$DEV_VIVO_APK"

cp "$DEV_NORMAL_APK" "$ARCHIVE_NORMAL_APK"
cp "$DEV_VIVO_APK" "$ARCHIVE_VIVO_APK"

SHA_NORMAL="$(shasum -a 256 "$DEV_NORMAL_APK" | awk '{print $1}')"
SIZE_NORMAL="$(ls -lh "$DEV_NORMAL_APK" | awk '{print $5}')"
SHA_VIVO="$(shasum -a 256 "$DEV_VIVO_APK" | awk '{print $1}')"
SIZE_VIVO="$(ls -lh "$DEV_VIVO_APK" | awk '{print $5}')"

echo "DEV_NORMAL_APK=$DEV_NORMAL_APK"
echo "DEV_VIVO_APK=$DEV_VIVO_APK"
echo "ARCHIVE_NORMAL_APK=$ARCHIVE_NORMAL_APK"
echo "ARCHIVE_VIVO_APK=$ARCHIVE_VIVO_APK"
echo "NORMAL_APK_SIZE=$SIZE_NORMAL"
echo "NORMAL_APK_SHA256=$SHA_NORMAL"
echo "VIVO_APK_SIZE=$SIZE_VIVO"
echo "VIVO_APK_SHA256=$SHA_VIVO"
echo "BUILD_LOG=$LOG_FILE"

# 复制普通版和 vivo 版到桌面并重命名
DESKTOP_DIR="/Users/shawnrain/Desktop"
if [[ -d "$DESKTOP_DIR" ]]; then
  echo "Copying built devRelease APKs to Desktop..."
  cp "$DEV_NORMAL_APK" "$DESKTOP_DIR/SmartDash-normal-devRelease.apk"
  cp "$DEV_VIVO_APK" "$DESKTOP_DIR/SmartDash-vivo-devRelease.apk"
  echo "Successfully copied to Desktop:"
  echo "  - $DESKTOP_DIR/SmartDash-normal-devRelease.apk"
  echo "  - $DESKTOP_DIR/SmartDash-vivo-devRelease.apk"
else
  echo "Warning: Desktop directory not found at $DESKTOP_DIR"
fi
