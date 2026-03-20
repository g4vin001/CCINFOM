package com.archersground.dbapp.config;

public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/archers_ground_db";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "root123";

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

    private static String getEnvOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
