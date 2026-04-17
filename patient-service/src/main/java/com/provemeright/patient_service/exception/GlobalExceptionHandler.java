package com.provemeright.patient_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * GLOBAL EXCEPTION HANDLER - CENTRALIZED ERROR HANDLING
 * ============================================================================
 *
 * WHAT IS THIS CLASS?
 * -------------------
 * This class intercepts ALL exceptions thrown by any controller in the
 * application and converts them into proper HTTP responses. Without this,
 * unhandled exceptions would result in ugly 500 Internal Server Error
 * responses with stack traces (a security risk and poor user experience).
 *
 * @ControllerAdvice EXPLAINED:
 * -----------------------------
 * @ControllerAdvice is a special annotation that tells Spring:
 * "This class provides cross-cutting concerns for ALL controllers."
 *
 * It's called "Advice" because it follows the Aspect-Oriented Programming
 * (AOP) terminology — it "advises" controllers on how to handle exceptions.
 *
 * WHAT CAN @ControllerAdvice DO?
 * 1. @ExceptionHandler → Handle exceptions globally (what we use here)
 * 2. @InitBinder → Customize request parameter binding
 * 3. @ModelAttribute → Add common model attributes to all responses
 *
 * HOW THE EXCEPTION HANDLING MECHANISM WORKS:
 * ---------------------------------------------
 * 1. Controller method throws an exception (or validation fails)
 * 2. Spring's DispatcherServlet catches the exception
 * 3. DispatcherServlet checks if any @ControllerAdvice class has an
 *    @ExceptionHandler matching the exception type
 * 4. If found → that handler method is called instead of returning 500
 * 5. The handler converts the exception into a proper HTTP response
 * 6. If NOT found → default Spring error handling kicks in (generic 500)
 *
 * EXCEPTION MATCHING RULES:
 * --------------------------
 * @ExceptionHandler uses TYPE MATCHING with inheritance:
 * - @ExceptionHandler(EmailAlreadyExistsException.class) catches ONLY this type
 * - @ExceptionHandler(RuntimeException.class) catches ALL runtime exceptions
 * - @ExceptionHandler(Exception.class) catches ALL exceptions (most generic)
 * - More SPECIFIC handlers take priority over more general ones
 *
 * BEST PRACTICE: Always define specific handlers first, then add a generic
 * catch-all handler as a safety net to prevent stack traces in responses.
 *
 * WHY NOT JUST USE try-catch IN EACH CONTROLLER METHOD?
 * -------------------------------------------------------
 * 1. DRY (Don't Repeat Yourself): Without centralized handling, every
 *    controller method would need its own try-catch blocks
 * 2. Consistency: All errors follow the same format across the entire API
 * 3. Maintainability: Add/change error handling in ONE place, affects everywhere
 * 4. Separation of Concerns: Controllers focus on happy path, errors handled here
 */
@ControllerAdvice

public class GlobalExceptionHandler {

    /**
     * SLF4J LOGGER
     * -------------
     * SLF4J (Simple Logging Facade for Java) is a FACADE pattern — it provides
     * a unified API for various logging frameworks (Logback, Log4j2, etc.).
     * Spring Boot uses Logback by default.
     *
     * WHY USE A LOGGER INSTEAD OF System.out.println()?
     * 1. LOG LEVELS: TRACE < DEBUG < INFO < WARN < ERROR
     *    → You can filter by severity. In production, you might only show WARN+
     * 2. PERFORMANCE: Loggers use lazy evaluation — the message string isn't
     *    built unless that log level is enabled
     * 3. OUTPUT CONTROL: Logs can go to console, files, databases, or remote
     *    systems (ELK Stack, Splunk) — configurable without code changes
     * 4. CONTEXT: Loggers include timestamps, thread names, class names
     * 5. THREAD-SAFE: Unlike System.out, loggers are designed for concurrent use
     *
     * LoggerFactory.getLogger(GlobalExceptionHandler.class):
     * Creates a logger named after this class. The class name appears in log output:
     *   WARN c.p.p.exception.GlobalExceptionHandler - Email address already exists: john@example.com
     *
     * WHY 'static final'?
     * - static: One logger per CLASS (not per instance). Since logging is
     *   the same regardless of instance, one shared logger is sufficient.
     * - final: The logger is created once and never reassigned. Loggers are
     *   thread-safe singletons that are cached by the logging framework.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * HANDLER FOR VALIDATION ERRORS (MethodArgumentNotValidException)
     * ----------------------------------------------------------------
     * This method is called when @Valid validation fails on a controller parameter.
     *
     * WHEN IS THIS TRIGGERED?
     * When a POST request to /patients has invalid data, such as:
     *   {"name": "", "email": "not-an-email", "address": "", "dateOfBirth": ""}
     *
     * Spring validates the @Valid @RequestBody PatientRequestDto and finds:
     *   - name: @NotBlank fails → "Name is required"
     *   - email: @Email fails → "Email should be valid"
     *   - address: @NotBlank fails → "Address is required"
     *
     * ALL failures are collected into a MethodArgumentNotValidException object,
     * and THIS handler method is invoked.
     *
     * HOW WE PROCESS THE EXCEPTION:
     * 1. ex.getBindingResult() → Gets the BindingResult containing all errors
     * 2. .getFieldErrors() → Gets a list of FieldError objects
     * 3. We iterate over each FieldError and build a map:
     *    {
     *      "name": "Name is required",
     *      "email": "Email should be valid",
     *      "address": "Address is required"
     *    }
     * 4. Return as HTTP 400 (Bad Request) with the error map as the JSON body
     *
     * WHY A MAP<String, String>?
     * The key is the FIELD NAME (so the frontend knows which input field has the error)
     * The value is the ERROR MESSAGE (human-readable description)
     * This format makes it easy for frontend apps to display field-level errors
     * next to each form input.
     *
     * @param ex The exception containing all validation errors
     * @return 400 Bad Request with a map of field → error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException ex){
        // Create a map to collect all field-level validation errors
        Map<String,String> errors = new HashMap<>();

        // ex.getBindingResult().getFieldErrors() returns a List<FieldError>
        // Each FieldError contains:
        //   - getField(): The field name (e.g., "name", "email")
        //   - getDefaultMessage(): The error message from the annotation's 'message' attribute
        // Using forEach with a lambda to populate the errors map
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        // ResponseEntity.badRequest() → HTTP 400 status code
        // .body(errors) → The JSON response body is the errors map
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * HANDLER FOR DUPLICATE EMAIL (Custom Business Exception)
     * --------------------------------------------------------
     * This method is called when PatientService throws an
     * EmailAlreadyExistsException during patient creation.
     *
     * WHEN IS THIS TRIGGERED?
     * When someone tries to create a patient with an email that already
     * exists in the database. The PatientService detects this and throws
     * EmailAlreadyExistsException.
     *
     * FLOW:
     * 1. POST /patients with {"email": "john@example.com"} (already exists)
     * 2. PatientService.createPatient() checks → patientRepository.existsByEmail() returns true
     * 3. Throws: new EmailAlreadyExistsException("A patient with this email...")
     * 4. Exception bubbles through PatientController (not caught there)
     * 5. Spring's DispatcherServlet catches it
     * 6. Finds THIS @ExceptionHandler method matching the exception type
     * 7. THIS method is called → logs a warning and returns HTTP 400
     *
     * WHY log.warn() INSTEAD OF log.error()?
     * - ERROR: System is BROKEN, needs immediate attention (DB down, config error)
     * - WARN: Something unexpected happened but the system is still working
     * A duplicate email is USER ERROR, not a system error. The system correctly
     * detected and rejected the duplicate. Hence WARN, not ERROR.
     *
     * LOGGING TEMPLATE: "{}" is SLF4J's parameter placeholder.
     * log.warn("Email address already exists: {}", ex.getMessage())
     * SLF4J replaces {} with the actual message. This is MORE EFFICIENT
     * than string concatenation ("Email... " + ex.getMessage()) because:
     * - If WARN level is disabled, the string is never built
     * - No StringBuilder overhead at runtime
     * - Cleaner syntax
     *
     * @param ex The custom exception thrown by the service layer
     * @return 400 Bad Request with a JSON body: {"message": "Email already exists!"}
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){
        // Log the event at WARN level (not ERROR — this is user error, not system error)
        // The {} placeholder is replaced by ex.getMessage() at runtime by SLF4J
        log.warn("Email address already exists: {}",ex.getMessage());

        // Build a simple error response map
        Map<String,String> errors = new HashMap<>();
        errors.put("message","Email already exists!");

        // Return HTTP 400 Bad Request with the error message
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * HANDLER FOR RESOURCE NOT FOUND (PatientNotFoundException)
     * ---------------------------------------------------------
     * This method intercepts `PatientNotFoundException` when a requested patient
     * cannot be found in the database (e.g., during GET by ID, UPDATE, or DELETE).
     *
     * HTTP STATUS 404 vs 400:
     * - We return 404 Not Found (HttpStatus.NOT_FOUND) because the URL itself 
     *   identifies a resource (/patients/{id}) that does not exist. This is the 
     *   correct RESTful semantic for "I understand your request, but the thing 
     *   you want isn't here."
     * - Returning 400 Bad Request would imply the client formed the request
     *   incorrectly, which isn't the case here.
     *
     * @param ex The exception thrown when the patient is not found
     * @return 404 Not Found response containing the error message
     */
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String,String>> handlePatientNotFoundException(PatientNotFoundException ex){
        // Log the event. Still a WARN because it's a client issue (requesting bad ID)
        log.warn("Patient not found: {}", ex.getMessage());
        
        // Build the JSON error response
        Map<String,String> errors = new HashMap<>();
        // Use the exception's message to provide specific feedback
        errors.put("message", ex.getMessage() != null ? ex.getMessage() : "Patient not found");
        
        // Return HTTP 404 Not Found
        return ResponseEntity.status(404).body(errors);
    }
}
