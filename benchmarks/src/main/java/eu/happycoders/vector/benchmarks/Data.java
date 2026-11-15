package eu.happycoders.vector.benchmarks;

import java.util.Random;

/** Reproducible input data for the benchmarks. */
final class Data {

  private Data() {}

  static float[] randomFloats(int size, long seed) {
    Random random = new Random(seed);
    float[] values = new float[size];
    for (int i = 0; i < size; i++) {
      values[i] = random.nextFloat() * 2 - 1;
    }
    return values;
  }

  /** Random bytes from 'a' to 'z'; the search target '#' sits only in the last position. */
  static byte[] textWithTargetAtEnd(int size, byte target) {
    Random random = new Random(size);
    byte[] values = new byte[size];
    for (int i = 0; i < size; i++) {
      values[i] = (byte) ('a' + random.nextInt(26));
    }
    values[size - 1] = target;
    return values;
  }
}
