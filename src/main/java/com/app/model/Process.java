package com.app.model;

import java.util.Objects;

public final class Process {
    private int pid;
    private int arrivalTime;
    private int burstTime;
    private int ioBurstTime;
    private int remainingTime;
    private int priority;
    private int completionTime;
    private int waitingTime;
    private int turnaroundTime;
    private int responseTime;

    public Process(int pid, int arrivalTime, int burstTime, int priority) {
        this(pid, arrivalTime, burstTime, priority, 0);
    }

    public Process(int pid, int arrivalTime, int burstTime, int priority, int ioBurstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.ioBurstTime = ioBurstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
    }

    public Process copy() {
        Process clone = new Process(pid, arrivalTime, burstTime, priority, ioBurstTime);
        clone.remainingTime = remainingTime;
        clone.completionTime = completionTime;
        clone.waitingTime = waitingTime;
        clone.turnaroundTime = turnaroundTime;
        clone.responseTime = responseTime;
        return clone;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getIoBurstTime() {
        return ioBurstTime;
    }

    public void setIoBurstTime(int ioBurstTime) {
        this.ioBurstTime = ioBurstTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public int compareByPriorityArrivalPid(Process other) {
        int byPriority = Integer.compare(priority, other.priority);
        if (byPriority != 0) {
            return byPriority;
        }
        int byArrival = Integer.compare(arrivalTime, other.arrivalTime);
        if (byArrival != 0) {
            return byArrival;
        }
        return Integer.compare(pid, other.pid);
    }

    @Override
    public String toString() {
        return "Process P" + pid + System.lineSeparator() +
            System.lineSeparator() +
            "Arrival time: " + arrivalTime + System.lineSeparator() +
            "Burst time: " + burstTime + System.lineSeparator() +
            "I/O burst time: " + ioBurstTime + System.lineSeparator() +
            "Priority: " + priority;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Process process)) {
            return false;
        }
        return pid == process.pid &&
            arrivalTime == process.arrivalTime &&
            burstTime == process.burstTime &&
            ioBurstTime == process.ioBurstTime &&
            remainingTime == process.remainingTime &&
            priority == process.priority &&
            completionTime == process.completionTime &&
            waitingTime == process.waitingTime &&
            turnaroundTime == process.turnaroundTime &&
            responseTime == process.responseTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            pid,
            arrivalTime,
            burstTime,
            ioBurstTime,
            remainingTime,
            priority,
            completionTime,
            waitingTime,
            turnaroundTime,
            responseTime
        );
    }
}
