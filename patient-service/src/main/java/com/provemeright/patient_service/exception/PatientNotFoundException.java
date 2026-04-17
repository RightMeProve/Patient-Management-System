package com.provemeright.patient_service.exception;

/**
 * ============================================================================
 * CUSTOM EXCEPTION - PatientNotFoundException
 * ============================================================================
 *
 * This exception is thrown when an operation refers to a Patient ID that
 * does not exist in the database (e.g., during UPDATE or DELETE).
 *
 * Extends RuntimeException (Unchecked) to allow it to bubble up to the
 * @ControllerAdvice handler without cluttering method signatures with `throws`.
 */
public class PatientNotFoundException extends RuntimeException {

    /**
     * Constructor that accepts an error message.
     * The message is passed to the parent RuntimeException class via super().
     * This makes it available to the GlobalExceptionHandler via ex.getMessage().
     *
     * @param message Descriptive error message detailing which ID was not found
     */
    public PatientNotFoundException(String message) {
        super(message);
    }
}
