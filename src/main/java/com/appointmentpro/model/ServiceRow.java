package com.appointmentpro.model;

public record ServiceRow(
        int id,
        String name,
        int durationMinutes,
        double price
) {
    @Override
    public String toString() {
        return name + " — " + durationMinutes + " min";
    }
}