/*
 * Copyright (c) Florian Schaefer 2019.
 *
 * This file is subject to the terms and conditions defined in the
 * file 'LICENSE.txt', which is part of this source code package.
 */

package net.adeptropolis.nephila.clustering.labeling;

import it.unimi.dsi.fastutil.ints.IntIterators;
import net.adeptropolis.nephila.clustering.Cluster;
import net.adeptropolis.nephila.graphs.implementations.CompressedSparseGraph;
import net.adeptropolis.nephila.graphs.implementations.CompressedSparseGraphBuilder;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TopWeightsAggregateLabelingTest {

  @Test
  public void basicFunctionality() {
    CompressedSparseGraph graph = new CompressedSparseGraphBuilder()
            .add(0, 1, 1)
            .add(0, 2, 2)
            .add(0, 3, 3)
            .add(0, 4, 4)
            .add(0, 5, 5)
            .add(0, 6, 6)
            .add(1, 2, 7)
            .add(1, 3, 8)
            .add(1, 4, 9)
            .add(1, 5, 10)
            .add(1, 6, 11)
            .add(2, 3, 12)
            .add(4, 5, 13)
            .add(4, 6, 14)
            .add(4, 7, 15)
            .add(4, 8, 16)
            .add(4, 9, 17)
            .add(5, 6, 18)
            .add(5, 7, 19)
            .add(5, 8, 20)
            .add(5, 9, 21)
            .add(6, 7, 22)
            .add(8, 9, 23)
            .build();
    Cluster root = new Cluster(null);
    root.addToRemainder(IntIterators.wrap(new int[]{0, 1}));
    Cluster c1 = new Cluster(root);
    c1.addToRemainder(IntIterators.wrap(new int[]{2, 3}));
    Cluster c2 = new Cluster(root);
    c2.addToRemainder(IntIterators.wrap(new int[]{4, 5}));
    Cluster c21 = new Cluster(c2);
    c2.addToRemainder(IntIterators.wrap(new int[]{6, 7}));
    Cluster c22 = new Cluster(c2);
    c2.addToRemainder(IntIterators.wrap(new int[]{8, 9}));
    Labels labels = new TopWeightsAggregateLabeling(3, graph).label(c2);
    assertThat(labels.getVertices().length, is(3));
    assertThat(labels.getVertices()[0], is(5));
    assertThat(labels.getVertices()[1], is(4));
    assertThat(labels.getVertices()[2], is(9));
    assertThat(labels.getWeights().length, is(3));
    assertThat(labels.getWeights()[0], closeTo(91, 1E-9));
    assertThat(labels.getWeights()[1], closeTo(75, 1E-9));
    assertThat(labels.getWeights()[2], closeTo(61, 1E-9));
    assertThat(labels.getLikelihoods().length, is(3));
    assertThat(labels.getLikelihoods()[0], closeTo(0.8584905660377359, 1E-9));
    assertThat(labels.getLikelihoods()[1], closeTo(0.8522727272727273, 1E-9));
    assertThat(labels.getLikelihoods()[2], closeTo(1.0, 1E-9));
  }

  @Test
  public void smallGraph() {
    CompressedSparseGraph graph = new CompressedSparseGraphBuilder()
            .add(0, 1, 1)
            .add(0, 2, 2)
            .build();
    Cluster root = new Cluster(null);
    root.addToRemainder(IntIterators.wrap(new int[]{0, 1}));
    Labels labels = new TopWeightsAggregateLabeling(3, graph).label(root);
    assertThat(labels.getVertices().length, is(2));
    assertThat(labels.getWeights().length, is(2));
    assertThat(labels.getLikelihoods().length, is(2));
  }

  @Test
  public void emptyGraph() {
    CompressedSparseGraph graph = new CompressedSparseGraphBuilder().build();
    Cluster root = new Cluster(null);
    root.addToRemainder(IntIterators.wrap(new int[]{}));
    Labels labels = new TopWeightsAggregateLabeling(3, graph).label(root);
    assertThat(labels.getVertices().length, is(0));
    assertThat(labels.getWeights().length, is(0));
    assertThat(labels.getLikelihoods().length, is(0));
  }


}