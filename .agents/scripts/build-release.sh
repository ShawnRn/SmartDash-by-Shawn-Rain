#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_release_signing

if should_route_remote_build; then
  ensure_dirs
  ensure_remote_build_tools
  REMOTE_OUTPUT_FILE="$(mktemp)"
  REMOTE_TARGET="$(resolve_remote_ssh_target)"
  run_remote_script_capture "$REMOTE_TARGET" ".agents/scripts/build-release.sh" "$REMOTE_OUTPUT_FILE"

  REMOTE_RELEASE_APK_PATH="$(extract_output_var "$REMOTE_OUTPUT_FILE" "RELEASE_APK_PATH")"
  REMOTE_VIVO_RELEASE_APK_PATH="$(extract_output_var "$REMOTE_OUTPUT_FILE" "VIVO_RELEASE_APK_PATH")"
  REMOTE_ARCHIVE_NORMAL_APK="$(extract_output_var "$REMOTE_OUTPUT_FILE" "ARCHIVE_NORMAL_APK")"
  REMOTE_ARCHIVE_VIVO_APK="$(extract_output_var "$REMOTE_OUTPUT_FILE" "ARCHIVE_VIVO_APK")"
  REMOTE_BUILD_LOG="$(extract_output_var "$REMOTE_OUTPUT_FILE" "BUILD_LOG")"

  if [[ -z "$REMOTE_RELEASE_APK_PATH" || -z "$REMOTE_VIVO_RELEASE_APK_PATH" || -z "$REMOTE_ARCHIVE_NORMAL_APK" || -z "$REMOTE_ARCHIVE_VIVO_APK" || -z "$REMOTE_BUILD_LOG" ]]; then
    echo "Remote build output was missing expected paths." >&2
    exit 1
  fi

  LOCAL_ARCHIVE_NORMAL_APK="$ARTIFACT_DIR/$(basename "$REMOTE_ARCHIVE_NORMAL_APK")"
  LOCAL_ARCHIVE_VIVO_APK="$ARTIFACT_DIR/$(basename "$REMOTE_ARCHIVE_VIVO_APK")"
  LOCAL_BUILD_LOG="$LOG_DIR/$(basename "$REMOTE_BUILD_LOG")"

  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_RELEASE_APK_PATH" "$RELEASE_APK_PATH"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_VIVO_RELEASE_APK_PATH" "$VIVO_RELEASE_APK_PATH"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_ARCHIVE_NORMAL_APK" "$LOCAL_ARCHIVE_NORMAL_APK"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_ARCHIVE_VIVO_APK" "$LOCAL_ARCHIVE_VIVO_APK"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_BUILD_LOG" "$LOCAL_BUILD_LOG"

  SHA_NORMAL="$(shasum -a 256 "$RELEASE_APK_PATH" | awk '{print $1}')"
  SIZE_NORMAL="$(ls -lh "$RELEASE_APK_PATH" | awk '{print $5}')"
  SHA_VIVO="$(shasum -a 256 "$VIVO_RELEASE_APK_PATH" | awk '{print $1}')"
  SIZE_VIVO="$(ls -lh "$VIVO_RELEASE_APK_PATH" | awk '{print $5}')"

  echo "REMOTE_BUILD_HOST=$REMOTE_TARGET"
  echo "REMOTE_RELEASE_APK_PATH=$REMOTE_RELEASE_APK_PATH"
  echo "REMOTE_VIVO_RELEASE_APK_PATH=$REMOTE_VIVO_RELEASE_APK_PATH"
  echo "REMOTE_ARCHIVE_NORMAL_APK=$REMOTE_ARCHIVE_NORMAL_APK"
  echo "REMOTE_ARCHIVE_VIVO_APK=$REMOTE_ARCHIVE_VIVO_APK"
  echo "REMOTE_BUILD_LOG=$REMOTE_BUILD_LOG"
  echo "RELEASE_APK_PATH=$RELEASE_APK_PATH"
  echo "VIVO_RELEASE_APK_PATH=$VIVO_RELEASE_APK_PATH"
  echo "ARCHIVE_NORMAL_APK=$LOCAL_ARCHIVE_NORMAL_APK"
  echo "ARCHIVE_VIVO_APK=$LOCAL_ARCHIVE_VIVO_APK"
  echo "NORMAL_APK_SIZE=$SIZE_NORMAL"
  echo "NORMAL_APK_SHA256=$SHA_NORMAL"
  echo "VIVO_APK_SIZE=$SIZE_VIVO"
  echo "VIVO_APK_SHA256=$SHA_VIVO"
  echo "BUILD_LOG=$LOCAL_BUILD_LOG"

  # 复制普通版和 vivo 版到桌面并重命名
  DESKTOP_DIR="/Users/shawnrain/Desktop"
  if [[ -d "$DESKTOP_DIR" ]]; then
    echo "Copying built APKs to Desktop..."
    cp "$RELEASE_APK_PATH" "$DESKTOP_DIR/SmartDash-normal-release.apk"
    cp "$VIVO_RELEASE_APK_PATH" "$DESKTOP_DIR/SmartDash-vivo-release.apk"
    echo "Successfully copied to Desktop:"
    echo "  - $DESKTOP_DIR/SmartDash-normal-release.apk"
    echo "  - $DESKTOP_DIR/SmartDash-vivo-release.apk"
  else
    echo "Warning: Desktop directory not found at $DESKTOP_DIR"
  fi

  exit 0
fi

ensure_base_env
ensure_release_signing

echo "DEBUG: HABE_RELEASE_STORE_FILE=$HABE_RELEASE_STORE_FILE"
echo "DEBUG: HABE_RELEASE_KEY_ALIAS=$HABE_RELEASE_KEY_ALIAS"
echo "DEBUG: HABE_RELEASE_STORE_PASSWORD_LEN=${#HABE_RELEASE_STORE_PASSWORD}"
echo "DEBUG: HABE_RELEASE_KEY_PASSWORD_LEN=${#HABE_RELEASE_KEY_PASSWORD}"

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/build-release-$STAMP.log"
ARCHIVE_NORMAL_APK="$ARTIFACT_DIR/habe-release-$STAMP-normal.apk"
ARCHIVE_VIVO_APK="$ARTIFACT_DIR/habe-release-$STAMP-vivo.apk"

./gradlew --stop
run_gradle_logged_with_dex_retry "$LOG_FILE" release :app:assembleRelease

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null
test -f "$RELEASE_APK_PATH"
test -f "$VIVO_RELEASE_APK_PATH"
cp "$RELEASE_APK_PATH" "$ARCHIVE_NORMAL_APK"
cp "$VIVO_RELEASE_APK_PATH" "$ARCHIVE_VIVO_APK"

SHA_NORMAL="$(shasum -a 256 "$RELEASE_APK_PATH" | awk '{print $1}')"
SIZE_NORMAL="$(ls -lh "$RELEASE_APK_PATH" | awk '{print $5}')"
SHA_VIVO="$(shasum -a 256 "$VIVO_RELEASE_APK_PATH" | awk '{print $1}')"
SIZE_VIVO="$(ls -lh "$VIVO_RELEASE_APK_PATH" | awk '{print $5}')"

echo "RELEASE_APK_PATH=$RELEASE_APK_PATH"
echo "VIVO_RELEASE_APK_PATH=$VIVO_RELEASE_APK_PATH"
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
  echo "Copying built APKs to Desktop..."
  cp "$RELEASE_APK_PATH" "$DESKTOP_DIR/SmartDash-normal-release.apk"
  cp "$VIVO_RELEASE_APK_PATH" "$DESKTOP_DIR/SmartDash-vivo-release.apk"
  echo "Successfully copied to Desktop:"
  echo "  - $DESKTOP_DIR/SmartDash-normal-release.apk"
  echo "  - $DESKTOP_DIR/SmartDash-vivo-release.apk"
else
  echo "Warning: Desktop directory not found at $DESKTOP_DIR"
fi
