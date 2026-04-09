#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

if should_route_remote_build; then
  ensure_dirs
  ensure_remote_build_tools
  REMOTE_OUTPUT_FILE="$(mktemp)"
  REMOTE_TARGET="$(resolve_remote_ssh_target)"
  run_remote_script_capture "$REMOTE_TARGET" ".agents/scripts/test-debug.sh" "$REMOTE_OUTPUT_FILE"

  REMOTE_TEST_LOG="$(extract_output_var "$REMOTE_OUTPUT_FILE" "TEST_LOG")"
  if [[ -z "$REMOTE_TEST_LOG" ]]; then
    echo "Remote test output was missing TEST_LOG." >&2
    exit 1
  fi

  LOCAL_TEST_LOG="$LOG_DIR/$(basename "$REMOTE_TEST_LOG")"
  copy_remote_file_to_local "$REMOTE_TARGET" "$REMOTE_TEST_LOG" "$LOCAL_TEST_LOG"

  if grep -q '^TEST_NOTE=' "$REMOTE_OUTPUT_FILE"; then
    extract_output_var "$REMOTE_OUTPUT_FILE" "TEST_NOTE" | sed 's/^/TEST_NOTE=/'
  fi

  echo "REMOTE_BUILD_HOST=$REMOTE_TARGET"
  echo "REMOTE_TEST_LOG=$REMOTE_TEST_LOG"
  echo "TEST_LOG=$LOCAL_TEST_LOG"
  exit 0
fi

ensure_base_env

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/test-debug-$STAMP.log"

run_gradle_logged "$LOG_FILE" :app:testDebugUnitTest

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null

if grep -q "NO-SOURCE" "$LOG_FILE"; then
  echo "TEST_NOTE=No local unit tests were present (NO-SOURCE)"
fi

echo "TEST_LOG=$LOG_FILE"
