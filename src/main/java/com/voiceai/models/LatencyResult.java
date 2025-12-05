package com.voiceai.models;

/**
 * Inner class for latency results
 */
public class LatencyResult {
    private long latencyMs;
    private boolean isAcceptable;
    private boolean withinThreshold;

    public LatencyResult(long latencyMs, boolean isAcceptable, boolean withinThreshold) {
        this.latencyMs = latencyMs;
        this.isAcceptable = isAcceptable;
        this.withinThreshold = withinThreshold;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public boolean isAcceptable() {
        return isAcceptable;
    }

    public boolean isWithinThreshold() {
        return withinThreshold;
    }

    public String getStatus() {
        return withinThreshold ? "PASS" : "FAIL";
    }
}
