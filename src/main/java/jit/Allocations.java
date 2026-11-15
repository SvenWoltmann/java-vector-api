import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import jdk.incubator.vector.*;

// How many bytes the heap sees per vector operation. With C2, the vectors
// live in registers; stored in an array, they become objects. Run it a second
// time with -XX:TieredStopAtLevel=1 (no C2) to see the Java fallback.

static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_128;
static final ThreadMXBean THREADS = (ThreadMXBean) ManagementFactory.getThreadMXBean();
static final int ROUNDS = 100_000;

int[] a = new int[1_024];
int[] b = new int[1_024];
int[] c = new int[1_024];
IntVector[] kept = new IntVector[1_024 / 4];

void main() {
  for (int warmUp = 0; warmUp < 20_000; warmUp++) {
    add();
    addAndKeep();
  }
  IO.println("add():        " + bytesPerOperation(this::add) + " bytes per vector");
  IO.println("addAndKeep(): " + bytesPerOperation(this::addAndKeep) + " bytes per vector");
}

// Result goes straight back into an int array
void add() {
  for (int i = 0; i < a.length; i += SPECIES.length()) {
    IntVector va = IntVector.fromArray(SPECIES, a, i);
    IntVector vb = IntVector.fromArray(SPECIES, b, i);
    va.add(vb).intoArray(c, i);
  }
}

// Result is stored as an object, so it must exist on the heap
void addAndKeep() {
  for (int i = 0; i < a.length; i += SPECIES.length()) {
    IntVector va = IntVector.fromArray(SPECIES, a, i);
    IntVector vb = IntVector.fromArray(SPECIES, b, i);
    kept[i / 4] = va.add(vb);
  }
}

long bytesPerOperation(Runnable operation) {
  long before = THREADS.getCurrentThreadAllocatedBytes();
  for (int round = 0; round < ROUNDS; round++) {
    operation.run();
  }
  long after = THREADS.getCurrentThreadAllocatedBytes();
  return (after - before) / ((long) ROUNDS * (a.length / SPECIES.length()));
}
