import jdk.incubator.vector.*;

static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

void main() {
  float[] a = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  float[] b = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};

  IO.println("Lanes:      " + SPECIES.length());
  IO.println("Loop bound: " + SPECIES.loopBound(a.length));

  float[] c = new float[a.length];
  addWithTailLoop(a, b, c);
  IO.println("Tail loop:  " + Arrays.toString(c));

  float[] d = new float[a.length];
  addWithMask(a, b, d);
  IO.println("Mask:       " + Arrays.toString(d));
}

// Vector loop for the full vectors, scalar loop for the rest
void addWithTailLoop(float[] a, float[] b, float[] c) {
  int i = 0;
  int upperBound = SPECIES.loopBound(a.length);
  for (; i < upperBound; i += SPECIES.length()) {
    FloatVector va = FloatVector.fromArray(SPECIES, a, i);
    FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
    FloatVector vc = va.add(vb);
    vc.intoArray(c, i);
  }
  for (; i < a.length; i++) {
    c[i] = a[i] + b[i];
  }
}

// One loop; in the last round, the mask switches off the lanes
// beyond the end of the arrays
void addWithMask(float[] a, float[] b, float[] c) {
  for (int i = 0; i < a.length; i += SPECIES.length()) {
    VectorMask<Float> m = SPECIES.indexInRange(i, a.length);
    FloatVector va = FloatVector.fromArray(SPECIES, a, i, m);
    FloatVector vb = FloatVector.fromArray(SPECIES, b, i, m);
    FloatVector vc = va.add(vb);
    vc.intoArray(c, i, m);
  }
}
