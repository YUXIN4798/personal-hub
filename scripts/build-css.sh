#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TAILWIND_BIN="$ROOT_DIR/tools/tailwindcss-linux-x64"
INPUT_CSS="$ROOT_DIR/src/main/resources/static/css/app.css"
OUTPUT_CSS="$ROOT_DIR/src/main/resources/static/css/main.css"

if [[ ! -x "$TAILWIND_BIN" ]]; then
  echo "Tailwind standalone CLI not found or not executable: $TAILWIND_BIN" >&2
  echo "Download v4 Linux x64 to tools/tailwindcss-linux-x64 and run chmod +x." >&2
  exit 1
fi

"$TAILWIND_BIN" -i "$INPUT_CSS" -o "$OUTPUT_CSS" --minify --cwd "$ROOT_DIR"
