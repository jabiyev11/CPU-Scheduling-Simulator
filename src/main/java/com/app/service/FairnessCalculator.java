package com.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FairnessCalculator {
  private FairnessCalculator() {
  }

  static double jainsFairness(List<Double> values) {
    if (values.isEmpty()) {
      return 0.0;
    }
    double sum = 0.0;
    double sumSquares = 0.0;
    for (double value : values) {
      sum += value;
      sumSquares += value * value;
    }
    if (sumSquares == 0.0) {
      return 0.0;
    }
    return (sum * sum) / (values.size() * sumSquares);
  }

  // Standard Gini via sorted-rank formula: 0 = equal, 1 = maximally unequal
  static double giniCoefficient(List<Double> values) {
    if (values.isEmpty()) {
      return 0.0;
    }
    List<Double> sorted = new ArrayList<>(values);
    Collections.sort(sorted);

    double sum = 0.0;
    for (double value : sorted) {
      sum += value;
    }
    if (sum == 0.0) {
      return 0.0;
    }

    int n = sorted.size();
    double weightedSum = 0.0;
    for (int i = 0; i < n; i++) {
      weightedSum += (i + 1) * sorted.get(i);
    }
    return (2.0 * weightedSum) / (n * sum) - ((double) (n + 1) / n);
  }

  static double minMaxFairnessGap(List<Double> values) {
    if (values.isEmpty()) {
      return 0.0;
    }
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;
    for (double value : values) {
      min = Math.min(min, value);
      max = Math.max(max, value);
    }
    return max - min;
  }
}
