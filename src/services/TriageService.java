package services;

import models.Patient;
import models.Symptom;
import datastructures.MinHeapQueue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TriageService {
    private MinHeapQueue queue;
    private Map<String, Symptom> symptomDatabase;

    public TriageService() {
        queue = new MinHeapQueue();
        symptomDatabase = new HashMap<>();
        
        java.io.File file = new java.io.File("resources/symptoms_severity.json");
        if (!file.exists()) {
            file = new java.io.File("../resources/symptoms_severity.json");
        }
        loadSymptoms(file.getPath());
    }

    private void loadSymptoms(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String json = jsonBuilder.toString();
            
            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{\\s*\"category\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"baseSeverity\"\\s*:\\s*(\\d+)\\s*,\\s*\"requiresPainScale\"\\s*:\\s*(true|false)\\s*\\}");
            Matcher matcher = pattern.matcher(json);
            
            while (matcher.find()) {
                String name = matcher.group(1);
                String category = matcher.group(2);
                int baseSeverity = Integer.parseInt(matcher.group(3));
                boolean requiresPainScale = Boolean.parseBoolean(matcher.group(4));
                
                symptomDatabase.put(name, new Symptom(name, category, baseSeverity, requiresPainScale));
            }
        } catch (IOException e) {
            System.err.println("Could not load symptoms database: " + e.getMessage());
        }
    }

    public int calculateFinalSeverity(String symptomName, int painLevel) {
        Symptom symptom = symptomDatabase.get(symptomName);
        if (symptom == null) {
            return 5;
        }

        int base = symptom.getBaseSeverity();
        
        if (!symptom.isRequiresPainScale()) {
            return base;
        }

        int modifier = 0;
        if (painLevel >= 9) modifier = -3;
        else if (painLevel >= 7) modifier = -2;
        else if (painLevel >= 5) modifier = -1;
        else if (painLevel <= 2) modifier = 1;
        else modifier = 0;

        int finalScore = base + modifier;
        
        if (finalScore < 1) finalScore = 1;
        if (finalScore > 10) finalScore = 10;
        
        return finalScore;
    }

    public Patient registerPatient(String name, String symptomName, int painLevel, boolean isManualOverride, int manualScore) {
        int finalScore;
        if (isManualOverride) {
            finalScore = manualScore;
        } else {
            finalScore = calculateFinalSeverity(symptomName, painLevel);
        }
        
        Patient p = new Patient(name, symptomName, painLevel, finalScore);
        queue.insert(p);
        return p;
    }

    public Patient treatNextPatient() {
        return queue.extractMin();
    }

    public boolean updatePatientPriority(String patientId, int newScore) {
        return queue.updatePriority(patientId, newScore);
    }

    public List<Patient> getQueueStatus() {
        return queue.getAllPatients();
    }
    
    public List<Symptom> getAllSymptoms() {
        return new ArrayList<>(symptomDatabase.values());
    }
}
