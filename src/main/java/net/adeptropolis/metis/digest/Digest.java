/*
 * Copyright Florian Schaefer 2019.
 * SPDX-License-Identifier: Apache-2.0
 */

package net.adeptropolis.metis.digest;

/**
 * <p>A cluster digest</p>
 * <p>Digests are precursors of final cluster outputs and hold three index-aligned lists or vertices, weights and cluster
 * likelihood scores that aggregated from a given cluster and sorted according to some criterion. Digests may either store
 * all cluster vertices or just the top-ranked subset.
 * </p>
 */

public class Digest {

  private final int[] vertices;
  private final double[] weights;
  private final double[] scores;
  private final int totalSize;

  /**
   * Constructor
   *
   * @param vertices  (Aggregated) cluster vertices
   * @param weights   Vertex weights
   * @param scores    Vertex cluster likelihood scores
   * @param totalSize Total size of available vertices. Useful if the digest doesn't contain all cluster vertices.
   */

  Digest(int[] vertices, double[] weights, double[] scores, int totalSize) {
    this.vertices = vertices;
    this.weights = weights;
    this.scores = scores;
    this.totalSize = totalSize;
  }

  /**
   * @return Vertices
   */

  public int[] getVertices() {
    return vertices;
  }

  /**
   * @return Vertex weights
   */

  public double[] getWeights() {
    return weights;
  }

  /**
   * @return Cluster likelihood scores
   */

  public double[] getScores() {
    return scores;
  }

  /**
   * @return Size of the digest
   */

  public int size() {
    return vertices.length;
  }

  /**
   * @return Total cluster size (In case the digest has been cut off at some point)
   */

  public int totalSize() {
    return totalSize;
  }
}