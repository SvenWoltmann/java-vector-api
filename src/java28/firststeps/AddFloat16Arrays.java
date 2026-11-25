import jdk.incubator.vector.*;

static final VectorSpecies<Float16> SPECIES = Float16Vector.SPECIES_PREFERRED;

void main() {
  short[] a = toFloat16Bits(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
  short[] b = new short[a.length];
  Arrays.fill(b, Float16.float16ToRawShortBits(Float16.valueOf(0.1f)));
  short[] c = new short[a.length];

  int i = 0;
  int upperBound = SPECIES.loopBound(a.length);
  for (; i < upperBound; i += SPECIES.length()) {
    Float16Vector va = Float16Vector.fromArray(SPECIES, a, i);
    Float16Vector vb = Float16Vector.fromArray(SPECIES, b, i);
    va.add(vb).intoArray(c, i);
  }
  for (; i < a.length; i++) {
    Float16 fa = Float16.shortBitsToFloat16(a[i]);
    Float16 fb = Float16.shortBitsToFloat16(b[i]);
    c[i] = Float16.float16ToRawShortBits(Float16.add(fa, fb));
  }

  IO.println("Lanes: " + SPECIES.length());
  IO.println("Sums:  " + Arrays.toString(toFloats(c)));
}

short[] toFloat16Bits(float... values) {
  short[] bits = new short[values.length];
  for (int i = 0; i < values.length; i++) {
    bits[i] = Float16.float16ToRawShortBits(Float16.valueOf(values[i]));
  }
  return bits;
}

float[] toFloats(short[] bits) {
  float[] values = new float[bits.length];
  for (int i = 0; i < bits.length; i++) {
    values[i] = Float16.shortBitsToFloat16(bits[i]).floatValue();
  }
  return values;
}
