package com.ai.spam.classifier.dto;

public record ClassificationResult(String category, double confidence) {
    public boolean isSpam() {
        return "spam".equalsIgnoreCase(category);
    }
}