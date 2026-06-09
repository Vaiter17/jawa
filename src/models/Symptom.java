package models;

public class Symptom {
    private String name;
    private String category;
    private int baseSeverity;
    private boolean requiresPainScale;

    public Symptom(String name, String category, int baseSeverity, boolean requiresPainScale) {
        this.name = name;
        this.category = category;
        this.baseSeverity = baseSeverity;
        this.requiresPainScale = requiresPainScale;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getBaseSeverity() { return baseSeverity; }
    public boolean isRequiresPainScale() { return requiresPainScale; }

    @Override
    public String toString() {
        return name;
    }
}
