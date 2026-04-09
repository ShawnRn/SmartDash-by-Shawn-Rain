#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
BASELINE_FILE="${ROOT_DIR}/baseline/build-warnings.txt"
CURRENT_FILE="${ROOT_DIR}/reports/build-warnings/normalized.txt"
NEW_WARNINGS_FILE="${ROOT_DIR}/reports/build-warnings/new-warnings.txt"

cd "${ROOT_DIR}"

if [[ ! -f "${BASELINE_FILE}" ]]; then
  echo "Missing warning baseline: ${BASELINE_FILE}" >&2
  exit 1
fi

.agents/scripts/collect-build-warnings.sh

comm -13 <(sort "${BASELINE_FILE}") <(sort "${CURRENT_FILE}") > "${NEW_WARNINGS_FILE}" || true

if [[ -s "${NEW_WARNINGS_FILE}" ]]; then
  echo "New warnings detected against baseline:" >&2
  cat "${NEW_WARNINGS_FILE}" >&2
  exit 1
fi

echo "Warning baseline check passed."
