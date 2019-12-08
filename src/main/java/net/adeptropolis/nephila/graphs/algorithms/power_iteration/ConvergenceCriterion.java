/*
 * Copyright (c) Florian Schaefer 2019.
 *
 * This file is subject to the terms and conditions defined in the
 * file 'LICENSE.txt', which is part of this source code package.
 */

package net.adeptropolis.nephila.graphs.algorithms.power_iteration;

/**
 * A convergence criterion for the power iteration
 */

@FunctionalInterface
public interface ConvergenceCriterion {

  /**
   * <p>Assess whether the power iteration has converged</p>
   *
   * @param previous   Result of the previous iteration
   * @param current    Result of the current iteration
   * @param iterations Number of iterations
   * @return True if and only if the convergence criterion is satisfied.
   */

  boolean satisfied(double[] previous, double[] current, int iterations);

}
