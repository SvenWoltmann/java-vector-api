package eu.happycoders.vector.benchmarks;

import static jdk.incubator.vector.VectorOperators.EQ;

import java.util.concurrent.TimeUnit;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;

/**
 * Searches a byte array for the first occurrence of a byte that sits in the last position. C2
 * does not auto-vectorize the scalar loop because it has a second exit.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 3, jvmArgsAppend = {"--add-modules", "jdk.incubator.vector"})
public class IndexOfBenchmark {

  private static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;
  private static final byte TARGET = '#';

  @Param({"7", "16", "100", "1000", "100000"})
  private int size;

  private byte[] data;

  @Setup
  public void setUp() {
    data = Data.textWithTargetAtEnd(size, TARGET);
  }

  @Benchmark
  public int scalar() {
    for (int i = 0; i < data.length; i++) {
      if (data[i] == TARGET) return i;
    }
    return -1;
  }

  @Benchmark
  public int vector() {
    int i = 0;
    for (; i < SPECIES.loopBound(data.length); i += SPECIES.length()) {
      VectorMask<Byte> found = ByteVector.fromArray(SPECIES, data, i).compare(EQ, TARGET);
      if (found.anyTrue()) {
        return i + found.firstTrue();
      }
    }
    for (; i < data.length; i++) {
      if (data[i] == TARGET) return i;
    }
    return -1;
  }
}
