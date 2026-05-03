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

public final class ShortestRemainingTimeFirstScheduler implements Scheduler {
    @Override
    public SchedulerResult schedule(List<Process> processes) {
        // Preemptive SJF: re-select the shortest remaining burst every tick
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        Map<Integer, Integer> firstStartTimes = new HashMap<>();
        List<LogEntry> scheduleLog = new ArrayList<>();

        int time = processes.get(0).getArrivalTime();
        int completed = 0;

        while (completed < processes.size()) {
            Process chosen = null;
            for (Process process : processes) {
                if (process.getArrivalTime() <= time && process.getRemainingTime() > 0) {
                    if (chosen == null
                        || process.getRemainingTime() < chosen.getRemainingTime()
                        // tie: earlier arrival wins, then lower PID for determinism
                        || (process.getRemainingTime() == chosen.getRemainingTime()
                            && process.getArrivalTime() < chosen.getArrivalTime())
                        || (process.getRemainingTime() == chosen.getRemainingTime()
                            && process.getArrivalTime() == chosen.getArrivalTime()
                            && process.getPid() < chosen.getPid())) {
                        chosen = process;
                    }
                }
            }

            if (chosen == null) {
                time++;
                continue;
            }

            int start = time;
            firstStartTimes.putIfAbsent(chosen.getPid(), start);
            chosen.setRemainingTime(chosen.getRemainingTime() - 1);
            time++;

            // Merge consecutive ticks of the same PID to keep the Gantt log compact
            if (!scheduleLog.isEmpty() && scheduleLog.get(scheduleLog.size() - 1).pid() == chosen.getPid()) {
                LogEntry last = scheduleLog.get(scheduleLog.size() - 1);
                scheduleLog.set(scheduleLog.size() - 1,
                    new LogEntry(chosen.getPid(), last.start(), time, chosen.getRemainingTime() == 0));
            } else {
                scheduleLog.add(new LogEntry(chosen.getPid(), start, time, chosen.getRemainingTime() == 0));
            }

            if (chosen.getRemainingTime() == 0) {
                completed++;
                chosen.setCompletionTime(time);
                // Turnaround Time = Completion Time - Arrival Time
                chosen.setTurnaroundTime(time - chosen.getArrivalTime());
                // Waiting Time = Turnaround Time - Burst Time
                chosen.setWaitingTime(chosen.getTurnaroundTime() - chosen.getBurstTime());
                // Response Time = First Start Time - Arrival Time
                chosen.setResponseTime(firstStartTimes.get(chosen.getPid()) - chosen.getArrivalTime());
            }
        }

        return SchedulerMetricsCalculator.buildResult(
            SchedulingAlgorithm.SHORTEST_REMAINING_TIME_FIRST,
            processes,
            scheduleLog
        );
    }
}
