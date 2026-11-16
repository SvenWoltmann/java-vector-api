import static jdk.incubator.vector.VectorOperators.*;

import jdk.incubator.vector.*;

static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_128;

void main() {
  IntVector v = IntVector.fromArray(SPECIES, new int[] {3, -7, 12, 5}, 0);

  // A reduction can also take a mask: sum of the positive values only
  VectorMask<Integer> positive = v.compare(GT, 0);
  IO.println("Vector:       " + v);
  IO.println("Mask:         " + positive);
  IO.println("Positive sum: " + v.reduceLanes(ADD, positive));
}
