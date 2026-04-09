#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

if should_route_remote_build; then
  ensure_dirs
  ensure_remote_build_tools
  REMOTE_OUTPUT_FILE="$(mktemp)"
  REMOTE_TARGET="$(resolve_remote_ssh_target)"
  run_remote_script_capture "$REMOTE_TARGET" ".agents/scripts/compile-debug-kotlin.sh" "$REMOTE_OUTPUT_FILE"

  REMOTE_COMPILE_LOG="$(extract_output_var "$REMOTE_OUTPUT_FILE" "COMPILE_LOG")"
  if [[ -z "$REMOTE_COMPILE_LOG" ]]; then
    echo "Remote compile output was missing COMPILE_LOG." >&2
    exit 1
  fi

  LOCAL_COMPILE_LOG="$LOG_DIR/$(basename "$REMOTE_COMPILE_LOG")"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_COMPILE_LOG" "$LOCAL_COMPILE_LOG"

  echo "REMOTE_BUILD_HOST=$REMOTE_TARGET"
  echo "REMOTE_COMPILE_LOG=$REMOTE_COMPILE_LOG"
  echo "COMPILE_LOG=$LOCAL_COMPILE_LOG"
  exit 0
fi

ensure_base_env

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/compile-debug-kotlin-$STAMP.log"

run_gradle_logged "$LOG_FILE" :app:compileDebugKotlin

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null

echo "COMPILE_LOG=$LOG_FILE"
