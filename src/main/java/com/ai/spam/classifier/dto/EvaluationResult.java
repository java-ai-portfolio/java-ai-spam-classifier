package com.ai.spam.classifier.dto;

public record EvaluationResult(
        double accuracy,
        double precision,
        double recall,
        double f1Score,
        int totalTrained,
        int totalTested,
        int correctPredictions,
        long trainingTimeMs
) {
    public boolean isProductionReady() {
        return accuracy >= 0.90 && f1Score >= 0.85;
    }
}