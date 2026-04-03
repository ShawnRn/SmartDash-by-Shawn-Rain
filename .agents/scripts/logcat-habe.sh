#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_android_tools

ADB_TARGET="${ADB_TARGET:-}"
if [[ -n "$ADB_TARGET" ]]; then
  ADB_PREFIX=(adb -s "$ADB_TARGET")
else
  ADB_PREFIX=(adb)
fi

if [[ "${1:-}" == "--clear" ]]; then
  "${ADB_PREFIX[@]}" logcat -c
  shift || true
fi

STAMP="$(timestamp)"
LOG_FILE="$LOG_DIR/logcat-habe-$STAMP.log"

echo "Streaming logcat to $LOG_FILE"
"${ADB_PREFIX[@]}" logcat -v time \
  BleManager:V \
  ProtocolParser:V \
  ZhikeProtocol:V \
  MainViewModel:D \
  AppLogger:D \
  OverlayHud:D \
  HudOverlayService:D \
  *:S | tee "$LOG_FILE"
