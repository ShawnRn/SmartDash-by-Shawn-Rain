#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
REPORT_DIR="${ROOT_DIR}/reports/build-warnings"
LINT_DIR="${ROOT_DIR}/reports/lint"

mkdir -p "${REPORT_DIR}" "${LINT_DIR}"

JAVA_HOME_DEFAULT="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export JAVA_HOME="${JAVA_HOME:-${JAVA_HOME_DEFAULT}}"

cd "${ROOT_DIR}"

DEBUG_LOG="${REPORT_DIR}/assemble-debug.txt"
RELEASE_LOG="${REPORT_DIR}/assemble-release.txt"
KOTLIN_LOG="${REPORT_DIR}/compile-debug-kotlin.txt"
WARNINGS_LOG="${REPORT_DIR}/current.txt"
LINT_LOG="${LINT_DIR}/lint-debug.txt"

echo "Using JAVA_HOME=${JAVA_HOME}"

./gradlew :app:compileDebugKotlin --console plain 2>&1 | tee "${KOTLIN_LOG}"
./gradlew :app:assembleDebug --console plain 2>&1 | tee "${DEBUG_LOG}"
./gradlew :app:assembleRelease --console plain 2>&1 | tee "${RELEASE_LOG}"
./gradlew :app:lintDebug --console plain 2>&1 | tee "${LINT_LOG}"

{
  echo "# SmartDash Build Warning Snapshot"
  echo
  echo "## compileDebugKotlin"
  grep -iE "warning|deprecated|obsolete" "${KOTLIN_LOG}" || true
  echo
  echo "## assembleDebug"
  grep -iE "warning|deprecated|obsolete" "${DEBUG_LOG}" || true
  echo
  echo "## assembleRelease"
  grep -iE "warning|deprecated|obsolete" "${RELEASE_LOG}" || true
  echo
  echo "## lintDebug"
  grep -iE "warning|deprecated|obsolete|lint" "${LINT_LOG}" || true
} > "${WARNINGS_LOG}"

echo "Saved warning snapshot to ${WARNINGS_LOG}"
