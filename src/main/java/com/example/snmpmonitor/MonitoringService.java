package com.example.snmpmonitor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AlertEvaluator {

    public List<AlertEvent> evaluate(HostConfig host, SnmpCheckResult result) {
        List<AlertEvent> alerts = new ArrayList<>();

        if (!result.isReachable()) {
            alerts.add(new AlertEvent(host.getHost(), result.getMessage(), Instant.now()));
            return alerts;
        }

        if (!result.isSnmpAvailable()) {
            alerts.add(new AlertEvent(host.getHost(), result.getMessage(), Instant.now()));
            return alerts;
        }

        if (host.getCpuThreshold() != null && result.hasOid(SnmpClientService.OID_CPU_LOAD)) {
            Integer cpuLoad = result.numericValueFor(SnmpClientService.OID_CPU_LOAD).orElse(null);
            if (cpuLoad != null && cpuLoad > host.getCpuThreshold()) {
                alerts.add(new AlertEvent(
                        host.getHost(),
                        "Carga de CPU superior al límite configurado: " + cpuLoad + "%",
                        Instant.now()
                ));
            }
        }

        return alerts;
    }
}
