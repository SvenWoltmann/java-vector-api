# Machine code of AddBenchmark.vector and vectorMasked

Unlike the listings from `scripts/print-assembly.sh`, these two files show the code C2 generates for `AddBenchmark.vector` and `AddBenchmark.vectorMasked` with 1,003 elements, to explain why `vectorMasked` is about three times slower on this machine (see `../../benchmarks/supplementary/add-masked-tail.txt`). Each file holds every compilation of the method and of its JMH stub, C1 and C2; the hot code is the last one, `..._avgt_jmhStub (57 bytes)` compiled by C2.

Same machine and JDK as the benchmarks (i7-12700H, AVX2 without AVX-512, JDK 27+35, under WSL2). Command, run from the repository root after `scripts/run-benchmarks.sh` had built the benchmarks:

```
java --add-modules jdk.incubator.vector -cp 'benchmarks/target/classes:benchmarks/target/lib/*' \
  org.openjdk.jmh.Main 'AddBenchmark\.(vector|vectorMasked)$' -p size=1003 -f 1 -wi 3 -i 1 \
  -jvmArgsAppend "-XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=quiet -XX:CompileCommand=print,*AddBenchmark*::vector*"
```

No hsdis library was available, so HotSpot printed the code as hex bytes. These were disassembled afterwards with capstone 5.0 (Python bindings, x86-64, Intel syntax), keeping HotSpot's annotations at their addresses. Every byte decoded to an instruction, and the addresses were checked to be contiguous. In two places, JMH had written its own output (an iteration result and a warmup header) into the middle of a hex line; that text was moved to a line of its own before the code. The JMH settings header appears only at the top of `AddBenchmark-vector.txt`, since both benchmarks ran in one JMH invocation.

What the final C2 code shows:

- `vector`: the main loop is unrolled eight times, with `vmovups` (load), `vaddps` with a memory operand (second load and addition) and `vmovdqu` (store) per vector of eight floats, and no bounds checks inside the loop.
- `vectorMasked`: the loop is not unrolled. Even when the mask is all true, both loads are `vmaskmovps` (AVX2 has no mask registers), followed by `vaddps`, a `vtestps` that checks whether the mask is all true, a reload of the array length with a bounds check and an unmasked `vmovdqu` store. Each iteration then recomputes the remaining length and reloads the constant with the lane indices. The last, partial iteration builds the mask with `vcvtsi2ss`/`vcmplt_oqps` and stores with a masked `vmaskmovps`.

## AddBenchmark.scalar with fixed array offsets

`AddBenchmark-scalar-16-byte-offset.txt` and `AddBenchmark-scalar-12-byte-offset.txt` show the code C2 generates for the auto-vectorized `AddBenchmark.scalar` with 1,000 elements when every float array starts at a fixed offset from a 32-byte boundary, made the same way as above:

```
java --add-modules jdk.incubator.vector -cp 'benchmarks/target/classes:benchmarks/target/lib/*' \
  org.openjdk.jmh.Main 'AddBenchmark\.scalar$' -p size=1000 -f 1 -wi 3 -i 1 \
  -jvmArgsAppend "<flags> -XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=quiet -XX:CompileCommand=print,*AddBenchmark*::scalar*"
```

| File | `<flags>` | First element |
|---|---|---|
| `AddBenchmark-scalar-16-byte-offset.txt` | `-XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=32` | 16 bytes after a 32-byte boundary |
| `AddBenchmark-scalar-12-byte-offset.txt` | `-XX:ObjectAlignmentInBytes=32` (compact object headers, the default) | 12 bytes after a 32-byte boundary |

In both, C2 puts a scalar pre-loop (`vmovss`/`vaddss`/`vmovss`) in front of the eight-fold unrolled vector loop, and its limit is a constant: `min(length, 4)` with the 16-byte offset and `min(length, 5)` with the 12-byte offset. Since the objects are 32-byte aligned, C2 knows the address of the first element modulo 32 at compile time and chooses the number of scalar iterations that puts the vector stores on 32-byte boundaries (`SuperWordAutomaticAlignment = 1`, align with the store). This is what `AddAlignmentBenchmark.vectorAfterHead` does by hand with `head` = 4, and it explains why the auto-vectorized loop does not depend on where the arrays lie; see `../../benchmarks/supplementary/add-alignment-sweep.txt` and `add-scalar-12-byte-offset.txt`.
