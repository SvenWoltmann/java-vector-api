# Supplementary runs

Run on 2026-11-15 right after the main run in `../results.json`, on the same machine with the same JDK (27+35), to explain three of its results: the dot product with one sum or one vector of sums, the masked addition, and the Vector API addition being slower than the scalar one. Each `.json`/`.txt` pair is one JMH run with the settings from the benchmark annotations (3 forks, 5 × 1 s warmup, 5 × 1 s measurement).

The machine is a laptop with a 12th Gen Intel Core i7-12700H (AVX2, no AVX-512, `MaxVectorSize = 32`, see `../cpu.txt`), on mains power. All runs, including the main run, executed under **WSL2** (Linux kernel 6.18, microsoft-standard) on Windows, i.e. inside a Hyper-V virtual machine, not on bare metal.

| Run | What it measures |
|---|---|
| `add-masked-tail` | `AddBenchmark.vector` and `vectorMasked` with 1,003 and 100,003 elements, where the last iteration needs a partial mask (all sizes of the main run except 7 are multiples of the eight lanes), with the GC profiler |
| `add-16-byte-aligned-arrays` | `AddBenchmark.scalar` and `vector` with `-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=16`, which puts the first element of every array on a 16-byte boundary |
| `dot-product-dependency-chains` | All variants of `DotProductBenchmark` in one run: `scalar`, `scalarFourSums` (the scalar loop with four independent sums), `vector` and `vectorFourAccumulators` |
| `add-vector-1000-ten-forks` | `AddBenchmark.vector` with 1,000 elements and 10 forks instead of 3, run afterwards because the forks of the main run differed by up to 27 %. Each fork is stable (standard deviation below 1 ns), but the fork means range from 41.7 to 53.0 ns/op; even the fastest fork is slower than `AddBenchmark.scalar` (32.7 ns/op) |
| `add-masked-tail-variants` | `AddBenchmark.vector`, `vectorMasked` and `vectorMaskedTail` with 7, 1,000, 1,003 and 100,003 elements. `vectorMaskedTail` was added to `AddBenchmark` after the other runs: an unmasked main loop up to `loopBound()` and a single masked iteration for the remaining elements, to see whether a mask is still worth it for the tail on a CPU without mask registers |
| `add-masked-tail-epsilon-gc` | `AddBenchmark.vector` and `vectorMaskedTail` with 100,003 elements under Epsilon GC (no collection, so the arrays stay where `setUp()` put them), because `vectorMaskedTail` was about 30 % slower than `vector` at this size although the disassembled main loops are practically identical (same eight-fold unrolled loop, one extra `lea`/`mov`). The gap remains (10,684 vs. 13,869 ns/op), so arrays moved by the GC are not the cause |
| `add-masked-tail-with-and-without-tail` | `AddBenchmark.vector` and `vectorMaskedTail` with 100,000, 100,003 and 100,008 elements. Without a tail (100,000 and 100,008), both are equally fast; only with the masked tail (100,003) is `vectorMaskedTail` consistently slower, in all forks of all four runs at this size (13.0 to 14.0 µs). A single masked iteration should cost nanoseconds, not about 3 µs; the cause could not be determined, since WSL2 exposes no hardware performance counters |

`vectorMasked` was about three times slower than `vector` with 1,003 elements, so a short diagnostic run with `-XX:+PrintIntrinsics` followed; `masked-tail-intrinsics.txt` holds the lines of intrinsics C2 could not inline (marked `**`), with their counts.

Commands, run from the repository root after `scripts/run-benchmarks.sh` had built the benchmarks:

```
JMH="java --add-modules jdk.incubator.vector -cp benchmarks/target/classes:benchmarks/target/lib/* org.openjdk.jmh.Main -rf json"

$JMH -rff add-masked-tail.json 'AddBenchmark\.(vector|vectorMasked)$' -p size=1003,100003 -prof gc
$JMH -rff add-16-byte-aligned-arrays.json 'AddBenchmark\.(scalar|vector)$' -p size=1000,100000 \
  -jvmArgsAppend "-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=16"
$JMH -rff dot-product-dependency-chains.json 'DotProductBenchmark\.'
$JMH -rff add-vector-1000-ten-forks.json 'AddBenchmark\.vector$' -p size=1000 -f 10
$JMH -rff add-masked-tail-variants.json 'AddBenchmark\.(vector|vectorMasked|vectorMaskedTail)$' -p size=7,1000,1003,100003
$JMH -rff add-masked-tail-epsilon-gc.json 'AddBenchmark\.(vector|vectorMaskedTail)$' -p size=100003 \
  -jvmArgsAppend "-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xms12g -Xmx12g -XX:+AlwaysPreTouch"
$JMH -rff add-masked-tail-with-and-without-tail.json 'AddBenchmark\.(vector|vectorMaskedTail)$' -p size=100000,100003,100008

$JMH 'AddBenchmark\.vectorMasked$' -p size=1003 -f 1 -wi 3 -i 1 \
  -jvmArgsAppend "-XX:+UnlockDiagnosticVMOptions -XX:+PrintIntrinsics" 2>&1 \
  | grep '\*\*' | sort | uniq -c > masked-tail-intrinsics.txt
```
