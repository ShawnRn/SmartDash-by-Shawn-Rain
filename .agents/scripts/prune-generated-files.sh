#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_dirs

removed_bytes=0
generated_dirs=(
  "$PROJECT_ROOT/app/build"
  "$PROJECT_ROOT/build"
  "$PROJECT_ROOT/.gradle"
  "$PROJECT_ROOT/.kotlin"
)

for generated_dir in "${generated_dirs[@]}"; do
  case "$generated_dir" in
    "$PROJECT_ROOT"/*) ;;
    *)
      echo "Refusing to remove path outside project: $generated_dir" >&2
      exit 1
      ;;
  esac

  if [[ -d "$generated_dir" ]]; then
    dir_bytes="$(du -sk "$generated_dir" | awk '{print $1 * 1024}')"
    removed_bytes=$((removed_bytes + dir_bytes))
    rm -rf -- "$generated_dir"
    echo "REMOVED_GENERATED_DIR=$generated_dir"
  fi
done

remaining_apks="$(find "$ARTIFACT_DIR" -maxdepth 1 -type f -name '*.apk' | wc -l | tr -d ' ')"
remaining_logs="$(find "$LOG_DIR" -maxdepth 1 -type f -name '*.log' | wc -l | tr -d ' ')"

echo "REMOVED_GENERATED_BYTES=$removed_bytes"
echo "RETAINED_APKS=$remaining_apks"
echo "RETAINED_LOGS=$remaining_logs"
echo "GRADLE_BUILD_ROOT=$SMARTDASH_GRADLE_BUILD_ROOT"
