import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.*;

// Four methods for the machine code in the article: two scalar loops
// (one that C2 vectorizes on its own, one that it does not) and their
// Vector API counterparts. scripts/print-assembly.sh prints what C2
// generates for each of them.

static final VectorSpecies<Integer> INTS = IntVector.SPECIES_PREFERRED;
static final VectorSpecies<Byte> BYTES = ByteVector.SPECIES_PREFERRED;

void main() {
  int[] a = new int[1_000];
  int[] b = new int[1_000];
  int[] c = new int[1_000];
  byte[] data = new byte[1_000];
  for (int i = 0; i < a.length; i++) {
    a[i] = i;
    b[i] = 2 * i;
  }
  data[999] = 1;  // the search runs to the end of the array

  // Enough calls for C2 to compile all four methods
  long check = 0;
  for (int round = 0; round < 200_000; round++) {
    addScalar(a, b, c);
    addVector(a, b, c);
    check += indexOfScalar(data, (byte) 1) + indexOfVector(data, (byte) 1) + c[999];
  }
  IO.println(check);
}

void addScalar(int[] a, int[] b, int[] c) {
  for (int i = 0; i < a.length; i++) {
    c[i] = a[i] + b[i];
  }
}

void addVector(int[] a, int[] b, int[] c) {
  int i = 0;
  for (; i < INTS.loopBound(a.length); i += INTS.length()) {
    IntVector va = IntVector.fromArray(INTS, a, i);
    IntVector vb = IntVector.fromArray(INTS, b, i);
    va.add(vb).intoArray(c, i);
  }
  for (; i < a.length; i++) {
    c[i] = a[i] + b[i];
  }
}

int indexOfScalar(byte[] data, byte b) {
  for (int i = 0; i < data.length; i++) {
    if (data[i] == b) return i;
  }
  return -1;
}

int indexOfVector(byte[] data, byte b) {
  int i = 0;
  for (; i < BYTES.loopBound(data.length); i += BYTES.length()) {
    VectorMask<Byte> found = ByteVector.fromArray(BYTES, data, i).compare(EQ, b);
    if (found.anyTrue()) {
      return i + found.firstTrue();
    }
  }
  for (; i < data.length; i++) {
    if (data[i] == b) return i;
  }
  return -1;
}
