package com.app.service;

import com.app.model.Process;
import com.app.model.ProcessResult;
import com.app.model.SchedulerResult;
import com.app.model.SchedulingAlgorithm;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class SimulationService {
    private final Random random = new Random();

    public int randomQuantum() {
        return random.nextInt(3, 5);
    }

    public int randomProcessCount() {
        return random.nextInt(2, 10);
    }

    public List<Process> randomizeProcesses(int processCount) {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < processCount; i++) {
            processes.add(new Process(
                i + 1,
                random.nextInt(0, 10),
                random.nextInt(1, 10),
                random.nextInt(1, 4),
                random.nextInt(0, 6)
            ));
        }
        return processes;
    }

    public List<SchedulerResult> runAlgorithms(
        List<Process> inputProcesses,
        int quantum,
        EnumSet<SchedulingAlgorithm> selectedAlgorithms
    ) {
        if (inputProcesses.isEmpty()) {
            throw new IllegalArgumentException("At least one process is required.");
        }
        if (quantum < 1) {
            throw new IllegalArgumentException("Quantum must be at least 1.");
        }
        // If no algorithm is selected, run all algorithms by default
        EnumSet<SchedulingAlgorithm> effectiveAlgorithms = selectedAlgorithms.isEmpty()
            ? EnumSet.allOf(SchedulingAlgorithm.class)
            : selectedAlgorithms;

        List<SchedulerResult> results = new ArrayList<>();
        for (SchedulingAlgorithm algorithm : effectiveAlgorithms) {
            List<Process> processes = deepCopy(inputProcesses);
            Map<Integer, Integer> ioByPid = ioBurstByPid(processes);
            var scheduler = schedulerFor(algorithm, quantum);
            SchedulerResult cpuOnlyResult = scheduler.schedule(processes);
            results.add(withIoAdjustedMetrics(cpuOnlyResult, ioByPid));
        }
        return results;
    }

    private Scheduler schedulerFor(SchedulingAlgorithm algorithm, int quantum) {
        return switch (algorithm) {
            case FCFS -> new FirstComeFirstServedScheduler();
            case PRIORITY -> new PriorityScheduler(quantum);
            case ROUND_ROBIN -> new RoundRobinScheduler(quantum);
            case SHORTEST_JOB_FIRST -> new ShortestJobFirstScheduler();
            case SHORTEST_REMAINING_TIME_FIRST -> new ShortestRemainingTimeFirstScheduler();
            case LOTTERY -> new LotteryScheduler(quantum);
            case GUARANTEED -> new GuaranteedScheduler(quantum);
            case MULTILEVEL_QUEUE -> new MultilevelQueueScheduler(quantum);
        };
    }

    private List<Process> deepCopy(List<Process> inputProcesses) {
        List<Process> copy = new ArrayList<>(inputProcesses.size());
        for (Process process : inputProcesses) {
            copy.add(process.copy());
        }
        return copy;
    }

    private Map<Integer, Integer> ioBurstByPid(List<Process> processes) {
        Map<Integer, Integer> ioByPid = new HashMap<>();
        for (Process process : processes) {
            ioByPid.put(process.getPid(), process.getIoBurstTime());
        }
        return ioByPid;
    }

    private SchedulerResult withIoAdjustedMetrics(
        SchedulerResult cpuOnlyResult,
        Map<Integer, Integer> ioByPid
    ) {
        List<ProcessResult> adjustedPerProcess = new ArrayList<>(cpuOnlyResult.perProcess().size());
        double totalWaiting = 0.0;
        double totalTurnaround = 0.0;
        double totalResponse = 0.0;

        int minArrival = Integer.MAX_VALUE;
        int maxCompletion = Integer.MIN_VALUE;

        for (ProcessResult row : cpuOnlyResult.perProcess()) {
            int ioBurst = ioByPid.getOrDefault(row.processId(), 0);
            int adjustedCompletion = row.completionTime() + ioBurst;
            int adjustedTurnaround = adjustedCompletion - row.arrivalTime();
            int adjustedWaiting = Math.max(0, adjustedTurnaround - row.burstTime() - ioBurst);

            adjustedPerProcess.add(new ProcessResult(
                row.processId(),
                row.processName(),
                row.arrivalTime(),
                row.burstTime(),
                adjustedCompletion,
                adjustedWaiting,
                adjustedTurnaround,
                row.responseTime()
            ));

            totalWaiting += adjustedWaiting;
            totalTurnaround += adjustedTurnaround;
            totalResponse += row.responseTime();
            minArrival = Math.min(minArrival, row.arrivalTime());
            maxCompletion = Math.max(maxCompletion, adjustedCompletion);
        }

        int n = adjustedPerProcess.size();
        double avgWaiting = n == 0 ? 0.0 : totalWaiting / n;
        double avgTurnaround = n == 0 ? 0.0 : totalTurnaround / n;
        double avgResponse = n == 0 ? 0.0 : totalResponse / n;
        double duration = Math.max(1, maxCompletion - minArrival);
        double busyCpuTime = cpuOnlyResult.scheduleLog().stream()
            .mapToInt(entry -> entry.end() - entry.start())
            .sum();
        double cpuUtilization = (busyCpuTime / duration) * 100.0;
        double throughput = n / duration;

        return new SchedulerResult(
            cpuOnlyResult.algorithm(),
            List.copyOf(adjustedPerProcess),
            avgWaiting,
            avgTurnaround,
            avgResponse,
            cpuUtilization,
            throughput,
            cpuOnlyResult.jainsFairness(),
            cpuOnlyResult.giniCoefficient(),
            cpuOnlyResult.minMaxFairnessGap(),
            cpuOnlyResult.scheduleLog()
        );
    }
}

