package com.app.util;

import com.app.model.Process;
import com.app.model.ProcessResult;
import com.app.model.SchedulerResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public final class RunLogWriter {
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final DateTimeFormatter HUMAN_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RunLogWriter() {
    }

    public static Path writeRunLog(
        String runType,
        int quantum,
        List<Process> inputProcesses,
        List<SchedulerResult> results
    ) throws IOException {
        Path logsDir = Path.of("run-logs");
        Files.createDirectories(logsDir);

        LocalDateTime now = LocalDateTime.now();
        Path logFile = logsDir.resolve("run_" + now.format(FILE_TS) + ".log");

        StringBuilder out = new StringBuilder();
        out.append("CPU Scheduling Simulator Run Log").append(System.lineSeparator());
        out.append("Timestamp: ").append(now.format(HUMAN_TS)).append(System.lineSeparator());
        out.append("Run Type: ").append(runType).append(System.lineSeparator());
        out.append("Quantum: ").append(quantum).append(System.lineSeparator()).append(System.lineSeparator());

        out.append("Input Process Data").append(System.lineSeparator());
        out.append("PID | Arrival | Burst | I/O Burst | Priority").append(System.lineSeparator());
        inputProcesses.stream()
            .sorted(Comparator.comparingInt(Process::getPid))
            .forEach(p -> out.append(String.format("%3d | %7d | %5d | %9d | %8d%n",
                p.getPid(), p.getArrivalTime(), p.getBurstTime(), p.getIoBurstTime(), p.getPriority())));
        out.append(System.lineSeparator());

        out.append("Comparison Summary").append(System.lineSeparator());
        out.append("Algorithm | AvgWaiting | AvgTurnaround | AvgResponse | CPUUtil(%) | Throughput | Jain | Gini | MinMaxGap")
            .append(System.lineSeparator());
        for (SchedulerResult result : results) {
            out.append(String.format("%-24s | %10.3f | %13.3f | %11.3f | %10.3f | %10.3f | %5.3f | %5.3f | %9.3f%n",
                result.algorithm().getLabel(),
                result.avgWaitingTime(),
                result.avgTurnaroundTime(),
                result.avgResponseTime(),
                result.cpuUtilization(),
                result.throughput(),
                result.jainsFairness(),
                result.giniCoefficient(),
                result.minMaxFairnessGap()
            ));
        }
        out.append(System.lineSeparator());

        for (SchedulerResult result : results) {
            out.append("Per-Process Results - ").append(result.algorithm().getLabel()).append(System.lineSeparator());
            out.append("PID | Name | Arrival | Burst | Completion | Waiting | Turnaround | Response").append(System.lineSeparator());
            for (ProcessResult processResult : result.perProcess()) {
                out.append(String.format("%3d | %-4s | %7d | %5d | %10d | %7d | %10d | %8d%n",
                    processResult.processId(),
                    processResult.processName(),
                    processResult.arrivalTime(),
                    processResult.burstTime(),
                    processResult.completionTime(),
                    processResult.waitingTime(),
                    processResult.turnaroundTime(),
                    processResult.responseTime()
                ));
            }
            out.append(System.lineSeparator());
        }

        Files.writeString(logFile, out.toString(), StandardCharsets.UTF_8);
        return logFile;
    }
}
