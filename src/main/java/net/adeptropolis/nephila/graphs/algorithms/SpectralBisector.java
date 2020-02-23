/*
 * Copyright (c) Florian Schaefer 2019.
 *
 * This file is subject to the terms and conditions defined in the
 * file 'LICENSE.txt', which is part of this source code package.
 */

package net.adeptropolis.nephila.graphs.algorithms;

import net.adeptropolis.nephila.ClusteringSettings;
import net.adeptropolis.nephila.graphs.Graph;
import net.adeptropolis.nephila.graphs.algorithms.power_iteration.PartialConvergenceCriterion;
import net.adeptropolis.nephila.graphs.algorithms.power_iteration.PowerIteration;
import net.adeptropolis.nephila.graphs.algorithms.power_iteration.RandomInitialVectors;
import net.adeptropolis.nephila.graphs.operators.SSNLOperator;

import java.util.function.Consumer;

public class SpectralBisector {

  /**
   * <p>Spectral bisector for biparite graphs</p>
   * <p>The original graph will be split into two partitions such that the normalized cut is minimized</p>
   */

  private final ClusteringSettings settings;

  public SpectralBisector(ClusteringSettings settings) {
    this.settings = settings;
  }

  /**
   * Create a subgraph based on the signum of the second-largest eigenvector of the Normalized Laplacian
   *
   * @param graph        The input graph
   * @param v2           The approximate second-smallest eigenvector of the Normalized Laplacian of the Graph
   * @param consumer     A consumer for the resulting partition
   * @param selectSignum Select either all non-negative (selectSignum ≥ 0) or negative (selectSignum &lt; 0) entries from the eigenvector.
   */

  private static void yieldSubgraph(Graph graph, double[] v2, Consumer<Graph> consumer, int selectSignum) {
    SignumSelectingIndexIterator vertices = new SignumSelectingIndexIterator(v2, selectSignum, null);
    consumer.accept(graph.localInducedSubgraph(vertices));
  }

  /**
   * Bisects the given graph into two partitons
   *
   * @param graph         The input graph
   * @param maxIterations Maximum number of iterations
   * @param consumer      A consumer for the resulting partitions
   * @throws PowerIteration.MaxIterationsExceededException if the number of iterations has been exceeded
   */

  public void bisect(Graph graph, int maxIterations, Consumer<Graph> consumer) throws PowerIteration.MaxIterationsExceededException {
    PartialConvergenceCriterion convergenceCriterion = settings.convergenceCriterionForGraph(graph);
    SSNLOperator ssnl = new SSNLOperator(graph);
    double[] iv = RandomInitialVectors.generate(graph.size());
    double[] v2 = PowerIteration.apply(ssnl, convergenceCriterion, iv, maxIterations);
    convergenceCriterion.postprocess(v2);
    yieldSubgraph(graph, v2, consumer, 1);
    yieldSubgraph(graph, v2, consumer, -1);
  }

}
