package com.app.service;

import com.app.model.Process;
import com.app.model.SchedulerResult;

import java.util.List;

public interface Scheduler {

    SchedulerResult schedule(List<Process> processes);
}
