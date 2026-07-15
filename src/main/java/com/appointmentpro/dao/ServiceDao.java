package com.appointmentpro.dao;

import com.appointmentpro.db.DatabaseManager;
import com.appointmentpro.model.ServiceRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides read/write access to services.
 */
public class ServiceDao {

    private static final String FIND_ALL_SQL = """
            SELECT id, name, duration_minutes, price
            FROM services
            ORDER BY name
            """;

    private static final String INSERT_SQL = """
            INSERT INTO services (name, duration_minutes, price)
            VALUES (?, ?, ?)
            """;

    private static final String DELETE_SQL = """
            DELETE FROM services WHERE id = ?
            """;

    public List<ServiceRow> findAll() {
        List<ServiceRow> rows = new ArrayList<>();

        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rows.add(new ServiceRow(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("duration_minutes"),
                        resultSet.getDouble("price")
                ));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load services.", e);
        }

        return rows;
    }

    public void insert(String name, int durationMinutes, double price) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, name);
            statement.setInt(2, durationMinutes);
            statement.setDouble(3, price);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save service.", e);
        }
    }

    public void delete(int serviceId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setInt(1, serviceId);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (DeleteBlockedException.isConstraintViolation(e)) {
                throw new DeleteBlockedException(
                        "This service has existing appointment history and can't be deleted.");
            }
            throw new IllegalStateException("Failed to delete service.", e);
        }
    }
}