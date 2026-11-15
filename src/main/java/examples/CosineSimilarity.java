import static jdk.incubator.vector.VectorOperators.*;

import jdk.incubator.vector.*;

static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

void main() {
  // Two "embeddings" with 1,000 dimensions, reproducible random values
  Random random = new Random(42);
  float[] a = new float[1_000];
  float[] b = new float[1_000];
  for (int i = 0; i < a.length; i++) {
    a[i] = random.nextFloat() * 2 - 1;
    b[i] = a[i] * 0.8f + (random.nextFloat() * 2 - 1) * 0.2f;
  }

  IO.println("Scalar: " + cosineScalar(a, b));
  IO.println("Vector: " + cosineVector(a, b));
}

float cosineScalar(float[] a, float[] b) {
  float dot = 0;
  float normA = 0;
  float normB = 0;
  for (int i = 0; i < a.length; i++) {
    dot += a[i] * b[i];
    normA += a[i] * a[i];
    normB += b[i] * b[i];
  }
  return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
}

float cosineVector(float[] a, float[] b) {
  FloatVector dot = FloatVector.zero(SPECIES);
  FloatVector normA = FloatVector.zero(SPECIES);
  FloatVector normB = FloatVector.zero(SPECIES);
  int i = 0;
  for (; i < SPECIES.loopBound(a.length); i += SPECIES.length()) {
    FloatVector va = FloatVector.fromArray(SPECIES, a, i);
    FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
    dot = va.fma(vb, dot); // dot += va * vb, lane by lane
    normA = va.fma(va, normA);
    normB = vb.fma(vb, normB);
  }
  float d = dot.reduceLanes(ADD);
  float na = normA.reduceLanes(ADD);
  float nb = normB.reduceLanes(ADD);
  for (; i < a.length; i++) {
    d += a[i] * b[i];
    na += a[i] * a[i];
    nb += b[i] * b[i];
  }
  return (float) (d / (Math.sqrt(na) * Math.sqrt(nb)));
}
