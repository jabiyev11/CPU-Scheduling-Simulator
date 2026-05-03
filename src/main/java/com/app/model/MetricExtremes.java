package com.app.model;

import com.app.service.MetricExtractor;
import java.util.Comparator;
import java.util.List;

public record MetricExtremes(
    SchedulingAlgorithm bestWaiting,
    SchedulingAlgorithm worstWaiting,
    SchedulingAlgorithm bestTurnaround,
    SchedulingAlgorithm worstTurnaround,
    SchedulingAlgorithm bestResponse,
    SchedulingAlgorithm worstResponse,
    SchedulingAlgorithm bestCpuUtil,
    SchedulingAlgorithm worstCpuUtil,
    SchedulingAlgorithm bestThroughput,
    SchedulingAlgorithm worstThroughput,
    SchedulingAlgorithm bestJainsFairness,
    SchedulingAlgorithm worstJainsFairness,
    SchedulingAlgorithm bestGini,
    SchedulingAlgorithm worstGini,
    SchedulingAlgorithm bestMinMaxGap,
    SchedulingAlgorithm worstMinMaxGap
) {

  public static MetricExtremes empty() {
    return new MetricExtremes(
        null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null
    );
  }

  public static MetricExtremes from(List<SchedulerResult> results) {
    // Note the polarity flip for some metrics: cpuUtil/throughput/jain are higher-is-better,
    return new MetricExtremes(
        min(results, SchedulerResult::avgWaitingTime),
        max(results, SchedulerResult::avgWaitingTime),
        min(results, SchedulerResult::avgTurnaroundTime),
        max(results, SchedulerResult::avgTurnaroundTime),
        min(results, SchedulerResult::avgResponseTime),
        max(results, SchedulerResult::avgResponseTime),
        max(results, SchedulerResult::cpuUtilization),
        min(results, SchedulerResult::cpuUtilization),
        max(results, SchedulerResult::throughput),
        min(results, SchedulerResult::throughput),
        max(results, SchedulerResult::jainsFairness),
        min(results, SchedulerResult::jainsFairness),
        min(results, SchedulerResult::giniCoefficient),
        max(results, SchedulerResult::giniCoefficient),
        min(results, SchedulerResult::minMaxFairnessGap),
        max(results, SchedulerResult::minMaxFairnessGap)
    );
  }

  public boolean isBest(MetricType type, SchedulingAlgorithm algorithm) {
    return switch (type) {
      case AVG_WAITING -> algorithm == bestWaiting;
      case AVG_TURNAROUND -> algorithm == bestTurnaround;
      case AVG_RESPONSE -> algorithm == bestResponse;
      case CPU_UTIL -> algorithm == bestCpuUtil;
      case THROUGHPUT -> algorithm == bestThroughput;
      case JAINS_FAIRNESS -> algorithm == bestJainsFairness;
      case GINI -> algorithm == bestGini;
      case MIN_MAX_GAP -> algorithm == bestMinMaxGap;
    };
  }

  public boolean isWorst(MetricType type, SchedulingAlgorithm algorithm) {
    return switch (type) {
      case AVG_WAITING -> algorithm == worstWaiting;
      case AVG_TURNAROUND -> algorithm == worstTurnaround;
      case AVG_RESPONSE -> algorithm == worstResponse;
      case CPU_UTIL -> algorithm == worstCpuUtil;
      case THROUGHPUT -> algorithm == worstThroughput;
      case JAINS_FAIRNESS -> algorithm == worstJainsFairness;
      case GINI -> algorithm == worstGini;
      case MIN_MAX_GAP -> algorithm == worstMinMaxGap;
    };
  }

  public static SchedulingAlgorithm min(List<SchedulerResult> results,
                                         MetricExtractor extractor) {
    return results.stream().min(Comparator.comparingDouble(extractor::extract))
        .map(SchedulerResult::algorithm).orElse(null);
  }

  public static SchedulingAlgorithm max(List<SchedulerResult> results,
                                         MetricExtractor extractor) {
    return results.stream().max(Comparator.comparingDouble(extractor::extract))
        .map(SchedulerResult::algorithm).orElse(null);
  }
}
