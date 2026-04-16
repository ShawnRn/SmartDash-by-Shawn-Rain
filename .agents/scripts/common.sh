#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$PROJECT_ROOT/.agents/logs"
ARTIFACT_DIR="$PROJECT_ROOT/.agents/artifacts"
JAVA_HOME_DEFAULT="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"

export ANDROID_HOME="${ANDROID_HOME:-/Users/shawnrain/android-sdk}"
export JAVA_HOME="${JAVA_HOME:-$JAVA_HOME_DEFAULT}"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
export SMARTDASH_BUILD_HOST="${SMARTDASH_BUILD_HOST:-shawn-rains-macbook-pro}"
export SMARTDASH_BUILD_HOST_IP="${SMARTDASH_BUILD_HOST_IP:-100.103.86.124}"
export SMARTDASH_REMOTE_PROJECT_ROOT="${SMARTDASH_REMOTE_PROJECT_ROOT:-$PROJECT_ROOT}"
export SMARTDASH_REMOTE_JAVA_HOME="${SMARTDASH_REMOTE_JAVA_HOME:-$JAVA_HOME_DEFAULT}"

APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
DEV_RELEASE_APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/devRelease/app-devRelease.apk"
FAST_DEV_RELEASE_APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/fastDevRelease/app-fastDevRelease.apk"
RELEASE_APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/release/app-release.apk"
DEFAULT_RELEASE_KEYSTORE_DIR="$HOME/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Secure/habe_android/signing"
DEFAULT_RELEASE_KEYSTORE_FILE="$DEFAULT_RELEASE_KEYSTORE_DIR/habe-release.jks"
SIGNING_FILE_PATTERN='(^|/)([^/]+\.(jks|keystore|p12)|signing\.properties|keystore\.properties|\.env(\..*)?)$'

timestamp() {
  date '+%Y%m%d-%H%M%S'
}

normalize_host_token() {
  printf '%s' "$1" \
    | tr '[:upper:]' '[:lower:]' \
    | sed -E 's/\.local$//; s/[^a-z0-9]+/-/g; s/^-+//; s/-+$//'
  printf '\n'
}

current_host_tokens() {
  {
    hostname 2>/dev/null || true
    scutil --get ComputerName 2>/dev/null || true
    scutil --get LocalHostName 2>/dev/null || true
  } | awk 'NF' | while IFS= read -r token; do
    normalize_host_token "$token"
  done | awk 'NF' | sort -u
}

is_primary_build_host() {
  local expected_host expected_ip token
  expected_host="$(normalize_host_token "$SMARTDASH_BUILD_HOST")"
  expected_ip="$(normalize_host_token "$SMARTDASH_BUILD_HOST_IP")"
  while IFS= read -r token; do
    if [[ "$token" == "$expected_host" || "$token" == "$expected_ip" ]]; then
      return 0
    fi
  done < <(current_host_tokens)
  return 1
}

should_route_remote_build() {
  if [[ "${SMARTDASH_FORCE_LOCAL_BUILD:-0}" == "1" ]]; then
    return 1
  fi
  if [[ "${SMARTDASH_REMOTE_BUILD_EXECUTION:-0}" == "1" ]]; then
    return 1
  fi
  ! is_primary_build_host
}

shell_quote() {
  printf '%q' "$1"
}

ssh_base_opts() {
  printf '%s\n' \
    "-o" "BatchMode=yes" \
    "-o" "ConnectTimeout=5" \
    "-o" "StrictHostKeyChecking=accept-new"
}

ensure_remote_build_tools() {
  ensure_base_env
  require_cmd ssh
  require_cmd rsync
  populate_release_signing_env_from_local_keychain
}

resolve_remote_ssh_target() {
  local explicit_target
  explicit_target="${SMARTDASH_REMOTE_SSH_TARGET:-}"
  if [[ -n "$explicit_target" ]]; then
    printf '%s\n' "$explicit_target"
    return 0
  fi

  local candidates=()
  if [[ -n "$SMARTDASH_BUILD_HOST" ]]; then
    candidates+=("$SMARTDASH_BUILD_HOST")
  fi
  if [[ -n "$SMARTDASH_BUILD_HOST_IP" && "$SMARTDASH_BUILD_HOST_IP" != "$SMARTDASH_BUILD_HOST" ]]; then
    candidates+=("$SMARTDASH_BUILD_HOST_IP")
  fi

  local candidate
  for candidate in "${candidates[@]}"; do
    if ssh $(ssh_base_opts) "$candidate" "exit 0" >/dev/null 2>&1; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done

  echo "Unable to reach the primary build host over SSH. Tried: ${candidates[*]}" >&2
  echo "Set SMARTDASH_REMOTE_SSH_TARGET explicitly after confirming Tailscale SSH is available." >&2
  exit 1
}

run_remote_script_capture() {
  local target="$1"
  local script_rel="$2"
  local output_file="$3"
  shift 3

  sync_workspace_to_remote "$target"

  local env_file="/tmp/sdash_build_${RANDOM}.env"
  local env_content=""
  
  # 收集签名环境变量
  if [[ -n "${HABE_RELEASE_STORE_FILE:-}" ]]; then env_content+="export HABE_RELEASE_STORE_FILE=$(shell_quote "$HABE_RELEASE_STORE_FILE")"$'\n'; fi
  if [[ -n "${HABE_RELEASE_STORE_PASSWORD:-}" ]]; then env_content+="export HABE_RELEASE_STORE_PASSWORD=$(shell_quote "$HABE_RELEASE_STORE_PASSWORD")"$'\n'; fi
  if [[ -n "${HABE_RELEASE_KEY_ALIAS:-}" ]]; then env_content+="export HABE_RELEASE_KEY_ALIAS=$(shell_quote "$HABE_RELEASE_KEY_ALIAS")"$'\n'; fi
  if [[ -n "${HABE_RELEASE_KEY_PASSWORD:-}" ]]; then env_content+="export HABE_RELEASE_KEY_PASSWORD=$(shell_quote "$HABE_RELEASE_KEY_PASSWORD")"$'\n'; fi
  
  # 收集构建环境
  env_content+="export SMARTDASH_REMOTE_BUILD_EXECUTION=1"$'\n'
  env_content+="export SMARTDASH_BUILD_HOST=$(shell_quote "$SMARTDASH_BUILD_HOST")"$'\n'
  env_content+="export SMARTDASH_BUILD_HOST_IP=$(shell_quote "$SMARTDASH_BUILD_HOST_IP")"$'\n'
  env_content+="export SMARTDASH_REMOTE_PROJECT_ROOT=$(shell_quote "$SMARTDASH_REMOTE_PROJECT_ROOT")"$'\n'
  env_content+="export SMARTDASH_REMOTE_JAVA_HOME=$(shell_quote "$SMARTDASH_REMOTE_JAVA_HOME")"$'\n'
  env_content+="export JAVA_HOME=$(shell_quote "$SMARTDASH_REMOTE_JAVA_HOME")"$'\n'
  env_content+="export ANDROID_HOME=$(shell_quote "$ANDROID_HOME")"$'\n'

  # 将环境变量写入远端文件
  ssh $(ssh_base_opts) "$target" "cat <<EOF > $(shell_quote "$env_file")
$env_content
EOF"

  local remote_cmd
  remote_cmd=$(
    cat <<EOF
source $(shell_quote "$env_file") && \
rm -f $(shell_quote "$env_file") && \
cd $(shell_quote "$SMARTDASH_REMOTE_PROJECT_ROOT") && \
bash $(shell_quote "$script_rel")
EOF
  )

  ssh $(ssh_base_opts) "$target" "bash -lc $(shell_quote "$remote_cmd")" 2>&1 | tee "$output_file"
  local status=${PIPESTATUS[0]}
  return "$status"
}

populate_release_signing_env_from_local_keychain() {
  if [[ -n "${HABE_RELEASE_STORE_FILE:-}" && -n "${HABE_RELEASE_STORE_PASSWORD:-}" && -n "${HABE_RELEASE_KEY_ALIAS:-}" && -n "${HABE_RELEASE_KEY_PASSWORD:-}" ]]; then
    return 0
  fi

  local store_file store_password key_alias key_password
  store_file="$(keychain_get "habe.android.release.store.path" || true)"
  store_password="$(keychain_get "habe.android.release.store.password" || true)"
  key_alias="$(keychain_get "habe.android.release.key.alias" || true)"
  key_password="$(keychain_get "habe.android.release.key.password" || true)"

  if [[ -n "$store_file" && -f "$store_file" && -n "$store_password" && -n "$key_alias" && -n "$key_password" ]]; then
    export HABE_RELEASE_STORE_FILE="$store_file"
    export HABE_RELEASE_STORE_PASSWORD="$store_password"
    export HABE_RELEASE_KEY_ALIAS="$key_alias"
    export HABE_RELEASE_KEY_PASSWORD="$key_password"
  fi
}

sync_workspace_to_remote() {
  local target="$1"

  ensure_dirs
  require_cmd rsync

  local remote_root_quoted
  remote_root_quoted="$(shell_quote "$SMARTDASH_REMOTE_PROJECT_ROOT")"
  local remote_rsync_path
  remote_rsync_path="$(shell_quote "$SMARTDASH_REMOTE_PROJECT_ROOT/")"

  ssh $(ssh_base_opts) "$target" "mkdir -p $remote_root_quoted"

  rsync -az --delete \
    -e "ssh -o BatchMode=yes -o ConnectTimeout=5 -o StrictHostKeyChecking=accept-new" \
    --exclude '.git/' \
    --exclude '.gradle/' \
    --exclude 'build/' \
    --exclude 'app/build/' \
    --exclude '.agents/logs/' \
    --exclude '.agents/artifacts/' \
    --exclude 'zhike_source/' \
    --exclude 'habe_miniprogram/' \
    --exclude 'tools/unveilr/' \
    "$PROJECT_ROOT/" "$target:$remote_rsync_path"

  echo "REMOTE_SYNC_HOST=$target"
  echo "REMOTE_SYNC_ROOT=$SMARTDASH_REMOTE_PROJECT_ROOT"
}

extract_output_var() {
  local output_file="$1"
  local key="$2"
  sed -n "s/^${key}=//p" "$output_file" | tail -n 1
}

copy_remote_file_to_local() {
  local target="$1"
  local remote_path="$2"
  local local_path="$3"

  mkdir -p "$(dirname "$local_path")"
  ssh $(ssh_base_opts) "$target" "cat $(shell_quote "$remote_path")" > "$local_path"
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

  if [[ -n "${HABE_RELEASE_STORE_FILE:-}" && -n "${HABE_RELEASE_STORE_PASSWORD:-}" && -n "${HABE_RELEASE_KEY_ALIAS:-}" && -n "${HABE_RELEASE_KEY_PASSWORD:-}" ]]; then
    if [[ ! -f "$HABE_RELEASE_STORE_FILE" ]]; then
      echo "Release keystore file is missing: $HABE_RELEASE_STORE_FILE" >&2
      exit 1
    fi
    export HABE_RELEASE_STORE_FILE
    export HABE_RELEASE_STORE_PASSWORD
    export HABE_RELEASE_KEY_ALIAS
    export HABE_RELEASE_KEY_PASSWORD
    return 0
  fi

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
    fastDevRelease) task_suffix="FastDevRelease" ;;
    *) task_suffix="$variant" ;;
  esac
  rm -rf \
    "$PROJECT_ROOT/app/build/intermediates/project_dex_archive/$variant" \
    "$PROJECT_ROOT/app/build/intermediates/dex_archive_input_jar_hashes/$variant" \
    "$PROJECT_ROOT/app/build/intermediates/mergeDex$task_suffix" \
    "$PROJECT_ROOT/app/build/intermediates/dex_builder/$variant" \
    "$PROJECT_ROOT/app/build/tmp/kotlin-classes/$variant"
}

variant_task_suffix() {
  local variant="$1"
  case "$variant" in
    debug) echo "Debug" ;;
    release) echo "Release" ;;
    devRelease) echo "DevRelease" ;;
    fastDevRelease) echo "FastDevRelease" ;;
    *) echo "$variant" ;;
  esac
}

sanitize_variant_split_artifacts() {
  local variant="$1"
  local task_suffix
  task_suffix="$(variant_task_suffix "$variant")"
  local removed=0

  while IFS= read -r -d '' file; do
    case "$file" in
      *"/$variant/"*|*"/mergeExtDex$task_suffix/"*)
        rm -f "$file"
        removed=$((removed + 1))
        ;;
    esac
  done < <(
    find "$PROJECT_ROOT/app/build/intermediates" \
      -type f \
      \( -name "* *.dex" -o -name "* *.jar" \) \
      -print0 2>/dev/null
  )

  if [[ "$removed" -gt 0 ]]; then
    echo "[pre-clean] Removed $removed stale split dex/jar artifacts for variant '$variant'."
  fi
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

  sanitize_variant_split_artifacts "$variant"

  if [[ "${HABE_FORCE_CLEAN:-0}" == "1" ]]; then
    echo "[pre-clean] HABE_FORCE_CLEAN=1 -> running :app:clean before build." | tee "$log_file"
    RUN_GRADLE_LOG_MODE=append run_gradle_logged "$log_file" :app:clean || return 1
    if RUN_GRADLE_LOG_MODE=append run_gradle_logged "$log_file" "$@"; then
      return 0
    fi
  else
    if run_gradle_logged "$log_file" "$@"; then
      return 0
    fi
  fi

  if ! log_has_retryable_dex_issue "$log_file"; then
    return 1
  fi

  {
    echo
    echo "[dex-retry] Detected stale dex intermediates for variant '$variant'."
    echo "[dex-retry] Cleaning project_dex_archive and related caches, then retrying once..."
  } | tee -a "$log_file"

  sanitize_variant_split_artifacts "$variant"
  clean_variant_dex_intermediates "$variant"
  RUN_GRADLE_LOG_MODE=append run_gradle_logged "$log_file" "$@"
}
