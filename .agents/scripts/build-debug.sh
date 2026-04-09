#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

if should_route_remote_build; then
  ensure_dirs
  ensure_remote_build_tools
  REMOTE_OUTPUT_FILE="$(mktemp)"
  REMOTE_TARGET="$(resolve_remote_ssh_target)"
  run_remote_script_capture "$REMOTE_TARGET" ".agents/scripts/build-debug.sh" "$REMOTE_OUTPUT_FILE"

  REMOTE_APK_PATH="$(extract_output_var "$REMOTE_OUTPUT_FILE" "APK_PATH")"
  REMOTE_ARCHIVE_APK="$(extract_output_var "$REMOTE_OUTPUT_FILE" "ARCHIVE_APK")"
  REMOTE_BUILD_LOG="$(extract_output_var "$REMOTE_OUTPUT_FILE" "BUILD_LOG")"

  if [[ -z "$REMOTE_APK_PATH" || -z "$REMOTE_ARCHIVE_APK" || -z "$REMOTE_BUILD_LOG" ]]; then
    echo "Remote build output was missing expected paths." >&2
    exit 1
  fi

  LOCAL_ARCHIVE_APK="$ARTIFACT_DIR/$(basename "$REMOTE_ARCHIVE_APK")"
  LOCAL_BUILD_LOG="$LOG_DIR/$(basename "$REMOTE_BUILD_LOG")"

  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_APK_PATH" "$APK_PATH"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_ARCHIVE_APK" "$LOCAL_ARCHIVE_APK"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_BUILD_LOG" "$LOCAL_BUILD_LOG"

  SHA="$(shasum -a 256 "$APK_PATH" | awk '{print $1}')"
  SIZE="$(ls -lh "$APK_PATH" | awk '{print $5}')"

  echo "REMOTE_BUILD_HOST=$REMOTE_TARGET"
  echo "REMOTE_APK_PATH=$REMOTE_APK_PATH"
  echo "REMOTE_ARCHIVE_APK=$REMOTE_ARCHIVE_APK"
  echo "REMOTE_BUILD_LOG=$REMOTE_BUILD_LOG"
  echo "APK_PATH=$APK_PATH"
  echo "ARCHIVE_APK=$LOCAL_ARCHIVE_APK"
  echo "APK_SIZE=$SIZE"
  echo "APK_SHA256=$SHA"
  echo "BUILD_LOG=$LOCAL_BUILD_LOG"
  exit 0
fi

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
