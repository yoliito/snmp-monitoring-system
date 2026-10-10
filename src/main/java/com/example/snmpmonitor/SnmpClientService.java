package com.example.snmpmonitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SnmpCheckResult {
    private final boolean reachable;
    private final boolean snmpAvailable;
    private final String message;
    private final Map<String, String> values;

    public SnmpCheckResult(boolean reachable, boolean snmpAvailable, String message, Map<String, String> values) {
        this.reachable = reachable;
        this.snmpAvailable = snmpAvailable;
        this.message = message;
        this.values = new HashMap<>(values);
    }

    public boolean isReachable() {
        return reachable;
    }

    public boolean isSnmpAvailable() {
        return snmpAvailable;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public Optional<Integer> numericValueFor(String oid) {
        String raw = values.get(oid);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(raw.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public boolean hasOid(String oid) {
        return values.containsKey(oid);
    }
}
