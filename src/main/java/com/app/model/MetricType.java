package com.app.model;

public enum MetricType {

  AVG_WAITING,    // Average waiting time (lower is better)
  AVG_TURNAROUND, // Average turnaround time (lower is better)
  AVG_RESPONSE,   // Average response time (lower is better)
  CPU_UTIL,       // CPU utilization percentage (higher is better)
  THROUGHPUT,     // Completed processes per unit time (higher is better)
  JAINS_FAIRNESS, // Jain's fairness index in [0, 1], higher is better
  GINI,           // Gini coefficient in [0, 1], lower is better
  MIN_MAX_GAP     // Max-min service share gap, lower is better

}
