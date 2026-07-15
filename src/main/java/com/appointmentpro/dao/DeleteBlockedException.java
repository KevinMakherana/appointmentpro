package com.appointmentpro.dao;

/**
 * Thrown when a delete is rejected by the database because the
 * record still has related data pointing to it (e.g. a staff
 * member or service with existing appointment history).
 */
public class DeleteBlockedException extends RuntimeException {

    public DeleteBlockedException(String message) {
        super(message);
    }

    /**
     * True if the given SQLException looks like a foreign-key
     * constraint violation rather than some other database error.
     */
    public static boolean isConstraintViolation(java.sql.SQLException e) {
        String message = e.getMessage();
        return message != null && message.toLowerCase().contains("constraint");
    }
}