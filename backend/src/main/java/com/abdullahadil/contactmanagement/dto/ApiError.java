package com.abdullahadil.contactmanagement.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard shape returned to the client whenever a request fails,
 * so the frontend can render a consistent error message regardless
 * of which endpoint or exception produced it.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {

    public ApiError(int status, String error, String message, String path, List<String> details) {
        this(Instant.now(), status, error, message, path, details);
    }

    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, path, List.of());
    }
}
