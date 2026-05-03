package com.app.service;

import com.app.model.LogEntry;
import com.app.model.Process;
import com.app.model.SchedulerResult;
import com.app.model.SchedulingAlgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GuaranteedScheduler implements Scheduler {
    private final int quantum;

    public GuaranteedScheduler(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public SchedulerResult schedule(List<Process> processes) {
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        List<Process> ready = new ArrayList<>();
        Map<Integer, Integer> firstStartTimes = new HashMap<>();
        // cpuUsed tracks how many time units each process has actually run so far
        Map<Integer, Integer> cpuUsed = new HashMap<>();
        List<LogEntry> scheduleLog = new ArrayList<>();

        int time = processes.get(0).getArrivalTime();
        int index = 0;

        while (index < processes.size() || !ready.isEmpty()) {
            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                Process process = processes.get(index);
                if (process.getRemainingTime() > 0) {
                    ready.add(process);
                    cpuUsed.putIfAbsent(process.getPid(), 0);
                }
                index++;
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            // Always run the process that has received the smallest fraction of its expected CPU
            Process chosen = ready.stream()
                .min((a, b) -> {
                    double ratioA = cpuUsed.getOrDefault(a.getPid(), 0) / (double) a.getBurstTime();
                    double ratioB = cpuUsed.getOrDefault(b.getPid(), 0) / (double) b.getBurstTime();
                    int byRatio = Double.compare(ratioA, ratioB);
                    if (byRatio != 0) {
                        return byRatio;
                    }
                    int byArrival = Integer.compare(a.getArrivalTime(), b.getArrivalTime());
                    if (byArrival != 0) {
                        return byArrival;
                    }
                    return Integer.compare(a.getPid(), b.getPid());
                })
                .orElseThrow();

            ready.remove(chosen);

            int startTime = time;
            int executionTime = Math.min(quantum, chosen.getRemainingTime());
            firstStartTimes.putIfAbsent(chosen.getPid(), startTime);

            time += executionTime;
            chosen.setRemainingTime(chosen.getRemainingTime() - executionTime);
            cpuUsed.put(chosen.getPid(), cpuUsed.getOrDefault(chosen.getPid(), 0) + executionTime);

            if (!scheduleLog.isEmpty() && scheduleLog.get(scheduleLog.size() - 1).pid() == chosen.getPid()) {
                LogEntry last = scheduleLog.get(scheduleLog.size() - 1);
                scheduleLog.set(scheduleLog.size() - 1,
                    new LogEntry(chosen.getPid(), last.start(), time, chosen.getRemainingTime() == 0));
            } else {
                scheduleLog.add(new LogEntry(chosen.getPid(), startTime, time, chosen.getRemainingTime() == 0));
            }

            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                Process process = processes.get(index);
                if (process.getRemainingTime() > 0) {
                    ready.add(process);
                    cpuUsed.putIfAbsent(process.getPid(), 0);
                }
                index++;
            }

            if (chosen.getRemainingTime() > 0) {
                ready.add(chosen);
            } else {
                chosen.setCompletionTime(time);
                // Turnaround Time = Completion Time - Arrival Time
                chosen.setTurnaroundTime(time - chosen.getArrivalTime());
                // Waiting Time = Turnaround Time - Burst Time
                chosen.setWaitingTime(chosen.getTurnaroundTime() - chosen.getBurstTime());
                // Response Time = First Start Time - Arrival Time
                chosen.setResponseTime(firstStartTimes.get(chosen.getPid()) - chosen.getArrivalTime());
            }
        }

        return SchedulerMetricsCalculator.buildResult(SchedulingAlgorithm.GUARANTEED, processes, scheduleLog);
    }
}
