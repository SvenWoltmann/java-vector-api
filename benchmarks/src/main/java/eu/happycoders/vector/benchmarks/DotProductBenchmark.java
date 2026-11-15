package eu.happycoders.vector.benchmarks;

import static jdk.incubator.vector.VectorOperators.ADD;

import java.util.concurrent.TimeUnit;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;

/**
 * Dot product of two float arrays. The scalar loop must add in the order of the source code,
 * the Vector API adds lane by lane – a different order, so the result may differ in the last
 * digits. {@code scalar()} and {@code vector()} add into one sum or one vector of sums, so each
 * addition waits for the one before; the variants with four and eight sums spread the work over
 * independent sums. {@code scalarFourSums()} changes the order of the additions just like the
 * Vector API, so it is the fair scalar counterpart.
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
  public float scalarFourSums() {
    float sum0 = 0;
    float sum1 = 0;
    float sum2 = 0;
    float sum3 = 0;
    int i = 0;
    for (; i < a.length - 3; i += 4) {
      sum0 += a[i] * b[i];
      sum1 += a[i + 1] * b[i + 1];
      sum2 += a[i + 2] * b[i + 2];
      sum3 += a[i + 3] * b[i + 3];
    }
    float sum = (sum0 + sum1) + (sum2 + sum3);
    for (; i < a.length; i++) {
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

  @Benchmark
  public float vectorEightAccumulators() {
    int lanes = SPECIES.length();
    FloatVector sums0 = FloatVector.zero(SPECIES);
    FloatVector sums1 = FloatVector.zero(SPECIES);
    FloatVector sums2 = FloatVector.zero(SPECIES);
    FloatVector sums3 = FloatVector.zero(SPECIES);
    FloatVector sums4 = FloatVector.zero(SPECIES);
    FloatVector sums5 = FloatVector.zero(SPECIES);
    FloatVector sums6 = FloatVector.zero(SPECIES);
    FloatVector sums7 = FloatVector.zero(SPECIES);
    int i = 0;
    int upperBound = a.length - a.length % (8 * lanes);
    for (; i < upperBound; i += 8 * lanes) {
      sums0 = fma(i, sums0);
      sums1 = fma(i + lanes, sums1);
      sums2 = fma(i + 2 * lanes, sums2);
      sums3 = fma(i + 3 * lanes, sums3);
      sums4 = fma(i + 4 * lanes, sums4);
      sums5 = fma(i + 5 * lanes, sums5);
      sums6 = fma(i + 6 * lanes, sums6);
      sums7 = fma(i + 7 * lanes, sums7);
    }
    for (; i < SPECIES.loopBound(a.length); i += lanes) {
      sums0 = fma(i, sums0);
    }
    FloatVector sums = sums0.add(sums1).add(sums2.add(sums3));
    sums = sums.add(sums4.add(sums5).add(sums6.add(sums7)));
    float sum = sums.reduceLanes(ADD);
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
