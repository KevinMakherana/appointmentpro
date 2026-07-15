package com.appointmentpro.dao;

import com.appointmentpro.db.DatabaseManager;
import com.appointmentpro.model.StaffRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides read/write access to staff members.
 */
public class StaffDao {

    private static final String FIND_ACTIVE_SQL = """
            SELECT id, first_name, last_name, role, phone, active
            FROM staff
            WHERE active = 1
            ORDER BY last_name, first_name
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id, first_name, last_name, role, phone, active
            FROM staff
            ORDER BY active DESC, last_name, first_name
            """;

    private static final String INSERT_SQL = """
            INSERT INTO staff (first_name, last_name, role, phone, active)
            VALUES (?, ?, ?, ?, 1)
            """;

    private static final String SET_ACTIVE_SQL = """
            UPDATE staff SET active = ? WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM staff WHERE id = ?
            """;

    public List<StaffRow> findActive() {
        return runQuery(FIND_ACTIVE_SQL);
    }

    public List<StaffRow> findAll() {
        return runQuery(FIND_ALL_SQL);
    }

    private List<StaffRow> runQuery(String sql) {
        List<StaffRow> rows = new ArrayList<>();

        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rows.add(new StaffRow(
                        resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("role"),
                        resultSet.getString("phone"),
                        resultSet.getInt("active") == 1
                ));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load staff.", e);
        }

        return rows;
    }

    public void insert(String firstName, String lastName, String role, String phone) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, role);
            statement.setString(4, phone);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save staff member.", e);
        }
    }

    public void setActive(int staffId, boolean active) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SET_ACTIVE_SQL)) {

            statement.setInt(1, active ? 1 : 0);
            statement.setInt(2, staffId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update staff status.", e);
        }
    }

    public void delete(int staffId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setInt(1, staffId);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (DeleteBlockedException.isConstraintViolation(e)) {
                throw new DeleteBlockedException(
                        "This staff member has existing appointment history and can't be deleted. "
                                + "Use \"Toggle Active Status\" to deactivate them instead.");
            }
            throw new IllegalStateException("Failed to delete staff member.", e);
        }
    }
}