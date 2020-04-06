/*
 * Copyright (c) Florian Schaefer 2020.
 * SPDX-License-Identifier: Apache-2.0
 */

package net.adeptropolis.metis.digest;

import org.junit.Before;
import org.junit.Test;

import static net.adeptropolis.metis.digest.ClusterDigester.DESCENDING_SCORES;
import static net.adeptropolis.metis.digest.ClusterDigester.DESCENDING_WEIGHTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

public class MemberSortOpsTest {

  private int[] vertices;
  private double[] weights;
  private double[] scores;

  @Before
  public void initialize() {
    vertices = new int[]{1, 2};
    weights = new double[]{10d, 20d};
    scores = new double[]{100d, 200d};
  }

  @Test
  public void sortByCustomAscendingVertex() {
    sort((vertices, weights, scores, i, j) -> Double.compare(vertices[i], vertices[j]));
    assertIsAscending();
  }

  @Test
  public void sortByCustomDescendingVertex() {
    sort((vertices, weights, scores, i, j) -> Double.compare(vertices[j], vertices[i]));
    assertIsDescending();
  }

  @Test
  public void sortByWeight() {
    sort(DESCENDING_WEIGHTS);
    assertIsDescending();
  }

  @Test
  public void sortByCustomAscendingWeight() {
    sort((vertices, weights, scores, i, j) -> Double.compare(weights[i], weights[j]));
    assertIsAscending();
  }

  @Test
  public void sortByScore() {
    sort(DESCENDING_SCORES);
    assertIsDescending();
  }

  @Test
  public void sortByCustomAscendingScore() {
    sort((vertices, weights, scores, i, j) -> Double.compare(scores[i], scores[j]));
    assertIsAscending();
  }

  private void assertIsAscending() {
    assertThat(vertices[0], is(1));
    assertThat(vertices[1], is(2));
    assertThat(weights[0], closeTo(10d, 1E-6));
    assertThat(weights[1], closeTo(20d, 1E-6));
    assertThat(scores[0], closeTo(100d, 1E-6));
    assertThat(scores[1], closeTo(200d, 1E-6));
  }

  private void assertIsDescending() {
    assertThat(vertices[0], is(2));
    assertThat(vertices[1], is(1));
    assertThat(weights[0], closeTo(20d, 1E-6));
    assertThat(weights[1], closeTo(10d, 1E-6));
    assertThat(scores[0], closeTo(200d, 1E-6));
    assertThat(scores[1], closeTo(100d, 1E-6));
  }

  private void sort(ClusterMemberComparator comparator) {
    MemberSortOps.sort(vertices, weights, scores, comparator);
  }

}