package com.app.service;

import com.app.model.LogEntry;
import com.app.model.Process;
import com.app.model.SchedulerResult;
import com.app.model.SchedulingAlgorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PriorityScheduler implements Scheduler {
    private final int quantum;

    public PriorityScheduler(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public SchedulerResult schedule(List<Process> processes) {
        Map<Integer, ArrayDeque<Process>> priorityClasses = new HashMap<>();
        Map<Integer, Integer> firstStartTimes = new HashMap<>();
        int time = 0;
        // Determine upper priority bound for class scanning
        processes.sort(Comparator.comparingInt(Process::getPriority));
        int maxPriority = processes.get(processes.size() - 1).getPriority();
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        int maxArrivalTime = processes.get(processes.size() - 1).getArrivalTime();
        List<LogEntry> scheduleLog = new ArrayList<>();
        int index = 0;

        while (true) {
            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                int priority = processes.get(index).getPriority();
                priorityClasses.putIfAbsent(priority, new ArrayDeque<>());
                priorityClasses.get(priority).addLast(processes.get(index));
                index++;
            }

            boolean foundProcess = false;
            // Dispatching first available queue from highest priority class
            for (int priority = 1; priority <= maxPriority; priority++) {
                ArrayDeque<Process> queue = priorityClasses.get(priority);
                if (queue != null && !queue.isEmpty()) {
                    Process process = queue.removeFirst();
                    int executionTime = Math.min(quantum, process.getRemainingTime());
                    int startTime = time;
                    firstStartTimes.putIfAbsent(process.getPid(), startTime);
                    time += executionTime;
                    process.setRemainingTime(process.getRemainingTime() - executionTime);

                    if (!scheduleLog.isEmpty() && scheduleLog.get(scheduleLog.size() - 1).pid() == process.getPid()) {
                        scheduleLog.set(
                            scheduleLog.size() - 1,
                            new LogEntry(
                                process.getPid(),
                                scheduleLog.get(scheduleLog.size() - 1).start(),
                                time,
                                process.getRemainingTime() == 0
                            )
                        );
                    } else {
                        scheduleLog.add(new LogEntry(process.getPid(), startTime, time, process.getRemainingTime() == 0));
                    }

                    if (process.getRemainingTime() > 0) {
                        queue.addLast(process);
                    } else {
                        process.setCompletionTime(time);
                        // Turnaround Time = Completion Time - Arrival Time
                        process.setTurnaroundTime(time - process.getArrivalTime());
                        // Waiting Time = Turnaround Time - Burst Time
                        process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
                        // Response Time = First Start Time - Arrival Time
                        process.setResponseTime(firstStartTimes.get(process.getPid()) - process.getArrivalTime());
                    }
                    foundProcess = true;
                }
                if (foundProcess) {
                    break;
                }
            }

            if (foundProcess) {
                continue;
            }
            if (time > maxArrivalTime) {
                break;
            }
            // No ready process in any priority class yet
            time++;
        }

        return SchedulerMetricsCalculator.buildResult(SchedulingAlgorithm.PRIORITY, processes, scheduleLog);
    }
}
