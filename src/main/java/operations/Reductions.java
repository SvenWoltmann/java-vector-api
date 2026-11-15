import static jdk.incubator.vector.VectorOperators.*;

import jdk.incubator.vector.*;

static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_128;

void main() {
  int[] values = {3, -7, 12, 5, 8, -1, 4, 9, 2, 6};

  // Accumulate lane by lane, reduce to a scalar only once at the end
  IntVector sums = IntVector.zero(SPECIES);
  IntVector maxima = IntVector.broadcast(SPECIES, Integer.MIN_VALUE);
  int i = 0;
  for (; i < SPECIES.loopBound(values.length); i += SPECIES.length()) {
    IntVector v = IntVector.fromArray(SPECIES, values, i);
    sums = sums.add(v);
    maxima = maxima.max(v);
  }
  IO.println("Lane sums:   " + sums);
  IO.println("Lane maxima: " + maxima);

  int sum = sums.reduceLanes(ADD);
  int max = maxima.reduceLanes(MAX);
  for (; i < values.length; i++) {
    sum += values[i];
    max = Math.max(max, values[i]);
  }
  IO.println("Sum: " + sum + ", maximum: " + max);

  // A reduction can also take a mask: sum of the positive values only
  IntVector first = IntVector.fromArray(SPECIES, values, 0);
  VectorMask<Integer> positive = first.compare(GT, 0);
  IO.println("Positive sum of " + first + ": " + first.reduceLanes(ADD, positive));
}
