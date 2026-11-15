import java.lang.foreign.*;
import java.nio.ByteOrder;
import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.*;

static final VectorSpecies<Long> SPECIES = LongVector.SPECIES_PREFERRED;

void main() {
  int count = 1_000;
  try (Arena arena = Arena.ofConfined()) {
    // Off-heap memory, aligned to the vector size
    MemorySegment segment =
        arena.allocate(count * Long.BYTES, SPECIES.vectorByteSize());
    for (int i = 0; i < count; i++) {
      segment.setAtIndex(ValueLayout.JAVA_LONG, i, i + 1);
    }

    // The offset is in bytes, not in elements
    long step = SPECIES.vectorByteSize();
    long bound = SPECIES.loopBound(count) * (long) Long.BYTES;
    LongVector sums = LongVector.zero(SPECIES);
    long offset = 0;
    for (; offset < bound; offset += step) {
      sums = sums.add(LongVector.fromMemorySegment(
          SPECIES, segment, offset, ByteOrder.nativeOrder()));
    }
    long sum = sums.reduceLanes(ADD);
    for (; offset < segment.byteSize(); offset += Long.BYTES) {
      sum += segment.get(ValueLayout.JAVA_LONG, offset);
    }
    IO.println("Sum of 1 to " + count + ": " + sum);
  }
}
