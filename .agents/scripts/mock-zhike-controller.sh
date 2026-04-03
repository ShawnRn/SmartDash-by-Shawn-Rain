#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec swift "$SCRIPT_DIR/mock-zhike-controller.swift" "$@"
