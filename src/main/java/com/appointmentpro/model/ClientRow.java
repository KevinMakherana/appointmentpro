package com.appointmentpro.model;

public record ClientRow(
        int id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String notes
) {
    public String fullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return fullName();
    }
}