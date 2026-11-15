import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.*;

static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_128;

void main() {
  int[] values = {3, -7, 12, -5, -8, 1, 4, 9, 2, -6, 0, 11};

  // Filter: copy all positive values, without gaps, into result
  int[] result = new int[values.length];
  int count = 0;
  for (int i = 0; i < values.length; i += SPECIES.length()) {
    IntVector v = IntVector.fromArray(SPECIES, values, i);
    VectorMask<Integer> positive = v.compare(GT, 0);
    IntVector packed = v.compress(positive);
    IO.println(v + " -> compress " + positive + " -> " + packed);

    packed.intoArray(result, count);  // the zeros at the end are overwritten
    count += positive.trueCount();    // in the next round
  }
  IO.println(Arrays.toString(Arrays.copyOf(result, count)));

  // expand() is the inverse: it distributes lanes to the positions of the mask
  IntVector source = IntVector.fromArray(SPECIES, new int[] {1, 2, 3, 4}, 0);
  VectorMask<Integer> mask =
      VectorMask.fromArray(SPECIES, new boolean[] {false, true, false, true}, 0);
  IO.println(source + " -> expand " + mask + " -> " + source.expand(mask));
}
