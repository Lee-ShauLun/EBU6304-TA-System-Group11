#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="$ROOT_DIR/out-test"

if ! command -v javac >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1; then
  echo "Java environment not found. Install JDK 17+ and make sure java and javac are available."
  exit 1
fi

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"
find "$ROOT_DIR/src/main/java" "$ROOT_DIR/src/test/java" -name "*.java" > "$OUT_DIR/sources.list"

javac -encoding UTF-8 -d "$OUT_DIR" @"$OUT_DIR/sources.list"
java -cp "$OUT_DIR" cn.edu.bupt.tarecruitment.TestRunner
