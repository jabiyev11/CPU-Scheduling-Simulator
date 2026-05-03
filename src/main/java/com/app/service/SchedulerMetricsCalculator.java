package com.app.service;

import com.app.model.LogEntry;
import com.app.model.Process;
import com.app.model.ProcessResult;
import com.app.model.SchedulerResult;
import com.app.model.SchedulingAlgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class SchedulerMetricsCalculator {
    private SchedulerMetricsCalculator() {
    }

    static SchedulerResult buildResult(
        SchedulingAlgorithm algorithm,
        List<Process> processes,
        List<LogEntry> scheduleLog
    ) {
        List<Process> sortedByPid = new ArrayList<>(processes);
        sortedByPid.sort(Comparator.comparingInt(Process::getPid));

        List<ProcessResult> perProcess = new ArrayList<>(sortedByPid.size());
        double totalWaiting = 0;
        double totalTurnaround = 0;
        double totalResponse = 0;
        int minArrival = Integer.MAX_VALUE;
        int maxCompletion = Integer.MIN_VALUE;

        // Build per process table rows and running sums for aggregate averages
        for (Process process : sortedByPid) {
            perProcess.add(new ProcessResult(
                process.getPid(),
                "P" + process.getPid(),
                process.getArrivalTime(),
                process.getBurstTime(),
                process.getCompletionTime(),
                process.getWaitingTime(),
                process.getTurnaroundTime(),
                process.getResponseTime()
            ));
            totalWaiting += process.getWaitingTime();
            totalTurnaround += process.getTurnaroundTime();
            totalResponse += process.getResponseTime();
            minArrival = Math.min(minArrival, process.getArrivalTime());
            maxCompletion = Math.max(maxCompletion, process.getCompletionTime());
        }

        int processCount = perProcess.size();
        double avgWaiting = processCount == 0 ? 0 : totalWaiting / processCount;
        double avgTurnaround = processCount == 0 ? 0 : totalTurnaround / processCount;
        double avgResponse = processCount == 0 ? 0 : totalResponse / processCount;

        // CPU Utilization (%) = (busyTime / duration) * 100
        double busyTime = scheduleLog.stream().mapToInt(e -> e.end() - e.start()).sum();
        double duration = Math.max(1, maxCompletion - minArrival);
        double cpuUtilization = (busyTime / duration) * 100.0;
        // Throughput = completedProcesses / duration
        double throughput = processCount / duration;

        // Fairness vector based on achieved service share for each process.
        // A higher value means a process receives proportionally better service.
        List<Double> serviceShares = new ArrayList<>(sortedByPid.size());
        for (Process process : sortedByPid) {
            double turnaround = Math.max(1.0, process.getTurnaroundTime());
            serviceShares.add(process.getBurstTime() / turnaround);
        }
        double jainsFairness = FairnessCalculator.jainsFairness(serviceShares);
        double giniCoefficient = FairnessCalculator.giniCoefficient(serviceShares);
        double minMaxFairnessGap = FairnessCalculator.minMaxFairnessGap(serviceShares);

        return new SchedulerResult(
            algorithm,
            List.copyOf(perProcess),
            avgWaiting,
            avgTurnaround,
            avgResponse,
            cpuUtilization,
            throughput,
            jainsFairness,
            giniCoefficient,
            minMaxFairnessGap,
            List.copyOf(scheduleLog)
        );
    }
}
