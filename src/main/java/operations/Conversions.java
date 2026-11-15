import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.*;

static final VectorSpecies<Byte> BYTES = ByteVector.SPECIES_128;
static final VectorSpecies<Integer> INTS = IntVector.SPECIES_128;

void main() {
  byte[] data = new byte[16];
  for (int i = 0; i < data.length; i++) {
    data[i] = (byte) (i * 20);  // byte is signed: from 140 on, values wrap
  }
  ByteVector bytes = ByteVector.fromArray(BYTES, data, 0);
  IO.println("Bytes: " + bytes);

  // 16 bytes become 16 ints = 512 bits: four 128-bit vectors,
  // one per part. convertShape() returns one part at a time.
  for (int part = 0; part < 4; part++) {
    Vector<Integer> ints = bytes.convertShape(B2I, INTS, part);
    IO.println("Part " + part + ": " + ints);
  }

  // Same lane count, same shape: int -> float, no part needed
  IntVector i = IntVector.fromArray(INTS, new int[] {1, 2, 3, 16_777_217}, 0);
  IO.println(i + " -> " + i.convert(I2F, 0));

  // Reinterpret: the same 128 bits, read as four ints
  IO.println("Reinterpreted: " + bytes.reinterpretAsInts());
}
