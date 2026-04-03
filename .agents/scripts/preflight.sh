#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_android_tools
require_cmd gh

cd "$PROJECT_ROOT"

echo "PROJECT_ROOT=$PROJECT_ROOT"
echo "ANDROID_HOME=$ANDROID_HOME"
echo "JAVA_HOME=$JAVA_HOME"
echo "CURRENT_BRANCH=$(git branch --show-current)"
echo "REMOTE_URL=$(git remote get-url origin)"
echo "JAVA_VERSION=$(java -version 2>&1 | head -n 1)"
echo "ADB_VERSION=$(adb version | head -n 1)"
echo "GH_AUTH_STATUS:"
gh auth status
