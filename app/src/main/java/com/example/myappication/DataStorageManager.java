package com.example.myappication;

import android.content.Context;
import android.util.Log;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStorageManager {
    private static final String TAG = "DataStorageManager";
    private static final String FILE_EXTENSION = ".dat";

    /**
     * Save assessment to internal storage
     */
    public static boolean saveAssessment(Context context, AssessmentData assessment) {
        String filename = "assessment_" + assessment.getSessionId() + FILE_EXTENSION;

        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(assessment);
            oos.close();
            fos.close();

            Log.d(TAG, "Assessment saved: " + filename);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error saving assessment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load assessment from internal storage
     */
    public static AssessmentData loadAssessment(Context context, String sessionId) {
        String filename = "assessment_" + sessionId + FILE_EXTENSION;

        try {
            FileInputStream fis = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);

            AssessmentData assessment = (AssessmentData) ois.readObject();

            ois.close();
            fis.close();

            Log.d(TAG, "Assessment loaded: " + sessionId);
            return assessment;

        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading assessment: " + e.getMessage());
            return null;
        }
    }

    /**
     * List all saved assessments
     */
    public static List<String> listAssessments(Context context) {
        List<String> assessments = new ArrayList<>();
        File[] files = context.getFilesDir().listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("assessment_") && file.getName().endsWith(FILE_EXTENSION)) {
                    String sessionId = file.getName()
                            .replace("assessment_", "")
                            .replace(FILE_EXTENSION, "");
                    assessments.add(sessionId);
                }
            }
        }

        return assessments;
    }

    /**
     * Delete an assessment
     */
    public static boolean deleteAssessment(Context context, String sessionId) {
        String filename = "assessment_" + sessionId + FILE_EXTENSION;
        File file = new File(context.getFilesDir(), filename);

        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "Assessment deleted: " + sessionId + " - " + deleted);
            return deleted;
        }

        return false;
    }
}