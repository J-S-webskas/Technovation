package com.example.myappication;

import android.content.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartDataExtractor {

    public static Map<Date, Float> getWalkingSpeedData(Context context) {

        Map<Date, Float> data = new HashMap<>();

        List<AssessmentData> assessments =
                DataStorageManager.loadAllAssessments(context);

        for (AssessmentData assessment : assessments) {

            if (assessment.getBaseline() != null &&
                    assessment.getBaseline().getWalkingSpeed() != null) {

                Float value =
                        assessment.getBaseline()
                                .getWalkingSpeed()
                                .get("overall");

                if (value != null) {
                    data.put(assessment.getTimestamp(), value);
                }
            }
        }

        return data;
    }

    public static Map<Date, Float> getBalanceData(Context context) {

        Map<Date, Float> data = new HashMap<>();

        List<AssessmentData> assessments =
                DataStorageManager.loadAllAssessments(context);

        for (AssessmentData assessment : assessments) {

            if (assessment.getBaseline() != null &&
                    assessment.getBaseline().getBalanceScore() != null) {

                Float value =
                        assessment.getBaseline()
                                .getBalanceScore()
                                .get("overall");

                if (value != null) {
                    data.put(assessment.getTimestamp(), value);
                }
            }
        }

        return data;
    }

    public static Map<Date, Float> getEnduranceData(Context context) {

        Map<Date, Float> data = new HashMap<>();

        List<AssessmentData> assessments =
                DataStorageManager.loadAllAssessments(context);

        for (AssessmentData assessment : assessments) {

            if (assessment.getBaseline() != null &&
                    assessment.getBaseline().getEnduranceScore() != null) {

                Float value =
                        assessment.getBaseline()
                                .getEnduranceScore()
                                .get("overall");

                if (value != null) {
                    data.put(assessment.getTimestamp(), value);
                }
            }
        }

        return data;
    }
}