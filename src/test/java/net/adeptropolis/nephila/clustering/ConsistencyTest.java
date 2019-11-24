package net.adeptropolis.nephila.clustering;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterators;
import net.adeptropolis.nephila.graphs.Graph;
import net.adeptropolis.nephila.graphs.GraphTestBase;
import net.adeptropolis.nephila.graphs.VertexIterator;
import net.adeptropolis.nephila.graphs.implementations.CompressedSparseGraph;
import net.adeptropolis.nephila.graphs.implementations.CompressedSparseGraphBuilder;
import org.junit.Test;

import java.util.Comparator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class ConsistencyTest extends GraphTestBase {

  @Test
  public void sizeBelowThreshold() {
    Cluster cluster = new Cluster(null);
    CompressedSparseGraph graph = new CompressedSparseGraphBuilder()
            .add(50, 51, 1)
            .add(51,52, 1)
            .add(52,53, 1)
            .build();
    Graph candidate = graph.inducedSubgraph(IntIterators.wrap(new int[]{50, 51, 52}));
    Consistency consistency = new Consistency(graph, 10, 0.0);
    Graph consistentSubgraph = consistency.ensure(cluster, candidate);
    assertNull(consistentSubgraph);
    assertThat(cluster.getRemainder(), is(IntArrayList.wrap(new int[]{50, 51, 52})));
  }

  @Test
  public void filteringOutCascade() {
    Cluster cluster = new Cluster(null);
    CompressedSparseGraph graph = defaultGraph();
    Graph candidate = defaultCandidate(graph);
    Consistency consistency = new Consistency(graph, 0, 0.75);
    Graph consistentSubgraph = consistency.ensure(cluster, candidate);
    assertNotNull(consistentSubgraph);
    cluster.getRemainder().sort(Comparator.comparingInt(x -> x));
    assertThat(cluster.getRemainder(), is(IntArrayList.wrap(new int[]{52, 53})));
    IntArrayList consistentVertices = new IntArrayList();
    VertexIterator it = consistentSubgraph.vertexIterator();
    while (it.hasNext()) {
      consistentVertices.add(it.globalId());
    }
    consistentVertices.sort(Comparator.comparingInt(x -> x));
    assertThat(consistentVertices, is(IntArrayList.wrap(new int[]{50, 51})));
  }

  @Test
  public void sizeFallsShortDuringIteration() {
    Cluster cluster = new Cluster(null);
    CompressedSparseGraph graph = defaultGraph();
    Graph candidate = defaultCandidate(graph);
    Consistency consistency = new Consistency(graph, 3, 0.75);
    Graph consistentSubgraph = consistency.ensure(cluster, candidate);
    assertNull(consistentSubgraph);
    cluster.getRemainder().sort(Comparator.comparingInt(x -> x));
    assertThat(cluster.getRemainder(), is(IntArrayList.wrap(new int[]{50, 51, 52, 53})));
  }

  private CompressedSparseGraph defaultGraph() {
    return new CompressedSparseGraphBuilder()
            .add(50, 51, 10)
            .add(51, 52, 1)
            .add(52, 53, 1)
            .add(53, 54, 9)
            .build();
  }

  private Graph defaultCandidate(CompressedSparseGraph graph) {
    return graph.inducedSubgraph(IntIterators.wrap(new int[]{50, 51, 52, 53}));
  }

}