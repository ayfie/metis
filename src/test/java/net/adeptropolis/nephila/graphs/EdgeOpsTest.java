/*
 * Copyright (c) Florian Schaefer 2019.
 *
 * This file is subject to the terms and conditions defined in the
 * file 'LICENSE.txt', which is part of this source code package.
 */

package net.adeptropolis.nephila.graphs;

import net.adeptropolis.nephila.graphs.implementations.CompressedSparseGraph;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EdgeOpsTest extends GraphTestBase implements Thread.UncaughtExceptionHandler {

  @Test
  @Ignore("Intended for performance debugging")
  public void perfTest() {
    Graph graph = bandedGraph(120000, 20);
    while (true) {
      traverseFingerprint(graph);
    }
  }

  @Test
  public void emptyGraph() {
    CompressedSparseGraph graph = CompressedSparseGraph.builder().build();
    EdgeOps.traverse(graph, consumer);
    assertThat(consumer.getEdges(), is(empty()));
  }

  @Test
  public void singleEdgeGraph() {
    CompressedSparseGraph graph = CompressedSparseGraph.builder()
            .add(2, 3, 3.14)
            .build();
    EdgeOps.traverse(graph, consumer);
    assertThat(consumer.getEdges(), hasSize(2));
    assertThat(consumer.getEdges(), containsInAnyOrder(
            Edge.of(2, 3, 3.14),
            Edge.of(3, 2, 3.14)));
  }

  @Test
  public void largeBandedGraph() {
    Graph graph = bandedGraph(20000, 100);
    assertThat("Fingerprint mismatch", traverseFingerprint(graph), is(bandedGraphFingerprint(20000, 100)));
  }

  @Test
  public void parallelTraversal() {
    List<Thread> threads = IntStream.range(0, 50).mapToObj(i -> {
      Thread thread = new Thread(() -> {
        Graph graph = bandedGraph(10000, 30);
        FingerprintingEdgeConsumer fp = new FingerprintingEdgeConsumer();
        EdgeOps.traverse(graph, fp);
        assertThat("Fingerprint mismatch", fp.getFingerprint(), is(bandedGraphFingerprint(10000, 30)));
      });
      thread.setUncaughtExceptionHandler(this);
      thread.start();
      return thread;
    }).collect(Collectors.toList());
    threads.forEach(t -> {
      try {
        t.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    throw new RuntimeException(thread.getName(), throwable);
  }
}

