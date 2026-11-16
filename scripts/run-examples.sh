#!/usr/bin/env bash
# Runs every example program with the given JDK and writes the outputs to
# results/<machine-id>/. The outputs depend on the CPU (vector size, lane
# count), so each machine gets its own directory.
# Usage: scripts/run-examples.sh <path-to-jdk-27> <machine-id>
#
# Exits with 1 if a program fails.
set -uo pipefail

JDK=${1:?usage: scripts/run-examples.sh <path-to-jdk-27> <machine-id>}
MACHINE=${2:?usage: scripts/run-examples.sh <path-to-jdk-27> <machine-id>}
JAVA="$JDK/bin/java"
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT="$ROOT/results/$MACHINE"
# Overwrites only its own outputs; the machine code of print-assembly.sh stays
mkdir -p "$OUT"
{
  "$JAVA" -version 2>&1
  echo "os.arch: $(uname -m)"
} > "$OUT/environment.txt"
FAILED=()

run() { # <dir> <program> <output-name> [JVM options...]
  local dir=$1 program=$2 name=$3
  shift 3
  mkdir -p "$OUT/$dir"
  if ! "$JAVA" "$@" --add-modules jdk.incubator.vector \
      "$ROOT/src/main/java/$dir/$program.java" 2>&1 \
      | grep -v '^WARNING: Using incubator modules' > "$OUT/$dir/$name.txt"; then
    FAILED+=("$dir/$name")
  fi
}

run species PreferredSpecies PreferredSpecies
run firststeps AddArrays AddArrays
for p in Arithmetic CompareAndBlend Reductions MaskedReduction Compress Expand Rearrange SelectFrom Conversions MemorySegments; do
  run operations $p $p
done
for p in CosineSimilarity ByteSearch; do run examples $p $p; done
run jit Allocations Allocations
run jit Allocations Allocations-c1-only -XX:TieredStopAtLevel=1

if [[ ${#FAILED[@]} -gt 0 ]]; then
  printf 'Failed: %s\n' "${FAILED[@]}" >&2
  exit 1
fi
echo "All outputs in results/$MACHINE"
