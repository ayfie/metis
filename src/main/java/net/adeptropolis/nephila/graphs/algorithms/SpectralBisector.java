package net.adeptropolis.nephila.graphs.algorithms;

import it.unimi.dsi.fastutil.ints.IntIterators;
import net.adeptropolis.nephila.graphs.Graph;
import net.adeptropolis.nephila.graphs.algorithms.power_iteration.ConstantInitialVectors;
import net.adeptropolis.nephila.graphs.algorithms.power_iteration.ConvergenceCriterion;
import net.adeptropolis.nephila.graphs.algorithms.power_iteration.PowerIteration;
import net.adeptropolis.nephila.graphs.operators.SSNLOperator;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SpectralBisector {

  /**
   * <p>Spectral bisector for biparite graphs</p>
   * <p>The original graph will be split into two partitions such that the normalized cut is minimized</p>
   */

  private final ConvergenceCriterion convergenceCriterion;

  /**
   *
   * @param convergenceCriterion The convergence criterion. Usually, this is an instance of <code>SignumConvergence</code>.
   */

  public SpectralBisector(ConvergenceCriterion convergenceCriterion) {
    this.convergenceCriterion = convergenceCriterion;
  }

  /**
   * Bisects the given graph into two partitons
   * @param graph The input graph
   * @param consumer A consumer for the resulting partitions
   * @param maxIterations Maximum number of iterations
   * @throws PowerIteration.MaxIterationsExceededException
   */

  public void bisect(Graph graph, Consumer<Graph> consumer, int maxIterations) throws PowerIteration.MaxIterationsExceededException {
    SSNLOperator ssnl = new SSNLOperator(graph);
    double[] iv = ConstantInitialVectors.generate(graph.size());
    double[] v2 = PowerIteration.apply(ssnl, convergenceCriterion, iv, maxIterations);
    yieldSubgraph(graph, v2, consumer, 1);
    yieldSubgraph(graph, v2, consumer, -1);
  }

  /**
   *
   * @param graph The input graph
   * @param v2 The approximate second-smallest eigenvector of the Normalized Laplacian of the Graph
   * @param consumer A consumer for the resulting partition
   * @param selectSignum Select either all non-negative (selectSignum >= 0) or negative (selectSignum < 0) entries from the eigenvector.
   */

  private static void yieldSubgraph(Graph graph, double[] v2, Consumer<Graph> consumer, int selectSignum) {
    SignumSelectingIndexIterator vertices = new SignumSelectingIndexIterator(v2, selectSignum);
    consumer.accept(graph.localInducedSubgraph(vertices));
  }

}