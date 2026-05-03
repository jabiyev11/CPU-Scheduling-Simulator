package com.app.service;

import com.app.model.LogEntry;
import com.app.model.Process;
import com.app.model.SchedulerResult;
import com.app.model.SchedulingAlgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class ShortestJobFirstScheduler implements Scheduler {

    @Override
    public SchedulerResult schedule(List<Process> processes) {
        // Lower burst first; ties are resolved by priority, arrival, then PID
        PriorityQueue<Process> queue = new PriorityQueue<>(
            Comparator.comparingInt(Process::getBurstTime)
                .thenComparingInt(Process::getPriority)
                .thenComparingInt(Process::getArrivalTime)
                .thenComparingInt(Process::getPid)
        );
        int time = 0;
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        List<LogEntry> scheduleLog = new ArrayList<>();
        int index = 0;

        while (index < processes.size() || !queue.isEmpty()) {
            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                queue.add(processes.get(index));
                index++;
            }

            if (!queue.isEmpty()) {
                Process process = queue.remove();
                int startTime = time;
                time += process.getBurstTime();
                process.setCompletionTime(time);
                // Turnaround Time = Completion Time - Arrival Time
                process.setTurnaroundTime(time - process.getArrivalTime());
                // Waiting Time = Turnaround Time - Burst Time
                process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
                // Response Time = First Start Time - Arrival Time
                process.setResponseTime(startTime - process.getArrivalTime());
                scheduleLog.add(new LogEntry(process.getPid(), startTime, time, true));
            } else {
                // No process has arrived yet
                time++;
            }
        }
        return SchedulerMetricsCalculator.buildResult(SchedulingAlgorithm.SHORTEST_JOB_FIRST, processes, scheduleLog);
    }
}
