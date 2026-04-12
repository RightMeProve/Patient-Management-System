package com.provemeright.patient_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ============================================================================
 * PATIENT REQUEST DTO - DATA TRANSFER OBJECT FOR INCOMING REQUESTS
 * ============================================================================
 *
 * WHAT IS A DTO (DATA TRANSFER OBJECT)?
 * ---------------------------------------
 * A DTO is a simple object that carries data between processes. In a REST API,
 * a Request DTO defines the SHAPE of data the API accepts from the client.
 *
 * WHY DO WE NEED A SEPARATE DTO FROM THE ENTITY?
 * ------------------------------------------------
 * 1. DECOUPLING: API contract is independent of database schema.
 *    If we add a DB column, the API won't break.
 *    If we change the API format, the DB won't need migration.
 *
 * 2. SECURITY: The entity has fields like 'id' and 'registeredDate' that
 *    the client should NOT control. The DTO only exposes what we allow.
 *    Without DTOs, a malicious client could set their own ID!
 *
 * 3. TYPE DIFFERENCES: Notice how dateOfBirth is String here but LocalDate
 *    in the entity. The DTO accepts strings from JSON, and the Mapper
 *    converts them to proper types. This gives us control over parsing.
 *
 * 4. VALIDATION: Request validation annotations belong on the DTO, not the
 *    entity. @NotBlank makes sense for API input; @NotNull makes sense for
 *    database constraints. Different contexts, different rules.
 *
 * REQUEST vs RESPONSE DTO:
 * -------------------------
 * - PatientRequestDto: What the client SENDS to create/update a patient
 *   → Has validation annotations (@NotBlank, @Email, @Size)
 *   → Does NOT have 'id' field (the server generates it)
 *   → Dates are Strings (client sends ISO format strings)
 *
 * - PatientResponseDto: What the server RETURNS to the client
 *   → No validation annotations (we trust our own data)
 *   → HAS 'id' field (client needs it for subsequent requests)
 *   → All fields are Strings (serialized for display)
 *
 * JAKARTA BEAN VALIDATION (JSR 380):
 * ------------------------------------
 * The validation annotations come from the Jakarta Bean Validation spec.
 * They define declarative constraints that are checked when @Valid is
 * triggered in the controller. The validation framework iterates over
 * all annotated fields and reports ALL violations at once.
 *
 * WHY DECLARATIVE (ANNOTATIONS) INSTEAD OF IMPERATIVE (if-else)?
 *   - Less code: One annotation replaces 5+ lines of if/else validation
 *   - Consistent: Every developer validates the same way
 *   - Framework-integrated: Spring automatically validates @Valid parameters
 *   - Composable: You can create custom annotations combining multiple rules
 *   - Self-documenting: Annotations serve as documentation for the field
 */
public class PatientRequestDto {

    /**
     * PATIENT NAME - With @NotBlank and @Size Validation
     *
     * @NotBlank: Checks three things simultaneously:
     *   1. NOT null
     *   2. NOT empty string ("")
     *   3. NOT just whitespace ("   ")
     *
     * WHY @NotBlank INSTEAD OF @NotNull?
     * @NotNull only checks for null. A client could send {"name": ""} or
     * {"name": "   "} and @NotNull would pass! @NotBlank catches these edge
     * cases, which is critical for user-facing input.
     *
     * @Size(max = 100): Sets a maximum length constraint.
     * WHY? Prevents:
     *   1. Database overflow (VARCHAR columns have size limits)
     *   2. Memory attacks (someone sending a 1GB name field)
     *   3. Display issues in the UI
     *
     * 'message' parameter: The error message returned to the client when
     * validation fails. Spring collects ALL validation errors and returns
     * them as a map: {"name": "Name cannot exceed 100 characters"}.
     * Without custom messages, the framework returns generic ones.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 100,message = "Name cannot exceed 100 characters")
    private String name;

    /**
     * PATIENT EMAIL - With @NotBlank and @Email Validation
     *
     * @Email: Validates that the string follows a basic email format.
     * The default regex pattern checks for: localpart@domain
     *
     * IMPORTANT CAVEAT:
     * @Email considers "" (empty string) as VALID! That's why we also need
     * @NotBlank — it catches empty/null values that @Email would let through.
     * The validators are applied in order, and ALL failures are reported.
     *
     * IN PRODUCTION, you might use a stricter regex:
     * @Email(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "...")
     * or even better, send a verification email to confirm the address.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * PATIENT ADDRESS
     *
     * Uses @NotBlank because an address is required input.
     * No @Size constraint here, but in production you might add one
     * to prevent extremely long addresses.
     */
    @NotBlank(message = "Address is required")
    private String address;

    /**
     * DATE OF BIRTH - Stored as String in DTO
     *
     * WHY STRING INSTEAD OF LocalDate?
     * ---------------------------------
     * 1. JSON doesn't have a native date type — dates come as strings
     * 2. This gives us control over parsing and error handling in the Mapper
     * 3. If the client sends an invalid date format, our mapper can throw
     *    a meaningful exception instead of a generic Jackson deserialization error
     *
     * EXPECTED FORMAT: ISO 8601 date string, e.g., "1995-09-09" (yyyy-MM-dd)
     * The PatientMapper.toModel() method parses this using LocalDate.parse()
     *
     * NOTE: Using @NotBlank here because String-typed dates should not be
     * blank. If this field were typed as LocalDate, we'd use @NotNull instead
     * since @NotBlank only applies to CharSequence types.
     */
    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    /**
     * REGISTERED DATE - When the patient was registered
     *
     * Uses @NotNull instead of @NotBlank. This is slightly inconsistent with
     * dateOfBirth (which uses @NotBlank). Both are Strings, so @NotBlank
     * would be more appropriate here too for consistency.
     *
     * @NotNull accepts "" (empty string) as valid — this could be a subtle bug.
     * In production, this should arguably also be @NotBlank for consistency.
     *
     * EXPECTED FORMAT: ISO 8601 date string, e.g., "2024-11-28"
     */
    @NotNull(message = "Registered date is required")
    private String registeredDate;

    // ========================================================================
    // GETTERS AND SETTERS
    // ========================================================================
    // These are required for Jackson JSON deserialization and Bean Validation.
    //
    // JACKSON DESERIALIZATION PROCESS (@RequestBody):
    // 1. Jackson creates a new PatientRequestDto using the no-arg constructor
    //    (Java provides a default no-arg constructor since we didn't define any)
    // 2. Jackson reads each JSON key-value pair
    // 3. For each key (e.g., "name"), Jackson calls the setter: setName("value")
    // 4. Result: A fully populated PatientRequestDto object
    //
    // BEAN VALIDATION PROCESS (@Valid):
    // 1. After Jackson populates the object, the Validator reads the annotations
    // 2. For each field, it invokes the GETTER to read the value
    // 3. Each value is checked against its constraint annotation(s)
    // 4. All violations are collected into a list and thrown as one exception
    // ========================================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
    }


}
