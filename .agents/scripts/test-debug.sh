#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_base_env

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/test-debug-$STAMP.log"

run_gradle_logged "$LOG_FILE" :app:testDebugUnitTest

grep "BUILD SUCCESSFUL" "$LOG_FILE" >/dev/null

if grep -q "NO-SOURCE" "$LOG_FILE"; then
  echo "TEST_NOTE=No local unit tests were present (NO-SOURCE)"
fi

echo "TEST_LOG=$LOG_FILE"
