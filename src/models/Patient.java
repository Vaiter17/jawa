package models;

public class Patient implements Comparable<Patient> {
    private static int idCounter = 1000;
    
    private String patientId;
    private String name;
    private String symptom;
    private int painLevel;
    private int finalSeverityScore;
    private long arrivalTime;

    public Patient(String name, String symptom, int painLevel, int finalSeverityScore) {
        this.patientId = "P-" + (++idCounter);
        this.name = name;
        this.symptom = symptom;
        this.painLevel = painLevel;
        this.finalSeverityScore = finalSeverityScore;
        this.arrivalTime = System.currentTimeMillis();
    }

    public String getPatientId() { return patientId; }
    public String getName() { return name; }
    public String getSymptom() { return symptom; }
    public int getPainLevel() { return painLevel; }
    public int getFinalSeverityScore() { return finalSeverityScore; }
    public long getArrivalTime() { return arrivalTime; }

    public void setFinalSeverityScore(int finalSeverityScore) {
        this.finalSeverityScore = finalSeverityScore;
    }

    @Override
    public int compareTo(Patient other) {
        if (this.finalSeverityScore != other.finalSeverityScore) {
            return Integer.compare(this.finalSeverityScore, other.finalSeverityScore);
        }
        return Long.compare(this.arrivalTime, other.arrivalTime);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Score: %d, Pain: %d/10)", patientId, name, symptom, finalSeverityScore, painLevel);
    }
}
