# Scheduling Algorithms Simulator (JavaFX)

## Project Overview
This Java 17 + JavaFX project simulates:
- FCFS
- Priority (Round Robin inside same priority class)
- Round Robin
- Shortest Job First

The app includes:
- Gantt chart visualization per algorithm
- Unified metric comparison across algorithms

## Result Models

### `ProcessResult`
`src/main/java/com/app/model/ProcessResult.java`

Fields:
- `processId`
- `processName`
- `arrivalTime`
- `burstTime`
- `completionTime`
- `waitingTime`
- `turnaroundTime`
- `responseTime`

### `SchedulerResult`
`src/main/java/com/app/model/SchedulerResult.java`

Fields:
- `algorithm`
- `perProcess`
- `avgWaitingTime`
- `avgTurnaroundTime`
- `avgResponseTime`
- `cpuUtilization`
- `throughput`
- `scheduleLog`

## Comparison Panel
Use **Run All & Compare** to run all algorithms on the same process set and populate:

1. Metrics table:
   - `Algorithm | Avg Waiting | Avg Turnaround | Avg Response | CPU Util% | Throughput`
   - best values highlighted in green
   - worst values highlighted in red
   - tooltips on metric headers
2. Bar chart:
   - average waiting time across algorithms
3. Verdict:
   - best overall by lowest average waiting, turnaround tie-break

## Architecture
```text
Main
  -> MainController
     -> SimulationService
           -> Scheduler interface
           -> FCFS / Priority / RR / SJF
           -> SchedulerMetricsCalculator
     -> GanttChartRenderer

Scheduler -> SchedulerResult(perProcess + aggregate metrics + scheduleLog)
MainController -> Comparison table/chart + Gantt tabs
```

## Build and Run
### Prerequisites
- Java 17 (JDK): https://adoptium.net/temurin/releases/?version=17
- Gradle Wrapper (included in this project): https://docs.gradle.org/current/userguide/gradle_wrapper.html
- Linux (Ubuntu/Debian) install commands:

```bash
sudo apt install -y openjdk-17-jdk
java -version
```

- WSL (Ubuntu/Debian) install commands:

```bash
sudo apt install -y openjdk-17-jdk
java -version
```

- Gradle Wrapper check (Linux/WSL(Ubuntu)):

```bash
chmod +x gradlew
./gradlew --version
```

- If `gradlew` has Windows line endings and fails with `\r` errors:

```bash
# macOS (BSD sed)
sed -i '' 's/\r//' gradlew

# WSL/Linux (GNU sed)
sed -i 's/\r$//' gradlew
```

This removes carriage-return characters from `gradlew` so Unix shells can execute it correctly.

```bash
cd group7_os_project_phase2 

# Windows
.\gradlew.bat run

# macOS/Linux
./gradlew run
```
# CPU-Scheduling-Simulator
