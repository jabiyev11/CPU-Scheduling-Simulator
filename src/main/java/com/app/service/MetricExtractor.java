package com.app.service;

import com.app.model.SchedulerResult;

@FunctionalInterface
public interface MetricExtractor {

  double extract(SchedulerResult result);

}
