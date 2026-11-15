import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.*;

static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_128;

void main() {
  FloatVector a = FloatVector.fromArray(SPECIES, new float[] {1, 4, 9, 16}, 0);
  FloatVector b = FloatVector.fromArray(SPECIES, new float[] {2, 3, -4, 5}, 0);

  IO.println("a             = " + a);
  IO.println("b             = " + b);

  // Dedicated methods for the common operations
  IO.println("a + b         = " + a.add(b));
  IO.println("a * b         = " + a.mul(b));
  IO.println("a * 10        = " + a.mul(10));

  // Fused multiply-add: a * b + 100 in one step
  FloatVector hundred = FloatVector.broadcast(SPECIES, 100);
  IO.println("a * b + 100   = " + a.fma(b, hundred));

  // All other operators via lanewise()
  IO.println("sqrt(a)       = " + a.lanewise(SQRT));
  IO.println("abs(b)        = " + b.lanewise(ABS));
  IO.println("max(a, b)     = " + a.lanewise(MAX, b));
}
