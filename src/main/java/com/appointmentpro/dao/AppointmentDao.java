package com.appointmentpro.dao;

import com.appointmentpro.db.DatabaseManager;
import com.appointmentpro.model.AppointmentRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides read/write access to the appointments table, including
 * overlap detection, status updates, and deletion.
 */
public class AppointmentDao {

    private static final String FIND_ALL_SQL = """
            SELECT a.id,
                   c.first_name || ' ' || c.last_name AS client_name,
                   s.first_name || ' ' || s.last_name AS staff_name,
                   sv.name AS service_name,
                   a.appointment_date,
                   a.start_time,
                   a.end_time,
                   a.status
            FROM appointments a
            JOIN clients c ON a.client_id = c.id
            JOIN staff s ON a.staff_id = s.id
            JOIN services sv ON a.service_id = sv.id
            ORDER BY a.appointment_date, a.start_time
            """;

    private static final String CONFLICT_CHECK_SQL = """
            SELECT COUNT(*) AS conflict_count
            FROM appointments
            WHERE staff_id = ?
              AND appointment_date = ?
              AND status != 'Cancelled'
              AND start_time < ?
              AND end_time > ?
            """;

    private static final String INSERT_SQL = """
            INSERT INTO appointments (client_id, staff_id, service_id, appointment_date, start_time, end_time, status)
            VALUES (?, ?, ?, ?, ?, ?, 'Scheduled')
            """;

    private static final String UPDATE_STATUS_SQL = """
            UPDATE appointments SET status = ? WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM appointments WHERE id = ?
            """;

    public List<AppointmentRow> findAll() {
        List<AppointmentRow> rows = new ArrayList<>();

        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rows.add(new AppointmentRow(
                        resultSet.getInt("id"),
                        resultSet.getString("client_name"),
                        resultSet.getString("staff_name"),
                        resultSet.getString("service_name"),
                        LocalDate.parse(resultSet.getString("appointment_date")),
                        LocalTime.parse(resultSet.getString("start_time")),
                        LocalTime.parse(resultSet.getString("end_time")),
                        resultSet.getString("status")
                ));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load appointments.", e);
        }

        return rows;
    }

    public boolean hasConflict(int staffId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(CONFLICT_CHECK_SQL)) {

            statement.setInt(1, staffId);
            statement.setString(2, date.toString());
            statement.setString(3, endTime.toString());
            statement.setString(4, startTime.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("conflict_count") > 0;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check for scheduling conflicts.", e);
        }
    }

    public void insert(int clientId, int staffId, int serviceId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setInt(1, clientId);
            statement.setInt(2, staffId);
            statement.setInt(3, serviceId);
            statement.setString(4, date.toString());
            statement.setString(5, startTime.toString());
            statement.setString(6, endTime.toString());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save appointment.", e);
        }
    }

    public void updateStatus(int appointmentId, String status) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS_SQL)) {

            statement.setString(1, status);
            statement.setInt(2, appointmentId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update appointment status.", e);
        }
    }

    public void delete(int appointmentId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setInt(1, appointmentId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete appointment.", e);
        }
    }
}