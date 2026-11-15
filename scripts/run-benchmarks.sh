#!/usr/bin/env bash
# Builds the JMH benchmarks in benchmarks/ and runs all of them with the given
# JDK. Results go to results/<machine-id>/benchmarks/results.json, the console
# log next to it. Takes about 30 minutes (9 methods, 5 sizes,
# 3 forks of 10 seconds each); run it only on an otherwise idle machine,
# on mains power.
# Usage: scripts/run-benchmarks.sh <path-to-jdk-27> <machine-id> [JMH options...]
set -euo pipefail

JDK=${1:?usage: scripts/run-benchmarks.sh <path-to-jdk-27> <machine-id> [JMH options...]}
MACHINE=${2:?usage: scripts/run-benchmarks.sh <path-to-jdk-27> <machine-id> [JMH options...]}
shift 2
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT="$ROOT/results/$MACHINE/benchmarks"
mkdir -p "$OUT"

(cd "$ROOT/benchmarks" && JAVA_HOME="$JDK" mvn -B -q package)
{
  "$JDK/bin/java" -version 2>&1
  echo "os.arch: $(uname -m)"
} > "$OUT/environment.txt"

"$JDK/bin/java" --add-modules jdk.incubator.vector \
  -cp "$ROOT/benchmarks/target/classes:$ROOT/benchmarks/target/lib/*" \
  org.openjdk.jmh.Main -rf json -rff "$OUT/results.json" "$@" \
  2>&1 | tee "$OUT/console.txt"
