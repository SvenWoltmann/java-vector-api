import jdk.incubator.vector.*;

static final VectorSpecies<Integer> INTS = IntVector.SPECIES_128;
static final VectorSpecies<Byte> BYTES = ByteVector.SPECIES_128;

void main() {
  IntVector v = IntVector.fromArray(INTS, new int[] {10, 20, 30, 40}, 0);

  // rearrange(): lane i of the result takes the lane named at position i
  VectorShuffle<Integer> reverse = VectorShuffle.fromValues(INTS, 3, 2, 1, 0);
  IO.println(v + " -> rearrange " + reverse + " -> " + v.rearrange(reverse));

  VectorShuffle<Integer> rotate = VectorShuffle.iota(INTS, 1, 1, true);
  IO.println(v + " -> rearrange " + rotate + " -> " + v.rearrange(rotate));

  // selectFrom(): the vector holds the indexes, the argument is the table
  byte[] digits = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
  ByteVector table = ByteVector.fromArray(BYTES, digits, 0);
  byte[] nibbles = {12, 10, 15, 14, 11, 10, 11, 14, 0, 0, 0, 0, 0, 0, 0, 1};
  ByteVector indexes = ByteVector.fromArray(BYTES, nibbles, 0);
  byte[] hex = new byte[16];
  indexes.selectFrom(table).intoArray(hex, 0);
  IO.println(new String(hex, StandardCharsets.US_ASCII));
}
