#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="$ROOT_DIR/out"
LOCAL_JAVA_HOME="$ROOT_DIR/.tools/jdk-17/Contents/Home"

if [ -d "$LOCAL_JAVA_HOME" ]; then
  export JAVA_HOME="$LOCAL_JAVA_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if ! command -v java >/dev/null 2>&1 || ! command -v javac >/dev/null 2>&1; then
  echo "Java environment not found. Expected JDK 17+."
  echo "If needed, source $ROOT_DIR/scripts/use-local-jdk.sh"
  exit 1
fi

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"
find "$ROOT_DIR/src/main/java" -name "*.java" > "$OUT_DIR/sources.list"

javac -encoding UTF-8 -d "$OUT_DIR" @"$OUT_DIR/sources.list"
java -cp "$OUT_DIR" cn.edu.bupt.tarecruitment.Main
