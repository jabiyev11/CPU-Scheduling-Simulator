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
import java.util.Random;

public final class LotteryScheduler implements Scheduler {
    private final int quantum;
    private final Random random = new Random();

    public LotteryScheduler(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public SchedulerResult schedule(List<Process> processes) {
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        List<Process> ready = new ArrayList<>();
        Map<Integer, Integer> firstStartTimes = new HashMap<>();
        List<LogEntry> scheduleLog = new ArrayList<>();

        int time = processes.get(0).getArrivalTime();
        int index = 0;

        while (index < processes.size() || !ready.isEmpty()) {
            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                if (processes.get(index).getRemainingTime() > 0) {
                    ready.add(processes.get(index));
                }
                index++;
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            Process chosen = drawByTickets(ready);
            ready.remove(chosen);

            int startTime = time;
            int executionTime = Math.min(quantum, chosen.getRemainingTime());
            firstStartTimes.putIfAbsent(chosen.getPid(), startTime);

            time += executionTime;
            chosen.setRemainingTime(chosen.getRemainingTime() - executionTime);

            if (!scheduleLog.isEmpty() && scheduleLog.get(scheduleLog.size() - 1).pid() == chosen.getPid()) {
                LogEntry last = scheduleLog.get(scheduleLog.size() - 1);
                scheduleLog.set(scheduleLog.size() - 1,
                    new LogEntry(chosen.getPid(), last.start(), time, chosen.getRemainingTime() == 0));
            } else {
                scheduleLog.add(new LogEntry(chosen.getPid(), startTime, time, chosen.getRemainingTime() == 0));
            }

            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                if (processes.get(index).getRemainingTime() > 0) {
                    ready.add(processes.get(index));
                }
                index++;
            }

            if (chosen.getRemainingTime() > 0) {
                ready.add(chosen);
            } else {
                chosen.setCompletionTime(time);
                chosen.setTurnaroundTime(time - chosen.getArrivalTime());
                chosen.setWaitingTime(chosen.getTurnaroundTime() - chosen.getBurstTime());
                chosen.setResponseTime(firstStartTimes.get(chosen.getPid()) - chosen.getArrivalTime());
            }
        }

        return SchedulerMetricsCalculator.buildResult(SchedulingAlgorithm.LOTTERY, processes, scheduleLog);
    }

    private Process drawByTickets(List<Process> ready) {
        int totalTickets = 0;
        for (Process process : ready) {
            totalTickets += tickets(process);
        }
        int draw = random.nextInt(totalTickets) + 1;

        int cumulative = 0;
        for (Process process : ready) {
            cumulative += tickets(process);
            if (draw <= cumulative) {
                return process;
            }
        }
        return ready.get(ready.size() - 1);
    }

    private int tickets(Process process) {
        return Math.max(1, 6 - process.getPriority());
    }
}
