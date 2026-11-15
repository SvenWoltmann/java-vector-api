#!/usr/bin/env bash
# Prints the machine code that C2 generates for the four methods of
# src/main/java/jit/JitDemo.java into results/<machine-id>/jit/assembly/.
# Needs the hsdis disassembler library (hsdis-aarch64.dylib, hsdis-amd64.so)
# in <hsdis-dir>; the JDK does not ship it. One way to build it: compile
# src/utils/hsdis/capstone/hsdis-capstone.c of openjdk/jdk against capstone.
# Usage: scripts/print-assembly.sh <path-to-jdk-27> <hsdis-dir> <machine-id>
set -euo pipefail

JDK=${1:?usage: scripts/print-assembly.sh <path-to-jdk-27> <hsdis-dir> <machine-id>}
HSDIS=${2:?usage: scripts/print-assembly.sh <path-to-jdk-27> <hsdis-dir> <machine-id>}
MACHINE=${3:?usage: scripts/print-assembly.sh <path-to-jdk-27> <hsdis-dir> <machine-id>}
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT="$ROOT/results/$MACHINE/jit/assembly"
mkdir -p "$OUT"

for method in addScalar addVector indexOfScalar indexOfVector; do
  DYLD_LIBRARY_PATH="$HSDIS" LD_LIBRARY_PATH="$HSDIS" "$JDK/bin/java" \
    -XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=quiet \
    -XX:CompileCommand=print,JitDemo::$method \
    --add-modules jdk.incubator.vector "$ROOT/src/main/java/jit/JitDemo.java" \
    > "$OUT/$method.txt" 2>&1
  echo "$method: $(grep -c 'Compiled method (c2)' "$OUT/$method.txt") C2 compilation(s)"
done
