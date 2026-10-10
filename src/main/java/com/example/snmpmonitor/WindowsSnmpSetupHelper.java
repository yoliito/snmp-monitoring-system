package com.example.snmpmonitor;

import java.util.ArrayList;
import java.util.List;

public class MonitoringService {
    private final SnmpClientService snmpClientService;
    private final AlertEvaluator alertEvaluator;
    private final List<HostConfig> hosts;

    public MonitoringService() {
        this.snmpClientService = new SnmpClientService();
        this.alertEvaluator = new AlertEvaluator();
        this.hosts = new ArrayList<>();
    }

    public void addHost(HostConfig host) {
        hosts.add(host);
    }

    public void removeHost(String hostValue) {
        hosts.removeIf(host -> host.getHost().equalsIgnoreCase(hostValue));
    }

    public List<HostConfig> getHosts() {
        return new ArrayList<>(hosts);
    }

    public List<AlertEvent> checkAll() {
        List<AlertEvent> alerts = new ArrayList<>();
        for (HostConfig host : hosts) {
            SnmpCheckResult result = snmpClientService.checkHost(host);
            alerts.addAll(alertEvaluator.evaluate(host, result));
        }
        return alerts;
    }
}
