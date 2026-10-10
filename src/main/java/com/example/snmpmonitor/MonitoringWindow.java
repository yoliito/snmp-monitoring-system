package com.example.snmpmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WindowsSnmpSetupHelper {

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static String getStatusSummary() {
        if (!isWindows()) {
            return "La comprobación de SNMP para Windows solo es válida en sistemas operativos Windows.";
        }

        List<String> commands = new ArrayList<>();
        commands.add("powershell");
        commands.add("-NoProfile");
        commands.add("-ExecutionPolicy");
        commands.add("Bypass");
        commands.add("-Command");
        commands.add("Get-Service SNMP* | Select-Object Name,Status,StartType | Format-Table -AutoSize");

        try {
            Process process = new ProcessBuilder(commands).start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return output.toString().trim().isEmpty() ? "No se encontró el servicio SNMP en Windows." : output.toString().trim();
            }
            return "No fue posible verificar el estado del servicio SNMP con PowerShell.";
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "Error al comprobar la configuración de SNMP: " + ex.getMessage();
        }
    }

    public static String getManualSetupInstructions() {
        return """
        1. Abrir PowerShell como administrador.
        2. Ejecutar: Get-WindowsCapability -Online -Name SNMP*
        3. Si está disponible, instalar:
           Enable-WindowsOptionalFeature -Online -FeatureName SNMP -All
        4. Verificar servicio:
           Get-Service SNMP* | Format-Table Name,Status,StartType
        5. Iniciar el servicio:
           net start SNMP
        6. Si es necesario, configurar arranque automático:
           sc config SNMP start= auto
        7. Si no tiene permisos de administrador, la habilitación del servicio debe realizarla el administrador del equipo.
        """;
    }

    public static boolean tryLaunchAdminPowerShell() {
        if (!isWindows()) {
            return false;
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-Command",
                    "Start-Process powershell -Verb RunAs -ArgumentList \"-NoProfile -ExecutionPolicy Bypass -Command Get-Service SNMP*\""
            );
            Process process = builder.start();
            process.waitFor();
            return true;
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
