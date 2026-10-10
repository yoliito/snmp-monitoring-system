package com.example.snmpmonitor;

import java.time.Instant;

public class AlertEvent {
    private final String host;
    private final String description;
    private final Instant detectedAt;

    public AlertEvent(String host, String description, Instant detectedAt) {
        this.host = host;
        this.description = description;
        this.detectedAt = detectedAt;
    }

    public String getHost() {
        return host;
    }

    public String getDescription() {
        return description;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }
}
