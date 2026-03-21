package com.archersground.dbapp.config;

public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/archers_ground_db";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "root123";
    private static final int DEFAULT_PROCESSING_EMPLOYEE_ID = 1;

    private DatabaseConfig() {
    }

    public static String getUrl() {
        return getEnvOrDefault("DB_URL", DEFAULT_URL);
    }

    public static String getUsername() {
        return getEnvOrDefault("DB_USERNAME", DEFAULT_USERNAME);
    }

    public static String getPassword() {
        return getEnvOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
    }

    public static int getDefaultProcessingEmployeeId() {
        String value = System.getenv("DEFAULT_PROCESSING_EMPLOYEE_ID");
        if (value == null || value.isBlank()) {
            return DEFAULT_PROCESSING_EMPLOYEE_ID;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("DEFAULT_PROCESSING_EMPLOYEE_ID must be a whole number.");
        }
    }

    private static String getEnvOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
