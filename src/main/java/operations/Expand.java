import jdk.incubator.vector.*;

static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_128;

void main() {
  // expand() is the inverse of compress(): it distributes the first lanes
  // to the positions of the mask and fills the other lanes with 0
  IntVector packed = IntVector.fromArray(SPECIES, new int[] {1, 4, 9, 0}, 0);
  VectorMask<Integer> mask =
      VectorMask.fromArray(SPECIES, new boolean[] {false, true, true, true}, 0);
  IO.println(packed + " -> expand " + mask + " -> " + packed.expand(mask));
}
