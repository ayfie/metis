package net.adeptropolis.nephila.graph.implementations;

import com.google.common.util.concurrent.AtomicDouble;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.Matchers.is;

public class CSRTraversalTest {

  @Test
  public void traversalVisitsAllEntries() {
    withLargeDenseMatrix(mat -> {
      FingerprintingVisitor visitor = new FingerprintingVisitor();
      mat.defaultView().traverse(visitor);
      MatcherAssert.assertThat(visitor.getFingerprint(), is(582167083500d));
    });
  }

  @Test
  public void traversalIgnoresNonSelectedEntries() {
    withLargeDenseMatrix(mat -> {
      FingerprintingVisitor visitor = new FingerprintingVisitor();
      CSRStorage.View view = mat.view(indicesWithSize(999));
      view.traverse(visitor);
      MatcherAssert.assertThat(visitor.getFingerprint(), is(579840743502d));
    });
  }

  @Test
  public void traversalAllowsReuse() {
    withLargeDenseMatrix(mat -> {
      FingerprintingVisitor visitor = new FingerprintingVisitor();
      CSRStorage.View view = mat.view(indicesWithSize(999));
      view.traverse(visitor);
      MatcherAssert.assertThat(visitor.getFingerprint(), is(579840743502d));
      view = mat.view(indicesWithSize(998));
      view.traverse(visitor);
      MatcherAssert.assertThat(visitor.getFingerprint(), is(577521382520d));
    });
  }

  private int[] indicesWithSize(int size) {
    int[] indices = new int[size];
    for (int i = 0; i < size; i++) indices[i] = i;
    return indices;
  }

  private void withLargeDenseMatrix(Consumer<CSRStorage> storageConsumer) {
    CSRStorageBuilder builder = new CSRStorageBuilder();
    for (int i = 0; i < 1000; i++) {
      for (int j = i + 1; j < 1000; j++) {
        builder.addSymmetric(i, j, i + j);
      }
    }
    CSRStorage storage = builder.build();
    storageConsumer.accept(storage);
    storage.free();
  }

  class FingerprintingVisitor implements EntryVisitor {

    private final AtomicDouble fingerprint;

    FingerprintingVisitor() {
      fingerprint = new AtomicDouble();
    }

    double getFingerprint() {
      return fingerprint.get();
    }

    @Override
    public void visit(int rowIdx, int colIdx, double value) {
      burnCycles();
      fingerprint.addAndGet(rowIdx * value + colIdx);
    }

    @Override
    public void reset() {
      fingerprint.set(0);
    }

    private void burnCycles() {
      double sum = 0;
      for (int i = 0; i < 5000; i++) sum += Math.sqrt(i);
      if (Math.round(sum) % 12345 == 0) System.out.println("Ignore this");
    }
  }

}