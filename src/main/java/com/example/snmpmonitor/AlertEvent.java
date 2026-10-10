package com.example.snmpmonitor;

public class HostConfig {
    private final String host;
    private final String community;
    private final int port;
    private final int timeoutMs;
    private final int retries;
    private final Integer cpuThreshold;
    private final Integer memoryThreshold;

    public HostConfig(String host, String community, int port, int timeoutMs, int retries,
                      Integer cpuThreshold, Integer memoryThreshold) {
        this.host = host;
        this.community = community;
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.retries = retries;
        this.cpuThreshold = cpuThreshold;
        this.memoryThreshold = memoryThreshold;
    }

    public String getHost() {
        return host;
    }

    public String getCommunity() {
        return community;
    }

    public int getPort() {
        return port;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getRetries() {
        return retries;
    }

    public Integer getCpuThreshold() {
        return cpuThreshold;
    }

    public Integer getMemoryThreshold() {
        return memoryThreshold;
    }
}
