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

public final class MultilevelQueueScheduler implements Scheduler {
    private final int quantum;

    public MultilevelQueueScheduler(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public SchedulerResult schedule(List<Process> processes) {
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        ArrayDeque<Process> highQueue = new ArrayDeque<>();
        ArrayDeque<Process> midQueue = new ArrayDeque<>();
        ArrayDeque<Process> lowQueue = new ArrayDeque<>();
        Map<Integer, Integer> firstStartTimes = new HashMap<>();
        List<LogEntry> scheduleLog = new ArrayList<>();

        int highQuantum = quantum;
        int midQuantum = Math.max(1, quantum * 2);

        int time = processes.get(0).getArrivalTime();
        int index = 0;

        while (index < processes.size() || !highQueue.isEmpty() || !midQueue.isEmpty() || !lowQueue.isEmpty()) {
            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                enqueueByPriority(processes.get(index), highQueue, midQueue, lowQueue);
                index++;
            }

            Process chosen;
            int slice;
            ArrayDeque<Process> ownerQueue;

            if (!highQueue.isEmpty()) {
                chosen = highQueue.removeFirst();
                slice = Math.min(highQuantum, chosen.getRemainingTime());
                ownerQueue = highQueue;
            } else if (!midQueue.isEmpty()) {
                chosen = midQueue.removeFirst();
                slice = Math.min(midQuantum, chosen.getRemainingTime());
                ownerQueue = midQueue;
            } else if (!lowQueue.isEmpty()) {
                chosen = lowQueue.removeFirst();
                slice = chosen.getRemainingTime();
                ownerQueue = lowQueue;
            } else {
                time++;
                continue;
            }

            int startTime = time;
            firstStartTimes.putIfAbsent(chosen.getPid(), startTime);

            time += slice;
            chosen.setRemainingTime(chosen.getRemainingTime() - slice);

            if (!scheduleLog.isEmpty() && scheduleLog.get(scheduleLog.size() - 1).pid() == chosen.getPid()) {
                LogEntry last = scheduleLog.get(scheduleLog.size() - 1);
                scheduleLog.set(scheduleLog.size() - 1,
                    new LogEntry(chosen.getPid(), last.start(), time, chosen.getRemainingTime() == 0));
            } else {
                scheduleLog.add(new LogEntry(chosen.getPid(), startTime, time, chosen.getRemainingTime() == 0));
            }

            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                enqueueByPriority(processes.get(index), highQueue, midQueue, lowQueue);
                index++;
            }

            if (chosen.getRemainingTime() > 0) {
                ownerQueue.addLast(chosen);
            } else {
                chosen.setCompletionTime(time);
                chosen.setTurnaroundTime(time - chosen.getArrivalTime());
                chosen.setWaitingTime(chosen.getTurnaroundTime() - chosen.getBurstTime());
                chosen.setResponseTime(firstStartTimes.get(chosen.getPid()) - chosen.getArrivalTime());
            }
        }

        return SchedulerMetricsCalculator.buildResult(SchedulingAlgorithm.MULTILEVEL_QUEUE, processes, scheduleLog);
    }

    private void enqueueByPriority(
        Process process,
        ArrayDeque<Process> highQueue,
        ArrayDeque<Process> midQueue,
        ArrayDeque<Process> lowQueue
    ) {
        if (process.getRemainingTime() <= 0) {
            return;
        }
        if (process.getPriority() <= 1) {
            highQueue.addLast(process);
        } else if (process.getPriority() == 2) {
            midQueue.addLast(process);
        } else {
            lowQueue.addLast(process);
        }
    }
}
