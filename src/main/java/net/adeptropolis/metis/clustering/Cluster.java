/*
 * Copyright Florian Schaefer 2019.
 * SPDX-License-Identifier: Apache-2.0
 */

package net.adeptropolis.metis.clustering;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.adeptropolis.metis.graphs.Graph;
import net.adeptropolis.metis.graphs.VertexIterator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

// TODO: This class is by far the most messy part of the whole project. Find a way to unravel all the mixed concerns we have here!

public class Cluster {

  private final Set<Cluster> children;
  private Cluster parent;
  private IntArrayList remainder;

  public Cluster(Cluster parent) {
    this.parent = parent;
    this.children = new HashSet<>();
    this.remainder = new IntArrayList();
    if (parent != null) parent.children.add(this);
  }

  public void assimilateChild(Cluster child, boolean assimilateRemainder) {
    if (children.remove(child)) {
      for (Cluster grandchild : child.children) {
        grandchild.parent = this;
      }
      addChildren(child.getChildren());
      if (assimilateRemainder) {
        remainder.addAll(child.getRemainder());
      }
    } else {
      throw new RuntimeException("WTF?!");
    }
  }

  public void annex(Cluster cluster) {
    if (cluster.parent.children.remove(cluster)) {
      cluster.parent = this;
      children.add(cluster);
    }
  }

  public void assimilateChild(Cluster child) {
    assimilateChild(child, true);
  }

  public IntArrayList getRemainder() {
    return remainder;
  }

  public void setRemainder(IntArrayList remainder) {
    this.remainder = remainder;
  }

  public Set<Cluster> getChildren() {
    return children;
  }

  public Cluster getParent() {
    return parent;
  }

  public void addToRemainder(int globalId) {
    remainder.add(globalId);
  }

  public void addToRemainder(IntIterator it) {
    while (it.hasNext()) {
      addToRemainder(it.nextInt());
    }
  }

  void addToRemainder(Graph graph) {
    remainder.ensureCapacity(remainder.size() + graph.order());
    VertexIterator vertexIterator = graph.vertexIterator();
    while (vertexIterator.hasNext()) {
      remainder.add(vertexIterator.globalId());
    }
  }

  public void addChildren(Collection<Cluster> newChildren) {
    children.addAll(newChildren);
  }

  public void traverse(Consumer<Cluster> consumer) {
    consumer.accept(this);
    for (Cluster child : children) {
      child.traverse(consumer);
    }
  }

  public void traverseLeafs(Consumer<Cluster> consumer) {
    traverse(cluster -> {
      if (cluster.getChildren().isEmpty()) {
        consumer.accept(cluster);
      }
    });
  }

  public IntArrayList aggregateVertices() {
    IntArrayList vertices = new IntArrayList();
    traverse(cluster -> vertices.addAll(cluster.remainder));
    return vertices;
  }

  /**
   * Depth of this cluster within the overall hierarchy
   *
   * @return depth
   */

  public int depth() {
    int depth = 0;
    Cluster ptr = this;
    while (ptr.getParent() != null) {
      depth++;
      ptr = ptr.getParent();
    }
    return depth;
  }

  /**
   * @return The root cluster
   */

  public Cluster root() {
    Cluster ptr = this;
    while (ptr.getParent() != null) {
      ptr = ptr.getParent();
    }
    return ptr;
  }

  public Graph aggregateGraph(Graph rootGraph) {
    return rootGraph.inducedSubgraph(aggregateVertices().iterator());
  }

  public Graph remainderGraph(Graph rootGraph) {
    return rootGraph.inducedSubgraph(remainder.iterator());
  }

  public Set<Cluster> aggregateClusters() {
    Set<Cluster> clusters = new HashSet<>();
    traverse(clusters::add);
    return clusters;
  }

  /**
   * Equals
   *
   * <p>Please note that due to the fact that clusters may be wildly modified in the process and still two clusters should only
   * be regarded as equal if they refer to the same reference. The call below is just there as a reminder of this fact.</p>
   *
   * @param obj The reference object with which to compare.
   * @return True if this object is the same as the obj argument; false otherwise.
   */

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  /**
   * Hash Code
   *
   * <p>Please note that due to the fact that clusters may be wildly modified in the process and still two clusters should only
   * yield the same hash code if they refer to the same reference. The call below is just there as a reminder of this fact.</p>
   *
   * @return A hash code value for this object.
   */

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}