#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SCRIPT_PATH="$SCRIPT_DIR/k6-load-test.js"

if ! command -v k6 >/dev/null 2>&1; then
  echo "k6 is required for load testing. Install from https://k6.io/docs/get-started/installation/." >&2
  exit 1
fi

cd "$PROJECT_ROOT"
echo "Running k6 load test against $BASE_URL"
k6 run --env BASE_URL="$BASE_URL" "$SCRIPT_PATH"
