import static jdk.incubator.vector.VectorOperators.*;

import jdk.incubator.vector.*;

static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_128;

void main() {
  float[] values = {-1.5f, 2.0f, -0.5f, 3.0f, 4.0f, -2.0f, 0.0f, 1.0f};
  float[] result = new float[values.length];

  for (int i = 0; i < values.length; i += SPECIES.length()) {
    FloatVector v = FloatVector.fromArray(SPECIES, values, i);

    // Scalar: result[i] = values[i] < 0 ? 0 : values[i]
    VectorMask<Float> negative = v.compare(LT, 0);
    FloatVector clamped = v.blend(0, negative);

    IO.println(v + " -> mask " + negative + " -> " + clamped);
    clamped.intoArray(result, i);
  }

  IO.println(Arrays.toString(result));
}
