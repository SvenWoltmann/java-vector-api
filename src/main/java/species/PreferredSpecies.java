import jdk.incubator.vector.*;

// Which vector size the JVM prefers on this CPU, per element type
void main() {
  List<VectorSpecies<?>> species = List.of(
      ByteVector.SPECIES_PREFERRED,
      ShortVector.SPECIES_PREFERRED,
      IntVector.SPECIES_PREFERRED,
      LongVector.SPECIES_PREFERRED,
      FloatVector.SPECIES_PREFERRED,
      DoubleVector.SPECIES_PREFERRED);

  for (VectorSpecies<?> s : species) {
    IO.println(s.elementType() + ": " + s.vectorBitSize() + " bits, "
        + s.length() + " lanes");
  }

  IO.println("Largest shape: " + VectorShape.S_Max_BIT.vectorBitSize() + " bits");
}
