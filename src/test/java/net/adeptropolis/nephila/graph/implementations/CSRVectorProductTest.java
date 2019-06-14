package net.adeptropolis.nephila.graph.implementations;

import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class CSRVectorProductTest {

  @Test
  public void simpleMultiplication() {
    withDefaultView(view -> {
      double[] res = new CSRVectorProduct(view).multiply(new double[]{17, 19, 23});
      assertThat(res[0], is(206.0));
      assertThat(res[1], is(437.0));
      assertThat(res[2], is(593.0));
    });
  }

  @Test
  public void subsetMultiplication() {
    withDefaultView(view -> {
      view.set(new int[]{0,2});
      double[] res = new CSRVectorProduct(view).multiply(new double[]{29, 31});
      assertThat(res[0], is(213.0));
      assertThat(res[1], is(548.0));
    });
  }


  private void withDefaultView(Consumer<CSRStorage.View> viewConsumer) {
    CSRStorage storage = new CSRStorageBuilder()
            .addSymmetric(0, 0, 2)
            .addSymmetric(0, 1, 3)
            .addSymmetric(0, 2, 5)
            .addSymmetric(1, 1, 7)
            .addSymmetric(1, 2, 11)
            .addSymmetric(2, 2, 13)
            .build();
    viewConsumer.accept(storage.defaultView());
    storage.free();
  }

}