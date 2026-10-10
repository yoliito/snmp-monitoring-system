package com.example.snmpmonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpClientService {
    public static final String OID_SYS_DESCR = "1.3.6.1.2.1.1.1.0";
    public static final String OID_SYS_NAME = "1.3.6.1.2.1.1.5.0";
    public static final String OID_SYS_UPTIME = "1.3.6.1.2.1.1.3.0";
    public static final String OID_CPU_LOAD = "1.3.6.1.2.1.25.3.3.1.2.1";

    public SnmpCheckResult checkHost(HostConfig hostConfig) {
        Map<String, String> values = new HashMap<>();
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(hostConfig.getCommunity()));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(new UdpAddress(hostConfig.getHost() + "/" + hostConfig.getPort()));
        target.setTimeout(hostConfig.getTimeoutMs());
        target.setRetries(hostConfig.getRetries());

        try (Snmp snmp = createSnmpClient()) {
            PDU request = new PDU();
            request.setType(PDU.GET);
            request.add(new VariableBinding(new OID(OID_SYS_DESCR)));
            request.add(new VariableBinding(new OID(OID_SYS_NAME)));
            request.add(new VariableBinding(new OID(OID_SYS_UPTIME)));
            request.add(new VariableBinding(new OID(OID_CPU_LOAD)));

            ResponseEvent responseEvent = snmp.send(request, target, null);
            if (responseEvent == null || responseEvent.getResponse() == null) {
                return new SnmpCheckResult(false, false, "Equipo no responde o SNMP no está habilitado.", values);
            }

            PDU response = responseEvent.getResponse();
            for (VariableBinding variableBinding : response.getVariableBindings()) {
                if (variableBinding == null || variableBinding.getOid() == null) {
                    continue;
                }
                values.put(variableBinding.getOid().toString(), variableBinding.getVariable().toString());
            }

            if (values.isEmpty()) {
                return new SnmpCheckResult(true, false, "SNMP respondió, pero no se recibieron datos válidos.", values);
            }

            return new SnmpCheckResult(true, true, "SNMP operativo.", values);
        } catch (IOException ex) {
            return new SnmpCheckResult(false, false, "No se pudo establecer la conexión SNMP: " + ex.getMessage(), values);
        } catch (RuntimeException ex) {
            return new SnmpCheckResult(false, false, "Error al consultar SNMP: " + ex.getMessage(), values);
        }
    }

    private Snmp createSnmpClient() throws IOException {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();
        return snmp;
    }
}
