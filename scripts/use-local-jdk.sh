#!/usr/bin/env bash

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="$ROOT_DIR/.tools/jdk-17/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "JAVA_HOME=$JAVA_HOME"
java -version
