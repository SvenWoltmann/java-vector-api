# Supplementary runs

Run on 2026-11-15 right after the main run in `../results.json`, on the same machine with the same JDK, to explain three of its results: the dot product with one sum or one vector of sums, the masked addition, and the Vector API addition being slower than the scalar one. Each `.json`/`.txt` pair is one JMH run with the settings from the benchmark annotations (3 forks, 5 × 1 s warmup, 5 × 1 s measurement).

| Run | What it measures |
|---|---|
| `dot-product-four-accumulators` | `DotProductBenchmark.vectorFourAccumulators`, added after the main run: the dot product with four independent vectors of sums instead of one |
| `add-masked-tail` | `AddBenchmark.vector` and `vectorMasked` with 1,003 and 100,003 elements, where the last iteration needs a partial mask (all sizes of the main run except 7 are multiples of the four lanes), with the GC profiler |
| `dot-product-dependency-chains` | All variants of `DotProductBenchmark` in one run, including `scalarFourSums` (the scalar loop with four independent sums) and `vectorEightAccumulators`. The latter allocates one vector object per `fma()` – about 10.8 KB per call for 1,000 elements – and was removed afterwards because it measures that, not the dependency chain |
| `add-masked-tail-allocations.txt` | Where `vectorMasked` with 1,003 elements allocates: Java Flight Recorder allocation samples, grouped by class and stack. The objects come from `fromArray()` on the in-range path (`FloatVector.java` line 2895 in JDK 27), i.e. also in the iterations with a full mask, and from `add()` |
| `add-masked-tail-variants` | `AddBenchmark.vector`, `vectorMasked` and `vectorMaskedTail` with 7, 1,000, 1,003 and 100,003 elements, with the GC profiler. `vectorMaskedTail` (added on the i7-12700H) keeps the main loop unmasked and masks only the one partial iteration at the end. On NEON that iteration allocates 176 bytes with 1,003 elements and costs about 35 ns; the main loop stays free of allocations |
| `add-masked-tail-with-and-without-tail` | `AddBenchmark.vector` and `vectorMaskedTail` with 100,000, 100,003 and 100,008 elements, with the GC profiler: only 100,003 needs a masked tail, and only there does `vectorMaskedTail` allocate (240 bytes per call) |
| `add-alignment-sweep` | `AddAlignmentBenchmark.vectorAfterHead` with a head of 0 to 7 scalar elements before the vector loop, and `AddBenchmark.scalar`, with 1,000 and 100,000 elements, under `-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=16`. Every array then starts at a 16-byte boundary, so the vector accesses are 16-byte aligned for a head of 0 and 4 only. Exactly these two are as fast as the scalar loop; all other heads take about 20 % longer |
| `add-16-byte-aligned-arrays` | `AddBenchmark.scalar` and `vector` with `-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=16`, which puts the first element of every array on a 16-byte boundary |

Commands, run from the repository root after `scripts/run-benchmarks.sh` had built the benchmarks:

```
JMH="java --add-modules jdk.incubator.vector -cp benchmarks/target/classes:benchmarks/target/lib/* org.openjdk.jmh.Main -rf json"

$JMH -rff dot-product-four-accumulators.json 'DotProductBenchmark\.vectorFourAccumulators$'
$JMH -rff dot-product-dependency-chains.json 'DotProductBenchmark\.'
$JMH -rff add-masked-tail-variants.json 'AddBenchmark\.(vector|vectorMasked|vectorMaskedTail)$' -p size=7,1000,1003,100003 -prof gc
$JMH -rff add-masked-tail-with-and-without-tail.json 'AddBenchmark\.(vector|vectorMaskedTail)$' -p size=100000,100003,100008 -prof gc
$JMH -rff add-masked-tail.json 'AddBenchmark\.(vector|vectorMasked)$' -p size=1003,100003 -prof gc
$JMH -rff add-alignment-sweep.json 'AddAlignmentBenchmark\.|AddBenchmark\.scalar$' -p size=1000,100000 \
  -jvmArgsAppend "-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=16"
$JMH -rff add-16-byte-aligned-arrays.json 'AddBenchmark\.(scalar|vector)$' -p size=1000,100000 \
  -jvmArgsAppend "-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=16"
```
