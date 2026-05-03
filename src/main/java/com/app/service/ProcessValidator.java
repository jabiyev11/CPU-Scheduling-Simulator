package com.app.service;

import com.app.model.Process;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;

public final class ProcessValidator {

  // Returns a deep copy so the scheduler can mutate fields
  public List<Process> validateAndCopy(ObservableList<Process> processes) {
    if (processes.isEmpty()) {
      throw new IllegalArgumentException(
          "No process data available. Initialize or randomize processes first.");
    }
    List<Process> copy = new ArrayList<>();
    for (Process process : processes) {
      if (process.getPid() < 1) {
        throw new IllegalArgumentException("PID must be at least 1.");
      }
      if (process.getArrivalTime() < 0) {
        throw new IllegalArgumentException("Arrival time must be at least 0.");
      }
      if (process.getBurstTime() < 1) {
        throw new IllegalArgumentException("Burst time must be at least 1.");
      }
      if (process.getIoBurstTime() < 0) {
        throw new IllegalArgumentException("I/O burst time must be at least 0.");
      }
      if (process.getPriority() < 1) {
        throw new IllegalArgumentException("Priority must be at least 1.");
      }
      copy.add(process.copy());
    }
    return copy;
  }

}
