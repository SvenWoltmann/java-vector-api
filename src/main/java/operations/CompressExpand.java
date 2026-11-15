import static jdk.incubator.vector.VectorOperators.*;

import jdk.incubator.vector.*;

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

    // Writes all four lanes; the next round overwrites the zeros at the end
    packed.intoArray(result, count);
    count += positive.trueCount();
  }
  IO.println(Arrays.toString(Arrays.copyOf(result, count)));

  // expand() is the inverse: it distributes lanes to the positions of the mask
  IntVector source = IntVector.fromArray(SPECIES, new int[] {1, 2, 3, 4}, 0);
  VectorMask<Integer> mask =
      VectorMask.fromArray(SPECIES, new boolean[] {false, true, false, true}, 0);
  IO.println(source + " -> expand " + mask + " -> " + source.expand(mask));
}
