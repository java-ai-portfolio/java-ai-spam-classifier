package com.ai.spam.classifier.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String error,
        int status,
        String path,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String error, int status, String path) {
        return new ErrorResponse(error, status, path, LocalDateTime.now());
    }
}