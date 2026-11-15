import static jdk.incubator.vector.VectorOperators.*;

import java.lang.foreign.*;
import java.nio.ByteOrder;
import jdk.incubator.vector.*;

static final VectorSpecies<Long> SPECIES = LongVector.SPECIES_PREFERRED;

void main() {
  int count = 1_000;
  try (Arena arena = Arena.ofConfined()) {
    // Off-heap memory, aligned to the vector size
    long vectorBytes = SPECIES.vectorByteSize();
    MemorySegment segment = arena.allocate(count * Long.BYTES, vectorBytes);
    for (int i = 0; i < count; i++) {
      segment.setAtIndex(ValueLayout.JAVA_LONG, i, i + 1);
    }

    // The offset is in bytes, not in elements
    long bound = SPECIES.loopBound(count) * (long) Long.BYTES;
    ByteOrder order = ByteOrder.nativeOrder();
    LongVector sums = LongVector.zero(SPECIES);
    long offset = 0;
    for (; offset < bound; offset += vectorBytes) {
      LongVector v = LongVector.fromMemorySegment(SPECIES, segment, offset, order);
      sums = sums.add(v);
    }
    long sum = sums.reduceLanes(ADD);
    for (; offset < segment.byteSize(); offset += Long.BYTES) {
      sum += segment.get(ValueLayout.JAVA_LONG, offset);
    }
    IO.println("Sum of 1 to " + count + ": " + sum);
  }
}
