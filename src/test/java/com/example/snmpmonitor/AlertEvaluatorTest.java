package com.example.snmpmonitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class MonitoringWindow extends JFrame {
    private final MonitoringService monitoringService = new MonitoringService();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Equipo", "Falla", "Fecha y hora" },
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("No se detectaron fallas", SwingConstants.CENTER);
    private final JTextField hostField = new JTextField("192.168.1.10", 18);
    private final JTextField communityField = new JTextField("public", 12);
    private final JButton addButton = new JButton("Agregar equipo");
    private final JButton startButton = new JButton("Iniciar monitoreo");
    private final JButton checkWindowsButton = new JButton("Verificar SNMP Windows");

    private Timer timer;

    public MonitoringWindow() {
        super("Monitor SNMP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initializeDefaultHosts();
        configureComponents();
        addPanels();
    }

    private void initializeDefaultHosts() {
        monitoringService.addHost(new HostConfig("192.168.1.10", "public", 161, 2000, 1, 90, 80));
    }

    private void configureComponents() {
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        addButton.addActionListener(e -> addHostFromForm());
        startButton.addActionListener(e -> startMonitoring());
        checkWindowsButton.addActionListener(e -> showWindowsSnmpStatus());
    }

    private void addPanels() {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.add(new JLabel("Equipo:"));
        inputPanel.add(hostField);
        inputPanel.add(new JLabel("Comunidad:"));
        inputPanel.add(communityField);
        inputPanel.add(addButton);
        inputPanel.add(startButton);
        inputPanel.add(checkWindowsButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        centerPanel.add(statusLabel);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(850, 350));
        centerPanel.add(scrollPane);

        add(inputPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void addHostFromForm() {
        String host = hostField.getText() == null ? "" : hostField.getText().trim();
        String community = communityField.getText() == null ? "public" : communityField.getText().trim();
        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar una dirección IP o nombre de equipo.", "Entrada inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HostConfig hostConfig = new HostConfig(host, community, 161, 2000, 1, 90, 80);
        monitoringService.addHost(hostConfig);
        JOptionPane.showMessageDialog(this, "Equipo agregado: " + host, "Equipo registrado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startMonitoring() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            startButton.setText("Iniciar monitoreo");
            statusLabel.setText("Monitoreo detenido");
            return;
        }

        timer = new Timer(15000, e -> refreshAlerts());
        timer.setRepeats(true);
        timer.start();
        startButton.setText("Detener monitoreo");
        refreshAlerts();
    }

    private void refreshAlerts() {
        List<AlertEvent> alerts = new ArrayList<>();
        for (HostConfig host : monitoringService.getHosts()) {
            alerts.addAll(new MonitoringService().checkAll());
            break;
        }

        if (alerts.isEmpty()) {
            tableModel.setRowCount(0);
            statusLabel.setText("No se detectaron fallas");
            return;
        }

        tableModel.setRowCount(0);
        for (AlertEvent alert : alerts) {
            tableModel.addRow(new Object[] {
                    alert.getHost(),
                    alert.getDescription(),
                    alert.getDetectedAt()
            });
        }

        statusLabel.setText("Se detectaron " + alerts.size() + " falla(s)");
    }

    private void showWindowsSnmpStatus() {
        if (!WindowsSnmpSetupHelper.isWindows()) {
            JOptionPane.showMessageDialog(this, "Esta comprobación de SNMP es específica de Windows.", "Sistema no compatible", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String result = WindowsSnmpSetupHelper.getStatusSummary();
        JOptionPane.showMessageDialog(this, result + "\n\nInstrucciones:\n" + WindowsSnmpSetupHelper.getManualSetupInstructions(), "Estado SNMP en Windows", JOptionPane.INFORMATION_MESSAGE);
    }
}

