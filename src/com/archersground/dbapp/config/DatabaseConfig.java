package com.archersground.dbapp.config;

public final class DatabaseConfig {
    private static final String URL = "jdbc:mysql://localhost:3306/archers_ground_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "As_031606";

    private DatabaseConfig() {
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}
