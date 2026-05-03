package com.app.model;

import java.util.List;

public record SchedulerResult(
    SchedulingAlgorithm algorithm,
    List<ProcessResult> perProcess,
    double avgWaitingTime,
    double avgTurnaroundTime,
    double avgResponseTime,
    double cpuUtilization,
    double throughput,
    double jainsFairness,
    double giniCoefficient,
    double minMaxFairnessGap,
    List<LogEntry> scheduleLog
) {
}
