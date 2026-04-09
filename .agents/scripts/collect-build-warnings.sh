#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
REPORT_DIR="${ROOT_DIR}/reports/build-warnings"
LINT_DIR="${ROOT_DIR}/reports/lint"
NORMALIZED_LOG="${REPORT_DIR}/normalized.txt"

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

run_gradle_capture() {
  local task="$1"
  local log_file="$2"
  local variant_dex_dir="${3:-}"
  local tmp_log
  tmp_log="$(mktemp)"

  set +e
  ./gradlew "${task}" --console plain 2>&1 | tee "${tmp_log}"
  local status=$?
  set -e

  if [[ ${status} -ne 0 && -n "${variant_dex_dir}" ]] && grep -q "Type .* is defined multiple times" "${tmp_log}"; then
    echo "Retrying ${task} after clearing ${variant_dex_dir} duplicate dex cache..."
    rm -rf "${variant_dex_dir}"
    set +e
    ./gradlew "${task}" --console plain 2>&1 | tee "${tmp_log}"
    status=$?
    set -e
  fi

  cp "${tmp_log}" "${log_file}"
  rm -f "${tmp_log}"
  return "${status}"
}

run_gradle_capture ":app:compileDebugKotlin" "${KOTLIN_LOG}"
run_gradle_capture ":app:assembleDebug" "${DEBUG_LOG}" "${ROOT_DIR}/app/build/intermediates/project_dex_archive/debug"
run_gradle_capture ":app:assembleRelease" "${RELEASE_LOG}" "${ROOT_DIR}/app/build/intermediates/project_dex_archive/release"
run_gradle_capture ":app:lintDebug" "${LINT_LOG}"

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

{
  grep -h -iE "warning|deprecated|obsolete" "${KOTLIN_LOG}" "${DEBUG_LOG}" "${RELEASE_LOG}" "${LINT_LOG}" || true
} \
  | sed "s#${ROOT_DIR}/##g" \
  | sed 's#/home/runner/work/SmartDash-by-Shawn-Rain/SmartDash-by-Shawn-Rain/##g' \
  | sed 's#file://##g' \
  | sed 's/[[:space:]]\+$//' \
  | awk 'NF' \
  | sort -u > "${NORMALIZED_LOG}"

echo "Saved warning snapshot to ${WARNINGS_LOG}"
echo "Saved normalized warning snapshot to ${NORMALIZED_LOG}"
