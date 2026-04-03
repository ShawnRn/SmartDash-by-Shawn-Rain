#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ensure_base_env
require_cmd gh

COMMIT_MESSAGE="${1:-chore: update AGENTS and agent workflows}"
BRANCH="$(cd "$PROJECT_ROOT" && git branch --show-current)"

"$SCRIPT_DIR/build-debug.sh" >/tmp/habe-sync-build.out
"$SCRIPT_DIR/test-debug.sh" >/tmp/habe-sync-test.out

cd "$PROJECT_ROOT"
git add -A

if git diff --cached --quiet; then
  echo "No staged changes to commit."
  exit 0
fi

git commit -m "$COMMIT_MESSAGE"
git push origin "$BRANCH"

echo "SYNCED_BRANCH=$BRANCH"
gh repo view --json url,defaultBranchRef
