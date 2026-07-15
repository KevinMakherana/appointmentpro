package com.appointmentpro.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Read-only, display-ready view of an appointment joined with
 * client, staff, and service names — used to populate the
 * appointments table in the UI.
 */
public record AppointmentRow(
        int id,
        String clientName,
        String staffName,
        String serviceName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String status
) {
}
