package com.assessment.booking.dto.response;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return of(status, message, path, Map.of());
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        );
    }
}

