package com.example.backend_quanlynhahanglau.util;

import java.time.LocalDateTime;

public class DateUtils {
    
    /**
     * Parse date string to LocalDateTime
     * Supports both date-only (yyyy-MM-dd) and date-time (yyyy-MM-ddTHH:mm:ss) formats
     * Compatible with both MySQL and SQL Server
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty");
        }
        
        // Remove any URL encoding or extra spaces
        dateTimeStr = dateTimeStr.trim();
        
        // If only date (yyyy-MM-dd), add time 00:00:00
        if (dateTimeStr.length() == 10 && dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDateTime.parse(dateTimeStr + "T00:00:00");
        }
        
        // If date-time format, parse directly
        // Handle both "T" and " " as separator
        String normalized = dateTimeStr.replace(" ", "T");
        // Remove milliseconds if present
        if (normalized.contains(".")) {
            normalized = normalized.substring(0, normalized.indexOf("."));
        }
        // Ensure we have seconds
        if (normalized.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
            normalized += ":00";
        }
        
        return LocalDateTime.parse(normalized);
    }
}

