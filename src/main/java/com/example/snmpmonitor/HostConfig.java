package com.example.snmpmonitor;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MonitoringWindow window = new MonitoringWindow();
            window.setVisible(true);
        });
    }
}
