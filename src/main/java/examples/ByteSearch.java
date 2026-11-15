import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.*;

static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;

void main() {
  String text = """
      The Vector API has been incubating since Java 16.
      Java 27 contains the twelfth incubator round.
      It waits for Project Valhalla.
      """;
  byte[] data = text.getBytes(StandardCharsets.US_ASCII);

  IO.println("Lines:          " + count(data, (byte) '\n'));
  IO.println("First 'J' at:   " + indexOf(data, (byte) 'J'));
  IO.println("Expected:       " + text.indexOf('J'));
  IO.println("First '#' at:   " + indexOf(data, (byte) '#'));
}

// How often b occurs in data
int count(byte[] data, byte b) {
  int count = 0;
  int i = 0;
  for (; i < SPECIES.loopBound(data.length); i += SPECIES.length()) {
    ByteVector v = ByteVector.fromArray(SPECIES, data, i);
    count += v.compare(EQ, b).trueCount();
  }
  for (; i < data.length; i++) {
    if (data[i] == b) count++;
  }
  return count;
}

// Index of the first b in data, -1 if there is none
int indexOf(byte[] data, byte b) {
  int i = 0;
  for (; i < SPECIES.loopBound(data.length); i += SPECIES.length()) {
    ByteVector v = ByteVector.fromArray(SPECIES, data, i);
    VectorMask<Byte> found = v.compare(EQ, b);
    if (found.anyTrue()) {
      return i + found.firstTrue();
    }
  }
  for (; i < data.length; i++) {
    if (data[i] == b) return i;
  }
  return -1;
}
