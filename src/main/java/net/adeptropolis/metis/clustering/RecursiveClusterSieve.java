/*
 * Copyright Florian Schaefer 2019.
 * SPDX-License-Identifier: Apache-2.0
 */

package net.adeptropolis.metis.clustering;

import net.adeptropolis.metis.ClusteringSettings;
import net.adeptropolis.metis.clustering.postprocessing.Postprocessing;
import net.adeptropolis.metis.graphs.Graph;
import net.adeptropolis.metis.graphs.algorithms.ConnectedComponents;
import net.adeptropolis.metis.graphs.algorithms.SpectralBisector;
import net.adeptropolis.metis.graphs.algorithms.power_iteration.PowerIteration;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.PriorityQueue;

public class RecursiveClusterSieve {

  private static final Logger LOG = LoggerFactory.getLogger(RecursiveClusterSieve.class.getSimpleName());

  private final Graph graph;
  private final ClusteringSettings settings;
  private final SpectralBisector bisector;
  private final PriorityQueue<Protocluster> queue;
  private final ConsistencyGuard consistencyGuard;

  public RecursiveClusterSieve(Graph graph, ClusteringSettings settings) {
    this.graph = graph;
    this.settings = settings;
    this.bisector = new SpectralBisector(settings);
    this.queue = new PriorityQueue<>(Comparator.comparingInt(protocluster -> -protocluster.getCluster().depth()));
    this.consistencyGuard = new ConsistencyGuard(settings.getConsistencyMetric(), graph, settings.getMinClusterSize(), settings.getMinClusterLikelihood());
  }

  public Cluster run() {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Cluster root = new Cluster(null);
    Protocluster initialProtocluster = new Protocluster(graph, Protocluster.GraphType.ROOT, root);
    queue.add(initialProtocluster);
    processQueue();
    stopWatch.stop();
    LOG.debug("Finished clustering {} vertices after {}", graph.order(), stopWatch);
    return new Postprocessing(root, graph, settings).apply();
  }

  private void processQueue() {
    while (!queue.isEmpty()) {
      Protocluster protocluster = queue.poll();
      if (protocluster.getGraphType() == Protocluster.GraphType.COMPONENT) {
        bisect(protocluster);
      } else {
        decomposeComponents(protocluster);
      }
    }
  }

  /**
   * Bisect the protocluster's graph such that the normalized cut is minimized. Possible scenarios for each partition:
   * <ol>
   *   <li>The partition is either smaller than the allowed minimum cluster size or comprises the full
   *   input graph (which hits an error) -&gt; Add its vertices to the cluster's remainder and terminate</li>
   *   <li>
   *     Ensure consistency of the resulting subgraph vertices and add all non-compliant vertices to the cluster reminder.
   *     For the remaining subgraph, one of two conditions may apply:
   *     <ol>
   *       <li>The remaining consistent subgraph is smaller than the allowed minimum cluster
   *       size -&gt; Add its vertices to the cluster's remainder and terminate</li>
   *       <li>Else: Create a new protocluster with graph type <code>SPECTRAL</code> and add it to the queue.</li>
   *     </ol>
   *   </li>
   * </ol>
   *
   * @param protocluster A protocluster
   */

  private void bisect(Protocluster protocluster) {
    try {
      bisector.bisect(protocluster.getGraph(), settings.getMaxIterations(), partition -> {
        if (partition.order() < settings.getMinClusterSize() || partition.order() == protocluster.getGraph().order()) {
          protocluster.getCluster().addToRemainder(partition);
        } else {
          Graph consistentSubgraph = consistencyGuard.ensure(protocluster.getCluster(), partition);
          if (consistentSubgraph != null && consistentSubgraph.order() > settings.getMinClusterSize()) {
            enqueueProtocluster(Protocluster.GraphType.SPECTRAL, protocluster.getCluster(), consistentSubgraph);
          }
        }
      });
    } catch (PowerIteration.MaxIterationsExceededException e) {
      LOG.warn("Exceeded maximum number of iterations ({}). Not clustering any further.", settings.getMaxIterations());
    }
  }

  /**
   * Decompose the current protocluster's graph into its connected components. There are 3 possible scenarios:
   * <ol>
   *   <li>The input graph was already fully connected -&gt; label the protocluster's graph as connected component and re-add it to the queue</li>
   *   <li>The connected component is smaller than the minimum cluster size -&gt; Add its vertices to the cluster's reminder and terminate</li>
   *   <li>Else label the component as connected and add a proper new protocluster to the queue</li>
   * </ol>
   *
   * @param protocluster A protocluster
   */

  private void decomposeComponents(Protocluster protocluster) {
    ConnectedComponents.find(protocluster.getGraph(), component -> {
      if (component.order() == protocluster.getGraph().order()) {
        protocluster.setGraphTypeConnectedComponent();
        queue.add(protocluster);
      } else if (component.order() < settings.getMinClusterSize()) {
        protocluster.getCluster().addToRemainder(component);
      } else if (component.order() > settings.getMinClusterSize()) {
        enqueueProtocluster(Protocluster.GraphType.COMPONENT, protocluster.getCluster(), component);
      }
    });
  }

  private void enqueueProtocluster(Protocluster.GraphType graphType, Cluster parent, Graph subgraph) {
    Cluster childCluster = new Cluster(parent);
    Protocluster protocluster = new Protocluster(subgraph, graphType, childCluster);
    queue.add(protocluster);
  }

}