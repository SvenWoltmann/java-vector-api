package eu.happycoders.vector.benchmarks;

import static jdk.incubator.vector.VectorOperators.ADD;

import java.util.concurrent.TimeUnit;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;

/**
 * Dot product of two float arrays. The scalar loop must add in the order of the source code,
 * the Vector API adds lane by lane – a different order, so the result may differ in the last
 * digits. {@code vector()} adds into one vector of sums, so each {@code fma()} waits for the one
 * before; {@code vectorFourAccumulators()} spreads the work over four independent vectors.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 3, jvmArgsAppend = {"--add-modules", "jdk.incubator.vector"})
public class DotProductBenchmark {

  private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

  @Param({"7", "16", "100", "1000", "100000"})
  private int size;

  private float[] a;
  private float[] b;

  @Setup
  public void setUp() {
    a = Data.randomFloats(size, 1);
    b = Data.randomFloats(size, 2);
  }

  @Benchmark
  public float scalar() {
    float sum = 0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i] * b[i];
    }
    return sum;
  }

  @Benchmark
  public float vector() {
    FloatVector sums = FloatVector.zero(SPECIES);
    int i = 0;
    for (; i < SPECIES.loopBound(a.length); i += SPECIES.length()) {
      FloatVector va = FloatVector.fromArray(SPECIES, a, i);
      FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
      sums = va.fma(vb, sums);
    }
    float sum = sums.reduceLanes(ADD);
    for (; i < a.length; i++) {
      sum += a[i] * b[i];
    }
    return sum;
  }

  @Benchmark
  public float vectorFourAccumulators() {
    int lanes = SPECIES.length();
    FloatVector sums0 = FloatVector.zero(SPECIES);
    FloatVector sums1 = FloatVector.zero(SPECIES);
    FloatVector sums2 = FloatVector.zero(SPECIES);
    FloatVector sums3 = FloatVector.zero(SPECIES);
    int i = 0;
    int upperBound = a.length - a.length % (4 * lanes);
    for (; i < upperBound; i += 4 * lanes) {
      sums0 = fma(i, sums0);
      sums1 = fma(i + lanes, sums1);
      sums2 = fma(i + 2 * lanes, sums2);
      sums3 = fma(i + 3 * lanes, sums3);
    }
    for (; i < SPECIES.loopBound(a.length); i += lanes) {
      sums0 = fma(i, sums0);
    }
    float sum = sums0.add(sums1).add(sums2.add(sums3)).reduceLanes(ADD);
    for (; i < a.length; i++) {
      sum += a[i] * b[i];
    }
    return sum;
  }

  private FloatVector fma(int i, FloatVector sums) {
    FloatVector va = FloatVector.fromArray(SPECIES, a, i);
    FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
    return va.fma(vb, sums);
  }
}
