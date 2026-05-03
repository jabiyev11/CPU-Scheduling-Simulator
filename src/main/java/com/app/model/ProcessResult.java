package com.app.model;

public record ProcessResult(
    int processId,
    String processName,
    int arrivalTime,
    int burstTime,
    int completionTime,
    int waitingTime,
    int turnaroundTime,
    int responseTime
) {
}
