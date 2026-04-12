package com.provemeright.patient_service.exception;

/**
 * ============================================================================
 * CUSTOM EXCEPTION - EmailAlreadyExistsException
 * ============================================================================
 *
 * WHAT IS A CUSTOM EXCEPTION?
 * ----------------------------
 * A custom exception is a user-defined exception class that represents a
 * SPECIFIC business rule violation. Instead of throwing generic exceptions
 * (like IllegalArgumentException), we create descriptive exception classes
 * that clearly communicate WHAT went wrong and WHY.
 *
 * WHY EXTEND RuntimeException (UNCHECKED) INSTEAD OF Exception (CHECKED)?
 * -----------------------------------------------------------------------
 * Java has two types of exceptions:
 *
 * 1. CHECKED EXCEPTIONS (extend Exception):
 *    - Compiler FORCES you to handle them (try-catch or 'throws' declaration)
 *    - Used for recoverable errors (file not found, network timeout)
 *    - Every method in the call chain must acknowledge the exception
 *    - This creates "exception pollution" — clutters method signatures
 *
 * 2. UNCHECKED EXCEPTIONS (extend RuntimeException):
 *    - Compiler does NOT force handling
 *    - Used for programming errors or business rule violations
 *    - Can propagate freely up the call stack
 *    - Our GlobalExceptionHandler catches them centrally
 *
 * WHY WE CHOSE UNCHECKED:
 * This is a BUSINESS LOGIC violation (duplicate email), not a recoverable
 * I/O error. We want it to "bubble up" through the service and controller
 * layers WITHOUT forcing every method to add 'throws' to their signatures.
 * The GlobalExceptionHandler (@ControllerAdvice) catches it and converts
 * it to a proper HTTP response — this is the centralized error handling pattern.
 *
 * EXCEPTION PROPAGATION FLOW:
 * ----------------------------
 * PatientService.createPatient()
 *   → throws EmailAlreadyExistsException("...")
 *     → bubbles through PatientController.createPatient()
 *       → caught by GlobalExceptionHandler.handleEmailAlreadyExistsException()
 *         → returns HTTP 400 Bad Request with JSON error body
 *
 * WHY NOT JUST THROW IllegalArgumentException?
 * Using a SPECIFIC custom exception (EmailAlreadyExistsException) instead
 * of a generic one (IllegalArgumentException) gives us:
 * 1. Targeted exception handling: @ExceptionHandler can match THIS exact type
 * 2. Clear semantics: The exception name tells you exactly what went wrong
 * 3. Different handling: Different business exceptions can return different
 *    HTTP status codes and error messages
 * 4. Searchability: You can grep the codebase for all places this happens
 */
public class EmailAlreadyExistsException extends RuntimeException{

    /**
     * Constructor that accepts an error message.
     * The message is passed to the parent RuntimeException class via super().
     *
     * super(message) stores the message and makes it available via:
     * - getMessage() → Returns the exact string passed here
     * - toString() → Returns "EmailAlreadyExistsException: <message>"
     * - printStackTrace() → Prints full stack trace with the message
     *
     * The GlobalExceptionHandler uses ex.getMessage() to log the error details.
     *
     * @param message Descriptive error message (e.g., "A patient with this email already exists: john@example.com")
     */
    public EmailAlreadyExistsException(String message){
        super(message);
    }
}