package eu.happycoders.vector.benchmarks;

import java.util.concurrent.TimeUnit;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;

/**
 * Adds two float arrays: scalar with and without C2's auto-vectorization, and with the Vector
 * API, once with a tail loop, once with a mask in every iteration, and once with an unmasked
 * main loop and a single masked iteration for the tail.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 3, jvmArgsAppend = {"--add-modules", "jdk.incubator.vector"})
public class AddBenchmark {

  private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

  @Param({"7", "16", "100", "1000", "100000"})
  private int size;

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
  public float[] scalar() {
    for (int i = 0; i < a.length; i++) {
      c[i] = a[i] + b[i];
    }
    return c;
  }

  @Benchmark
  @Fork(
      value = 3,
      jvmArgsAppend = {"--add-modules", "jdk.incubator.vector", "-XX:-UseSuperWord"})
  public float[] scalarWithoutAutoVectorization() {
    return scalar();
  }

  @Benchmark
  public float[] vector() {
    int i = 0;
    int upperBound = SPECIES.loopBound(a.length);
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

  @Benchmark
  public float[] vectorMasked() {
    for (int i = 0; i < a.length; i += SPECIES.length()) {
      VectorMask<Float> m = SPECIES.indexInRange(i, a.length);
      FloatVector va = FloatVector.fromArray(SPECIES, a, i, m);
      FloatVector vb = FloatVector.fromArray(SPECIES, b, i, m);
      va.add(vb).intoArray(c, i, m);
    }
    return c;
  }

  @Benchmark
  public float[] vectorMaskedTail() {
    int i = 0;
    int upperBound = SPECIES.loopBound(a.length);
    for (; i < upperBound; i += SPECIES.length()) {
      FloatVector va = FloatVector.fromArray(SPECIES, a, i);
      FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
      va.add(vb).intoArray(c, i);
    }
    if (i < a.length) {
      VectorMask<Float> m = SPECIES.indexInRange(i, a.length);
      FloatVector va = FloatVector.fromArray(SPECIES, a, i, m);
      FloatVector vb = FloatVector.fromArray(SPECIES, b, i, m);
      va.add(vb).intoArray(c, i, m);
    }
    return c;
  }
}
