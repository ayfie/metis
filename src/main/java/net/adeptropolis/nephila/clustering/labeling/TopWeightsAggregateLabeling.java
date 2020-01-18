/*
 * Copyright (c) Florian Schaefer 2019.
 *
 * This file is subject to the terms and conditions defined in the
 * file 'LICENSE.txt', which is part of this source code package.
 */

package net.adeptropolis.nephila.clustering.labeling;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntComparator;
import net.adeptropolis.nephila.clustering.Cluster;
import net.adeptropolis.nephila.graphs.Graph;
import net.adeptropolis.nephila.graphs.VertexIterator;
import net.adeptropolis.nephila.helpers.Arr;

/**
 * Returns the labels of the full aggregated subgraph, sorted by frequency
 */

public class TopWeightsAggregateLabeling implements Labeling {

  private final int maxLabels;
  private final Graph rootGraph;

  public TopWeightsAggregateLabeling(int maxLabels, Graph rootGraph) {
    this.maxLabels = maxLabels;
    this.rootGraph = rootGraph;
  }

  @Override
  public Labels label(Cluster cluster) {
    Graph graph = cluster.aggregateGraph(rootGraph);
    int[] vertices = graph.collectVertices();
    double[] weights = graph.weights();
    double[] likelihoods = graph.relativeWeights(rootGraph);
    WeightSortOps weightSortOps = new WeightSortOps(vertices, weights, likelihoods);
    Arrays.mergeSort(0, graph.size(), weightSortOps, weightSortOps);
    return new Labels(
            Arr.shrink(vertices, maxLabels),
            Arr.shrink(weights, maxLabels),
            Arr.shrink(likelihoods, maxLabels),
            graph.size()
    );
  }

}
