#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$PROJECT_ROOT/.agents/logs"
ARTIFACT_DIR="$PROJECT_ROOT/.agents/artifacts"

export ANDROID_HOME="${ANDROID_HOME:-/Users/shawnrain/android-sdk}"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"

timestamp() {
  date '+%Y%m%d-%H%M%S'
}

ensure_dirs() {
  mkdir -p "$LOG_DIR" "$ARTIFACT_DIR"
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

ensure_base_env() {
  ensure_dirs
  require_cmd java
  require_cmd shasum
  require_cmd git
}

ensure_android_tools() {
  ensure_base_env
  require_cmd adb
}

run_gradle_logged() {
  local log_file="$1"
  shift
  (
    cd "$PROJECT_ROOT"
    ./gradlew "$@" --no-daemon --console plain
  ) 2>&1 | tee "$log_file"
}
