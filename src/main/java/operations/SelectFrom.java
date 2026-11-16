import jdk.incubator.vector.*;

static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_128;

void main() {
  // selectFrom(): the vector holds the indexes, the argument is the table
  byte[] digits = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
  ByteVector table = ByteVector.fromArray(SPECIES, digits, 0);
  byte[] nibbles = {12, 10, 15, 14, 11, 10, 11, 14, 0, 0, 0, 0, 0, 0, 0, 1};
  ByteVector indexes = ByteVector.fromArray(SPECIES, nibbles, 0);
  byte[] hex = new byte[16];
  indexes.selectFrom(table).intoArray(hex, 0);
  IO.println(new String(hex, StandardCharsets.US_ASCII));
}
