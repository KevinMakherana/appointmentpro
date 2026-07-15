package com.appointmentpro.dao;

import com.appointmentpro.db.DatabaseManager;
import com.appointmentpro.model.ClientRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides read/write access to the clients table.
 */
public class ClientDao {

    private static final String FIND_ALL_SQL = """
            SELECT id, first_name, last_name, phone, email, notes
            FROM clients
            ORDER BY last_name, first_name
            """;

    private static final String INSERT_SQL = """
            INSERT INTO clients (first_name, last_name, phone, email, notes)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String DELETE_SQL = """
            DELETE FROM clients WHERE id = ?
            """;

    public List<ClientRow> findAll() {
        List<ClientRow> rows = new ArrayList<>();

        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rows.add(new ClientRow(
                        resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("phone"),
                        resultSet.getString("email"),
                        resultSet.getString("notes")
                ));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load clients.", e);
        }

        return rows;
    }

    public void insert(String firstName, String lastName, String phone, String email, String notes) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, phone);
            statement.setString(4, email == null || email.isBlank() ? null : email);
            statement.setString(5, notes == null || notes.isBlank() ? null : notes);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save client.", e);
        }
    }

    public void delete(int clientId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setInt(1, clientId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete client.", e);
        }
    }
}