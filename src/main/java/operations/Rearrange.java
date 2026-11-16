import jdk.incubator.vector.*;

static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_128;

void main() {
  IntVector v = IntVector.fromArray(SPECIES, new int[] {10, 20, 30, 40}, 0);

  // rearrange(): lane i of the result takes the lane named at position i
  VectorShuffle<Integer> reverse = VectorShuffle.fromValues(SPECIES, 3, 2, 1, 0);
  IO.println(v + " -> rearrange " + reverse + " -> " + v.rearrange(reverse));

  VectorShuffle<Integer> rotate = VectorShuffle.iota(SPECIES, 1, 1, true);
  IO.println(v + " -> rearrange " + rotate + " -> " + v.rearrange(rotate));
}
