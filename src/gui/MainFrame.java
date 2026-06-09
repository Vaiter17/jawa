package gui;

import services.TriageService;
import models.Patient;
import models.Symptom;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MainFrame extends JFrame {
    private TriageService triageService;
    
    private JTextField nameField;
    private JComboBox<Symptom> symptomCombo;
    private JSlider painSlider;
    private JCheckBox manualOverrideCheck;
    private JSpinner manualScoreSpinner;
    private JTable queueTable;
    private DefaultTableModel tableModel;

    public MainFrame() {
        triageService = new TriageService();
        
        setTitle("Hospital ER Patient Triage System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel regPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        regPanel.setBorder(BorderFactory.createTitledBorder("Register New Patient"));
        
        regPanel.add(new JLabel("Patient Name:"));
        nameField = new JTextField();
        regPanel.add(nameField);
        
        regPanel.add(new JLabel("Chief Symptom:"));
        symptomCombo = new JComboBox<>();
        for (Symptom s : triageService.getAllSymptoms()) {
            symptomCombo.addItem(s);
        }
        regPanel.add(symptomCombo);
        
        regPanel.add(new JLabel("Pain Level (1-10):"));
        painSlider = new JSlider(1, 10, 5);
        painSlider.setMajorTickSpacing(1);
        painSlider.setPaintTicks(true);
        painSlider.setPaintLabels(true);
        regPanel.add(painSlider);
        
        regPanel.add(new JLabel("Manual Override?"));
        manualOverrideCheck = new JCheckBox("Enable Manual Score");
        regPanel.add(manualOverrideCheck);
        
        regPanel.add(new JLabel("Manual Score (1=Critical, 10=Minor):"));
        manualScoreSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
        manualScoreSpinner.setEnabled(false);
        regPanel.add(manualScoreSpinner);
        
        manualOverrideCheck.addActionListener(e -> {
            boolean selected = manualOverrideCheck.isSelected();
            manualScoreSpinner.setEnabled(selected);
            painSlider.setEnabled(!selected);
            symptomCombo.setEnabled(!selected);
        });
        
        JButton btnRegister = new JButton("Add to Queue");
        btnRegister.setBackground(new Color(46, 204, 113));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegister.addActionListener(this::handleRegister);
        regPanel.add(new JLabel());
        regPanel.add(btnRegister);
        
        String[] columns = {"Priority Score", "Patient ID", "Name", "Symptom", "Pain/10"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        queueTable = new JTable(tableModel);
        queueTable.setRowHeight(25);
        queueTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(queueTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Waiting Queue (Min-Heap)"));
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnUpdate = new JButton("Update Condition (Worsen)");
        btnUpdate.addActionListener(this::handleUpdatePriority);
        
        JButton btnTreat = new JButton("Treat Next Critical Patient");
        btnTreat.setBackground(new Color(231, 76, 60));
        btnTreat.setForeground(Color.WHITE);
        btnTreat.setFont(new Font("Arial", Font.BOLD, 14));
        btnTreat.addActionListener(this::handleTreatPatient);
        
        actionPanel.add(btnUpdate);
        actionPanel.add(btnTreat);
        
        add(regPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleRegister(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter patient name.");
            return;
        }
        
        Symptom selectedSymptom = (Symptom) symptomCombo.getSelectedItem();
        int pain = painSlider.getValue();
        boolean manual = manualOverrideCheck.isSelected();
        int mScore = (int) manualScoreSpinner.getValue();
        
        String symptomName = selectedSymptom != null ? selectedSymptom.getName() : "Unknown";
        
        triageService.registerPatient(name, symptomName, pain, manual, mScore);
        
        nameField.setText("");
        painSlider.setValue(5);
        manualOverrideCheck.setSelected(false);
        manualScoreSpinner.setEnabled(false);
        symptomCombo.setEnabled(true);
        painSlider.setEnabled(true);
        
        refreshTable();
    }

    private void handleTreatPatient(ActionEvent e) {
        Patient p = triageService.treatNextPatient();
        if (p != null) {
            JOptionPane.showMessageDialog(this, "Calling Patient: " + p.getName() + " for treatment.\nSymptom: " + p.getSymptom());
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "The queue is empty.");
        }
    }

    private void handleUpdatePriority(ActionEvent e) {
        int selectedRow = queueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the queue to update.");
            return;
        }
        
        String pid = (String) tableModel.getValueAt(selectedRow, 1);
        int currentScore = (int) tableModel.getValueAt(selectedRow, 0);
        
        String input = JOptionPane.showInputDialog(this, "Enter new severity score for " + pid + " (1-10):", currentScore);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int newScore = Integer.parseInt(input.trim());
                if (newScore < 1 || newScore > 10) throw new NumberFormatException();
                
                triageService.updatePatientPriority(pid, newScore);
                refreshTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid score. Must be between 1 and 10.");
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Patient> list = triageService.getQueueStatus();
        
        list.sort(null); 
        
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                p.getFinalSeverityScore(),
                p.getPatientId(),
                p.getName(),
                p.getSymptom(),
                p.getPainLevel()
            });
        }
    }
}
