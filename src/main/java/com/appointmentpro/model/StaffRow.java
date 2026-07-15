package com.appointmentpro.model;

public record StaffRow(
        int id,
        String firstName,
        String lastName,
        String role,
        String phone,
        boolean active
) {
    public String fullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return fullName() + " (" + role + ")";
    }
}