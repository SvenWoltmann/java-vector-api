# Supplementary runs

Run on 2026-11-15 right after the main run in `../results.json`, on the same machine with the same JDK, to explain three of its results: the dot product with one vector of sums, the masked addition, and the Vector API addition being slower than the scalar one. Each `.json`/`.txt` pair is one JMH run with the settings from the benchmark annotations (3 forks, 5 × 1 s warmup, 5 × 1 s measurement).

| Run | What it measures |
|---|---|
| `dot-product-four-accumulators` | `DotProductBenchmark.vectorFourAccumulators`, added after the main run: the dot product with four independent vectors of sums instead of one |
| `add-masked-tail` | `AddBenchmark.vector` and `vectorMasked` with 1,003 and 100,003 elements, where the last iteration needs a partial mask (all sizes of the main run except 7 are multiples of the four lanes), with the GC profiler |
| `add-16-byte-aligned-arrays` | `AddBenchmark.scalar` and `vector` with `-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=16`, which puts the first element of every array on a 16-byte boundary |

Commands, run from the repository root after `scripts/run-benchmarks.sh` had built the benchmarks:

```
JMH="java --add-modules jdk.incubator.vector -cp benchmarks/target/classes:benchmarks/target/lib/* org.openjdk.jmh.Main -rf json"

$JMH -rff dot-product-four-accumulators.json 'DotProductBenchmark\.vectorFourAccumulators$'
$JMH -rff add-masked-tail.json 'AddBenchmark\.(vector|vectorMasked)$' -p size=1003,100003 -prof gc
$JMH -rff add-16-byte-aligned-arrays.json 'AddBenchmark\.(scalar|vector)$' -p size=1000,100000 \
  -jvmArgsAppend "-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=16"
```
