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
DEV_RELEASE_APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/devRelease/app-devRelease.apk"
RELEASE_APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/release/app-release.apk"
DEFAULT_RELEASE_KEYSTORE_DIR="$HOME/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Secure/habe_android/signing"
DEFAULT_RELEASE_KEYSTORE_FILE="$DEFAULT_RELEASE_KEYSTORE_DIR/habe-release.jks"
SIGNING_FILE_PATTERN='(^|/)([^/]+\.(jks|keystore|p12)|signing\.properties|keystore\.properties|\.env(\..*)?)$'

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

print_install_hint() {
  cat >&2 <<'EOF'
Environment is incomplete. On a new Mac, run:
  .agents/scripts/bootstrap-mac.sh
EOF
}

ensure_base_env() {
  ensure_dirs
  require_cmd java || true
  if ! command -v java >/dev/null 2>&1; then
    print_install_hint
    exit 1
  fi
  require_cmd shasum
  require_cmd git
}

ensure_android_tools() {
  ensure_base_env
  if [[ ! -d "$ANDROID_HOME/platform-tools" ]] || [[ ! -d "$ANDROID_HOME/platforms" ]]; then
    echo "ANDROID_HOME is missing required SDK packages: $ANDROID_HOME" >&2
    print_install_hint
    exit 1
  fi
  if ! command -v adb >/dev/null 2>&1; then
    echo "Missing required command: adb" >&2
    print_install_hint
    exit 1
  fi
}

keychain_get() {
  security find-generic-password -a "$USER" -s "$1" -w 2>/dev/null
}

ensure_release_signing() {
  require_cmd security

  local store_file
  local store_password
  local key_alias
  local key_password

  store_file="$(keychain_get "habe.android.release.store.path" || true)"
  store_password="$(keychain_get "habe.android.release.store.password" || true)"
  key_alias="$(keychain_get "habe.android.release.key.alias" || true)"
  key_password="$(keychain_get "habe.android.release.key.password" || true)"

  if [[ -z "$store_file" || -z "$store_password" || -z "$key_alias" || -z "$key_password" ]]; then
    echo "Release signing is not configured in Keychain." >&2
    echo "Run .agents/scripts/setup-release-signing.sh on a Mac that has access to the iCloud keystore file and synced Keychain items." >&2
    exit 1
  fi

  if [[ ! -f "$store_file" ]]; then
    echo "Release keystore file is missing: $store_file" >&2
    echo "Check iCloud Drive sync status for the signing directory." >&2
    exit 1
  fi

  export HABE_RELEASE_STORE_FILE="$store_file"
  export HABE_RELEASE_STORE_PASSWORD="$store_password"
  export HABE_RELEASE_KEY_ALIAS="$key_alias"
  export HABE_RELEASE_KEY_PASSWORD="$key_password"
}

find_repo_signing_files() {
  (
    cd "$PROJECT_ROOT"
    {
      git ls-files
      git ls-files --others --exclude-standard
    } | sort -u | grep -E "$SIGNING_FILE_PATTERN" || true
  )
}

find_staged_signing_files() {
  (
    cd "$PROJECT_ROOT"
    git diff --cached --name-only --diff-filter=ACMR | grep -E "$SIGNING_FILE_PATTERN" || true
  )
}

warn_if_repo_contains_signing_files() {
  local found
  found="$(find_repo_signing_files)"
  if [[ -n "$found" ]]; then
    echo "WARNING: signing-related files exist inside the repository working tree:" >&2
    echo "$found" >&2
    echo "Move them out of the repo and keep them only in iCloud Drive / Keychain." >&2
  fi
}

fail_if_staged_signing_files() {
  local staged
  staged="$(find_staged_signing_files)"
  if [[ -n "$staged" ]]; then
    echo "Refusing to continue because signing-related files are staged for Git:" >&2
    echo "$staged" >&2
    echo "Remove them from the index and keep signing materials only in iCloud Drive / Keychain." >&2
    exit 1
  fi
}

run_gradle_logged() {
  local log_file="$1"
  shift
  local tee_args=()
  if [[ "${RUN_GRADLE_LOG_MODE:-overwrite}" == "append" ]]; then
    tee_args=(-a)
  fi
  (
    cd "$PROJECT_ROOT"
    ./gradlew "$@" --console plain
  ) 2>&1 | {
    if [[ ${#tee_args[@]} -gt 0 ]]; then
      tee "${tee_args[@]}" "$log_file"
    else
      tee "$log_file"
    fi
  }
  local status=${PIPESTATUS[0]}
  return "$status"
}

clean_variant_dex_intermediates() {
  local variant="$1"
  local task_suffix
  case "$variant" in
    debug) task_suffix="Debug" ;;
    release) task_suffix="Release" ;;
    devRelease) task_suffix="DevRelease" ;;
    *) task_suffix="$variant" ;;
  esac
  rm -rf \
    "$PROJECT_ROOT/app/build/intermediates/project_dex_archive/$variant" \
    "$PROJECT_ROOT/app/build/intermediates/dex_archive_input_jar_hashes/$variant" \
    "$PROJECT_ROOT/app/build/intermediates/mergeDex$task_suffix" \
    "$PROJECT_ROOT/app/build/intermediates/dex_builder/$variant" \
    "$PROJECT_ROOT/app/build/tmp/kotlin-classes/$variant"
}

log_has_retryable_dex_issue() {
  local log_file="$1"
  grep -Eq \
    'DexArchiveMergerException|defined multiple times|project_dex_archive/.+/dexBuilder.+/out/.+ 2\.jar' \
    "$log_file"
}

run_gradle_logged_with_dex_retry() {
  local log_file="$1"
  local variant="$2"
  shift 2

  if run_gradle_logged "$log_file" "$@"; then
    return 0
  fi

  if ! log_has_retryable_dex_issue "$log_file"; then
    return 1
  fi

  {
    echo
    echo "[dex-retry] Detected stale dex intermediates for variant '$variant'."
    echo "[dex-retry] Cleaning project_dex_archive and related caches, then retrying once..."
  } | tee -a "$log_file"

  clean_variant_dex_intermediates "$variant"
  RUN_GRADLE_LOG_MODE=append run_gradle_logged "$log_file" "$@"
}
