package eu.happycoders.vector.benchmarks;

import java.util.concurrent.TimeUnit;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;

/**
 * Adds two float arrays with the Vector API, but starts the vector loop only after {@code head}
 * scalar elements. This shifts the addresses of all vector loads and stores by {@code head * 4}
 * bytes, so the run shows how much the alignment of the vector accesses matters.
 *
 * <p>Meant to run with {@code -XX:-UseCompactObjectHeaders -XX:ObjectAlignmentInBytes=<vector
 * size in bytes>}: then every array starts at the same offset from a vector-size boundary, and
 * one value of {@code head} puts all vector accesses on such a boundary. Without these flags, the
 * offset depends on where the arrays land on the heap.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 3, jvmArgsAppend = {"--add-modules", "jdk.incubator.vector"})
public class AddAlignmentBenchmark {

  private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

  @Param({"1000", "100000"})
  private int size;

  @Param({"0", "1", "2", "3", "4", "5", "6", "7"})
  private int head;

  private float[] a;
  private float[] b;
  private float[] c;

  @Setup
  public void setUp() {
    a = Data.randomFloats(size, 1);
    b = Data.randomFloats(size, 2);
    c = new float[size];
  }

  @Benchmark
  public float[] vectorAfterHead() {
    int i = 0;
    for (; i < head; i++) {
      c[i] = a[i] + b[i];
    }
    int upperBound = head + SPECIES.loopBound(a.length - head);
    for (; i < upperBound; i += SPECIES.length()) {
      FloatVector va = FloatVector.fromArray(SPECIES, a, i);
      FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
      va.add(vb).intoArray(c, i);
    }
    for (; i < a.length; i++) {
      c[i] = a[i] + b[i];
    }
    return c;
  }
}
