#!/usr/bin/env bash
# Run the Java/Linux demo on your real terminal. Build first with ../build.sh.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
: "${JAVA_HOME:=/usr/lib/jvm/java-25-openjdk-amd64}"
exec "$JAVA_HOME/bin/java" \
    -Dtermacs.jni="$ROOT/termacs-core/build/libtermacsjni.so" \
    --enable-native-access=ALL-UNNAMED \
    -cp "$ROOT/termacs-java/build/classes:$ROOT/termacs-java-demo/build/classes" \
    sciens.termacs.demo.TaskDemo
