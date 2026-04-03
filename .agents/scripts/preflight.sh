#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_android_tools
require_cmd gh

cd "$PROJECT_ROOT"
warn_if_repo_contains_signing_files

echo "PROJECT_ROOT=$PROJECT_ROOT"
echo "ANDROID_HOME=$ANDROID_HOME"
echo "JAVA_HOME=$JAVA_HOME"
echo "ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT:-}"
echo "CURRENT_BRANCH=$(git branch --show-current)"
echo "REMOTE_URL=$(git remote get-url origin)"
echo "JAVA_VERSION=$(java -version 2>&1 | head -n 1)"
echo "ADB_VERSION=$(adb version | head -n 1)"
echo "SDKMANAGER_VERSION=$(sdkmanager --version 2>/dev/null || echo missing)"
if security find-generic-password -a "$USER" -s "habe.android.release.store.path" -w >/dev/null 2>&1; then
  echo "RELEASE_SIGNING=ready"
else
  echo "RELEASE_SIGNING=missing"
fi
if [[ -n "$(find_repo_signing_files)" ]]; then
  echo "SIGNING_FILES_IN_REPO=detected"
else
  echo "SIGNING_FILES_IN_REPO=clean"
fi
echo "GH_AUTH_STATUS:"
gh auth status
