/*
 * Copyright Florian Schaefer 2019.
 * SPDX-License-Identifier: Apache-2.0
 */

package net.adeptropolis.metis.graphs.implementations.arrays;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LongMergeSortTest {

  @Test
  public void tinyList() {
    BigInts sorted = BigInts.of(4, 2, 1, 3).sort();
    assertThat(sorted, is(BigInts.of(1, 2, 3, 4)));
  }

  @Test
  public void mergeSort() {
    int size = 1 << 20;
    BigInts b = new BigInts(size);
    for (int i = 0; i < size; i++) b.set(i, size - i - 1);
    LongMergeSort.mergeSort(0, size, b);
    for (int i = 0; i < size; i++) assertThat(b.get(i), is(i));
  }

}