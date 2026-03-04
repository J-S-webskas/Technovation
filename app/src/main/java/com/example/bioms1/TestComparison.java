package com.example.bioms1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TestComparison implements Serializable {
    // Test type and timing
    private TestResult.TestType testType;
    private float duration; // seconds

    // DUAL TASK vs BASELINE changes
    private float dualTaskSpeedChange;       // e.g., -25% (slower with counting)
    private float dualTaskBalanceChange;     // e.g., -15% (worse balance with counting)
    private float dualTaskARJChange;         // e.g., +30% (more stiff with counting)
    private float dualTaskEnduranceChange;   // e.g., -50% (worse endurance with counting)

    // SENSORY ATAXIA vs BASELINE changes
    private float sensoryAtaxiaSpeedChange;      // e.g., -40% (much slower eyes closed)
    private float sensoryAtaxiaBalanceChange;    // e.g., -50% (much worse balance eyes closed)
    private float sensoryAtaxiaARJChange;        // e.g., +50% (more stiff eyes closed)
    private float sensoryAtaxiaEnduranceChange;  // e.g., -50% (worse endurance eyes closed)

    // Aggregated metrics for comprehensive analysis
    private float averageSpeed;
    private float speedChangePercent;
    private Map<String, Float> averageStiffness;       // ARJ values per joint
    private Map<String, Float> stiffnessChangesPercent;
    private Map<String, Float> averageAsymmetry;
    private Map<String, Float> asymmetryChangesPercent;

    // Impact scores (clinical relevance)
    private float speedImpact;
    private float stiffnessImpact;
    private float asymmetryImpact;
    private float balanceImpact;
    private float enduranceImpact;

    // ASYMMETRY SCORES
    private Map<String, Float> asymmetryScores;  // hip, knee, ankle

    // Default constructor
    public TestComparisons() {
        this.asymmetryScores = new HashMap<>();
        this.averageStiffness = new HashMap<>();
        this.stiffnessChangesPercent = new HashMap<>();
        this.averageAsymmetry = new HashMap<>();
        this.asymmetryChangesPercent = new HashMap<>();
    }

    // Full constructor for detailed initialization
    public TestComparisons(TestResult.TestType testType, float duration,
                           float averageSpeed, float speedChangePercent,
                           Map<String, Float> averageStiffness,
                           Map<String, Float> stiffnessChangesPercent,
                           Map<String, Float> averageAsymmetry,
                           Map<String, Float> asymmetryChangesPercent,
                           float speedImpact, float stiffnessImpact,
                           float asymmetryImpact) {
        this();
        this.testType = testType;
        this.duration = duration;
        this.averageSpeed = averageSpeed;
        this.speedChangePercent = speedChangePercent;
        this.averageStiffness = averageStiffness;
        this.stiffnessChangesPercent = stiffnessChangesPercent;
        this.averageAsymmetry = averageAsymmetry;
        this.asymmetryChangesPercent = asymmetryChangesPercent;
        this.speedImpact = speedImpact;
        this.stiffnessImpact = stiffnessImpact;
        this.asymmetryImpact = asymmetryImpact;
    }

    // ==================== SETTERS ====================

    public void setTestType(TestResult.TestType testType) {
        this.testType = testType;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void setAverageSpeed(float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public void setSpeedChangePercent(float speedChangePercent) {
        this.speedChangePercent = speedChangePercent;
    }

    public void setAverageStiffness(Map<String, Float> averageStiffness) {
        this.averageStiffness = averageStiffness;
    }

    public void setStiffnessChangesPercent(Map<String, Float> stiffnessChangesPercent) {
        this.stiffnessChangesPercent = stiffnessChangesPercent;
    }

    public void setAverageAsymmetry(Map<String, Float> averageAsymmetry) {
        this.averageAsymmetry = averageAsymmetry;
    }

    public void setAsymmetryChangesPercent(Map<String, Float> asymmetryChangesPercent) {
        this.asymmetryChangesPercent = asymmetryChangesPercent;
    }

    public void setSpeedImpact(float speedImpact) {
        this.speedImpact = speedImpact;
    }

    public void setStiffnessImpact(float stiffnessImpact) {
        this.stiffnessImpact = stiffnessImpact;
    }

    public void setAsymmetryImpact(float asymmetryImpact) {
        this.asymmetryImpact = asymmetryImpact;
    }

    public void setBalanceImpact(float balanceImpact) {
        this.balanceImpact = balanceImpact;
    }

    public void setEnduranceImpact(float enduranceImpact) {
        this.enduranceImpact = enduranceImpact;
    }

    public void setAsymmetryScores(Map<String, Float> asymmetryScores) {
        this.asymmetryScores = asymmetryScores;
    }

    // ==================== DUAL TASK SETTERS ====================

    public void setDualTaskSpeedChange(float value) {
        this.dualTaskSpeedChange = value;
    }

    public void setDualTaskBalanceChange(float value) {
        this.dualTaskBalanceChange = value;
    }

    public void setDualTaskEnduranceChange(float value) {
        this.dualTaskEnduranceChange = value;
    }

    public void setDualTaskARJChange(float value) {
        this.dualTaskARJChange = value;
    }

    // ==================== SENSORY ATAXIA SETTERS ====================

    public void setSensoryAtaxiaSpeedChange(float value) {
        this.sensoryAtaxiaSpeedChange = value;
    }

    public void setSensoryAtaxiaBalanceChange(float value) {
        this.sensoryAtaxiaBalanceChange = value;
    }

    public void setSensoryAtaxiaARJChange(float value) {
        this.sensoryAtaxiaARJChange = value;
    }

    public void setSensoryAtaxiaEnduranceChange(float value) {
        this.sensoryAtaxiaEnduranceChange = value;
    }

    // ==================== GETTERS ====================

    public TestResult.TestType getTestType() {
        return testType;
    }

    public float getDuration() {
        return duration;
    }

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public float getSpeedChangePercent() {
        return speedChangePercent;
    }

    public Map<String, Float> getAverageStiffness() {
        return averageStiffness;
    }

    public Map<String, Float> getStiffnessChangesPercent() {
        return stiffnessChangesPercent;
    }

    public Map<String, Float> getAverageAsymmetry() {
        return averageAsymmetry;
    }

    public Map<String, Float> getAsymmetryChangesPercent() {
        return asymmetryChangesPercent;
    }

    public float getSpeedImpact() {
        return speedImpact;
    }

    public float getStiffnessImpact() {
        return stiffnessImpact;
    }

    public float getAsymmetryImpact() {
        return asymmetryImpact;
    }

    public float getBalanceImpact() {
        return balanceImpact;
    }

    public float getEnduranceImpact() {
        return enduranceImpact;
    }

    public Map<String, Float> getAsymmetryScores() {
        return asymmetryScores;
    }

    public float getDualTaskSpeedChange() {
        return dualTaskSpeedChange;
    }

    public float getDualTaskBalanceChange() {
        return dualTaskBalanceChange;
    }

    public float getDualTaskARJChange() {
        return dualTaskARJChange;
    }

    public float getDualTaskEnduranceChange() {
        return dualTaskEnduranceChange;
    }

    public float getSensoryAtaxiaSpeedChange() {
        return sensoryAtaxiaSpeedChange;
    }

    public float getSensoryAtaxiaBalanceChange() {
        return sensoryAtaxiaBalanceChange;
    }

    public float getSensoryAtaxiaARJChange() {
        return sensoryAtaxiaARJChange;
    }

    public float getSensoryAtaxiaEnduranceChange() {
        return sensoryAtaxiaEnduranceChange;
    }

    // ==================== ANALYSIS METHODS ====================

    /**
     * The numbers here are in percentage
     * TODO: Only speed in here for now. Need balance, endurance, and spasticity
     */
    public String getSpeedComparison() {
        StringBuilder summary = new StringBuilder();

        switch (testType) {
            case DUAL_TASK:
                if (speedImpact > 50) {
                    summary.append("Large speed reduction (").append(String.format("%.1f", speedImpact))
                            .append("%) during counting");
                } else if (speedImpact > 20) {
                    summary.append("Moderate speed reduction (").append(String.format("%.1f", speedImpact))
                            .append("%) during counting");
                } else {
                    summary.append("Minimal speed change (").append(String.format("%.1f", speedImpact))
                            .append("%) during counting");
                }
                break;

            case SENSORY_ATAXIA:
                if (speedImpact > 60) {
                    summary.append("Large reliance on vision (").append(String.format("%.1f", speedImpact))
                            .append("% speed reduction)");
                } else if (speedImpact > 30) {
                    summary.append("Moderate reliance on vision (").append(String.format("%.1f", speedImpact))
                            .append("% speed reduction)");
                } else {
                    summary.append("Good speed without vision (").append(String.format("%.1f", speedImpact))
                            .append("% speed change)");
                }
                break;
        }

        return summary.toString();
    }

    /**
     * Get balance analysis based on test type and changes
     */
    public String getBalanceComparison() {
        StringBuilder summary = new StringBuilder();

        switch (testType) {
            case DUAL_TASK:
                if (balanceImpact > 50) {
                    summary.append("Large balance degradation (").append(String.format("%.1f", balanceImpact))
                            .append("%) during dual task");
                } else if (balanceImpact > 20) {
                    summary.append("Moderate balance degradation (").append(String.format("%.1f", balanceImpact))
                            .append("%) during dual task");
                } else {
                    summary.append("Minimal balance change (").append(String.format("%.1f", balanceImpact))
                            .append("%) during dual task");
                }
                break;

            case SENSORY_ATAXIA:
                if (balanceImpact > 60) {
                    summary.append("Large balance reliance on vision (").append(String.format("%.1f", balanceImpact))
                            .append("% degradation)");
                } else if (balanceImpact > 30) {
                    summary.append("Moderate balance reliance on vision (").append(String.format("%.1f", balanceImpact))
                            .append("% degradation)");
                } else {
                    summary.append("Good balance without vision (").append(String.format("%.1f", balanceImpact))
                            .append("% change)");
                }
                break;
        }

        return summary.toString();
    }

    /**
     * Get endurance analysis based on test type and changes
     */
    public String getEnduranceComparison() {
        StringBuilder summary = new StringBuilder();

        switch (testType) {
            case DUAL_TASK:
                if (enduranceImpact > 50) {
                    summary.append("Large endurance reduction (").append(String.format("%.1f", enduranceImpact))
                            .append("%) during counting");
                } else if (enduranceImpact > 20) {
                    summary.append("Moderate endurance reduction (").append(String.format("%.1f", enduranceImpact))
                            .append("%) during counting");
                } else {
                    summary.append("Minimal endurance change (").append(String.format("%.1f", enduranceImpact))
                            .append("%) during counting");
                }
                break;

            case SENSORY_ATAXIA:
                if (enduranceImpact > 60) {
                    summary.append("Large endurance reduction (").append(String.format("%.1f", enduranceImpact))
                            .append("%) without vision");
                } else if (enduranceImpact > 30) {
                    summary.append("Moderate endurance reduction (").append(String.format("%.1f", enduranceImpact))
                            .append("%) without vision");
                } else {
                    summary.append("Good endurance without vision (").append(String.format("%.1f", enduranceImpact))
                            .append("% change)");
                }
                break;
        }

        return summary.toString();
    }
}