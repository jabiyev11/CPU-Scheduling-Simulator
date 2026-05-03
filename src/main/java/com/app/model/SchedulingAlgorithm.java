package com.app.model;

public enum SchedulingAlgorithm {
    FCFS("First Come, First Served"),       // Non-preemptive FIFO scheduling
    PRIORITY("Priority"),                   // Priority class time-sliced scheduling
    ROUND_ROBIN("Round Robin"),             // Preemptive scheduling with a fixed quantum
    SHORTEST_JOB_FIRST("Shortest Job First"), // Non-preemptive shortest burst first scheduling
    SHORTEST_REMAINING_TIME_FIRST("Shortest Remaining Time First"), // Preemptive SJF variant
    LOTTERY("Lottery"), // Probabilistic selection using ticket draw
    GUARANTEED("Guaranteed"), // Equal-share style scheduling
    MULTILEVEL_QUEUE("Multilevel Queue"); // Fixed queue hierarchy by priority class

    private final String label;

    SchedulingAlgorithm(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
