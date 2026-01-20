package com.example.myappication;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/*
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",  // Unique ID
  "timestamp": "2024-12-15T10:30:00Z",                 // When recorded
  "patientId": "patient_001",                          // Who it's for
  "frames": [                                          // 36 landmarks per frame
    {
      "timestamp": 0,                                  // Time since start (ms)
      "landmarks": [[x,y,z], [x,y,z], ...36 times],    // Body coordinates
      "confidence": 0.9                                // Detection confidence
    },
    // ... hundreds more frames
  ],
  "scores": {                                          // Final 4 clinical scores
    "spasticity": 45.2,
    "balance": 78.5,
    "walkingSpeed": 1.2,
    "endurance": 65.0
  }
}
 */
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class AssessmentData implements Serializable {
    private String sessionId;
    private Date timestamp;
    private String patientId;

    // 1. BASELINE DATA (Normal walking)
    private BaselineAssessment baseline;

    // 2. DUAL TASK DATA (Counting while walking)
    private DualTaskAssessment dualTask;

    // 3. SENSORY ATAXIA DATA (Eyes closed walking)
    private SensoryAtaxiaAssessment sensoryAtaxia;

    // Auto-calculated final scores (READ-ONLY)
    private TestComparisons comparisons;

    // Status tracking
    private AssessmentStatus status;

    public enum AssessmentStatus {
        NOT_STARTED,
        BASELINE_COMPLETED,
        DUAL_TASK_COMPLETED,
        SENSORY_ATAXIA_COMPLETED,
        ALL_COMPLETED,
    }

    public AssessmentData(String patientId) {
        this.sessionId = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.patientId = patientId;
        this.baseline = new BaselineAssessment();
        this.dualTask = new DualTaskAssessment();
        this.sensoryAtaxia = new SensoryAtaxiaAssessment();
    }
    public String getSessionId() { return sessionId; }
    public Date getTimestamp() { return timestamp; }
    public String getPatientId() { return patientId; }
}

class JointAngleData implements Serializable {
    // angles[frameIndex][jointIndex] in degrees
    private float[][] angles;
    private float[] timestamps; // seconds

    // 12 key joints for gait analysis..maybe we'll do 33 if we have time to create 3D skeletons
    private static final String[] JOINT_NAMES = {
            "pelvis_tilt", "pelvis_list", "pelvis_rotation",
            "hip_flexion_r", "hip_adduction_r", "hip_rotation_r",
            "knee_angle_r", "ankle_angle_r",
            "hip_flexion_l", "hip_adduction_l", "hip_rotation_l",
            "knee_angle_l", "ankle_angle_l"
    };

    public JointAngleData(int numFrames) {
        angles = new float[numFrames][JOINT_NAMES.length];
        timestamps = new float[numFrames];
    }

    public void setFrame(int frameIndex, float time, float[] frameAngles) {
        if (frameIndex < angles.length) {
            timestamps[frameIndex] = time;
            System.arraycopy(frameAngles, 0, angles[frameIndex], 0,
                    Math.min(frameAngles.length, JOINT_NAMES.length));
        }
    }

    public float[][] getAngles() { return angles; }
    public float[] getTimestamps() { return timestamps; }
    public static String[] getJointNames() { return JOINT_NAMES; }
}

// BaselineAssessment
class BaselineAssessment implements Serializable {
    private JointAngleData jointAngles;       // joint angles
    private float walkingSpeed;               // m/sec
    private Map<String, Float> stiffness;     // Joint stiffness scores 0-100
    private Map<String, Float> asymmetry;     // Joint asymmetry %
    private float balanceScore;               // Calculated balance
    private float enduranceScore;             // Baseline endurance
    // ... getters/setters?
}

// DualTaskAssessment
class DualTaskAssessment implements Serializable {
    private JointAngleData jointAngles;
    private float walkingSpeed;
    private Map<String, Float> stiffness;
    private Map<String, Float> asymmetry;
    private float balanceScore;
    private float enduranceScore;
    private float cognitiveLoadImpact;        // How much counting affects walking
    // ... getters/setters?
}

// SensoryAtaxiaAssessment
class SensoryAtaxiaAssessment implements Serializable {
    private JointAngleData jointAngles;
    private float walkingSpeed;
    private Map<String, Float> stiffness;
    private Map<String, Float> asymmetry;
    private float balanceScore;
    private float enduranceScore;
    private float visualDependence;           // How much eyes closed affects walking
    // ... getters/setters
}

// TestComparisons
class TestComparisons implements Serializable {
    // DUAL TASK vs BASELINE changes
    private float dualTaskSpeedChange;       // e.g., -25% (slower with counting)
    private float dualTaskBalanceChange;     // e.g., -15% (worse balance with counting)
    private float dualTaskStiffnessChange;   // e.g., +30% (more stiff with counting)

    // SENSORY ATAXIA vs BASELINE changes
    private float sensoryAtaxiaSpeedChange;  // e.g., -40% (much slower eyes closed)
    private float sensoryAtaxiaBalanceChange; // e.g., -50% (much worse balance eyes closed)

    // ASYMMETRY SCORES
    private Map<String, Float> asymmetryScores;  // hip, knee, ankle
}