package com.appointmentpro.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and one-time schema
 * initialization for the AppointmentPro application.
 */
public final class DatabaseManager {

    private static final String DB_FOLDER = "data";
    private static final String DB_FILE_NAME = "appointmentpro.db";
    private static final String SCHEMA_RESOURCE = "/schema.sql";

    private static DatabaseManager instance;

    private final Path dbFilePath;
    private final String jdbcUrl;

    private DatabaseManager() {
        Path dbFolderPath = Paths.get(DB_FOLDER);
        this.dbFilePath = dbFolderPath.resolve(DB_FILE_NAME);
        this.jdbcUrl = "jdbc:sqlite:" + dbFilePath.toAbsolutePath();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Opens a new connection to the SQLite database with foreign keys
     * enabled. Callers should use try-with-resources to close it.
     */
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement pragmaStatement = connection.createStatement()) {
            pragmaStatement.execute("PRAGMA foreign_keys = ON;");
        }
        return connection;
    }

    /**
     * Ensures the data folder exists and, on first run only, executes
     * schema.sql to create the tables and load seed data.
     */
    public void initializeDatabase() {
        try {
            Files.createDirectories(dbFilePath.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Could not create data folder for the database.", e);
        }

        boolean isFirstRun = !Files.exists(dbFilePath);
        if (!isFirstRun) {
            return;
        }

        String schemaSql = readSchemaResource();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            for (String sqlStatement : schemaSql.split(";")) {
                String trimmed = sqlStatement.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize the database schema.", e);
        }
    }

    private String readSchemaResource() {
        try (InputStream inputStream = DatabaseManager.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not find " + SCHEMA_RESOURCE + " on the classpath.");
            }

            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("--")) {
                        builder.append(line).append("\n");
                    }
                }
            }
            return builder.toString();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read schema.sql from resources.", e);
        }
    }
}
