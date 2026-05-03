package com.app.service;

import com.app.model.LogEntry;
import com.app.model.Process;
import com.app.model.SchedulerResult;
import com.app.model.SchedulingAlgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FirstComeFirstServedScheduler implements Scheduler {

    @Override
    public SchedulerResult schedule(List<Process> processes) {
        // Sorting by arrival time to enforce FCFS order
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        List<LogEntry> scheduleLog = new ArrayList<>();
        int time = processes.get(0).getArrivalTime();

        for (Process process : processes) {
            int startTime = Math.max(process.getArrivalTime(), time);
            // FCFS executes to completion because it is non-preemptive
            time = startTime + process.getBurstTime();
            process.setCompletionTime(time);
            // Turnaround Time = Completion Time - Arrival Time
            process.setTurnaroundTime(time - process.getArrivalTime());
            // Waiting Time = Turnaround Time - Burst Time
            process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
            // Response Time = First Start Time - Arrival Time
            process.setResponseTime(startTime - process.getArrivalTime());
            scheduleLog.add(new LogEntry(process.getPid(), startTime, time, true));
        }
        return SchedulerMetricsCalculator.buildResult(SchedulingAlgorithm.FCFS, processes, scheduleLog);
    }
}
