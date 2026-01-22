package com.example.myappication;

import java.util.*;

public class BiomechanicalDataGenerator {

    public static List<BiomechanicalChart> generateAllMetrics() {
        List<BiomechanicalChart> metrics = new ArrayList<>();

//        metrics.add(generatePelvicSway());
//        metrics.add(generatePosturalStability());
//        metrics.add(generateGaitVariability());
//        metrics.add(generateKneeKinematics());
//        metrics.add(generateAnkleKinematics());

        return metrics;
    }

//    private static EnduranceChart generatePelvicSway() {
//        Map<Date, Float> data = generateTrendData(
//                5, 25, 0.03f, 6, 30, 15, 25, true
//        );
//        return new EnduranceChart(1, data, determineCondition(data, 15, 25, true));
//    }

//    private static PosturalStabilityChart generatePosturalStability() {
//        // LOWER values are better for stability (COP velocity)
//        Map<Date, Float> data = generateTrendData(
//                200, 50, -0.2f, 100, 400, 150, 300, false
//        );
//        return new PosturalStabilityChart(2, data, determineCondition(data, 150, 300, false));
//    }
//
//    private static GaitVariabilityChart generateGaitVariability() {
//        // LOWER values are better (less variability)
//        Map<Date, Float> data = generateTrendData(
//                20, 5, -0.1f, 8, 30, 15, 25, false
//        );
//        return new GaitVariabilityChart(3, data, determineCondition(data, 15, 25, false));
//    }
//
//    private static KneeKinematicsChart generateKneeKinematics() {
//        // HIGHER values are better (more knee flexion)
//        Map<Date, Float> data = generateTrendData(
//                40, 65, 0.08f, 10, 75, 50, 60, true
//        );
//        return new KneeKinematicsChart(4, data, determineCondition(data, 50, 60, true));
//    }
//
//    private static AnkleKinematicsChart generateAnkleKinematics() {
//        // HIGHER values are better (more dorsiflexion)
//        Map<Date, Float> data = generateTrendData(
//                8, 20, 0.05f, 6, 25, 12, 15, true
//        );
//        return new AnkleKinematicsChart(5, data, determineCondition(data, 12, 15, true));
//    }

    private static Map<Date, Float> generateTrendData(float startValue, float targetValue,
                                                      float dailyChange, float variation,
                                                      float maxValue, float minValue,
                                                      boolean increasingIsBad) {
        Map<Date, Float> data = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        Random rand = new Random();

        for (int i = 0; i < 365; i++) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date date = cal.getTime();

            float base = startValue + (dailyChange * i);
            if (!increasingIsBad) {
                base = startValue - (dailyChange * i); // Decrease over time if lower is better
            }

            float randomVar = (rand.nextFloat() * 2 - 1) * variation; // -variation to +variation
            float value = base + randomVar;

            // Clamp to min/max
            value = Math.max(minValue, Math.min(maxValue, value));

            data.put(date, value);
        }

        return data;
    }

    private static String determineCondition(Map<Date, Float> data,
                                             float warningThreshold,
                                             float criticalThreshold,
                                             boolean higherIsWorse) {
        List<Date> dates = new ArrayList<>(data.keySet());
        Collections.sort(dates);
        float latestValue = data.get(dates.get(dates.size() - 1));

        if (higherIsWorse) {
            if (latestValue >= criticalThreshold) return "Critical";
            if (latestValue >= warningThreshold) return "Elevated";
        } else {
            if (latestValue <= criticalThreshold) return "Critical";
            if (latestValue <= warningThreshold) return "Elevated";
        }
        return "Normal";
    }
}