package com.app.model;

public record LogEntry(int pid, int start, int end, boolean isFinished) {
}
