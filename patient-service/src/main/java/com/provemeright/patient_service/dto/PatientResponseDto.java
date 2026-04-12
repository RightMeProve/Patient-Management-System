package com.provemeright.patient_service.dto;

/**
 * ============================================================================
 * PATIENT RESPONSE DTO - DATA TRANSFER OBJECT FOR OUTGOING RESPONSES
 * ============================================================================
 *
 * WHY THIS IS DIFFERENT FROM PatientRequestDto:
 * -----------------------------------------------
 * This DTO controls what the API RETURNS to the client. Notice the differences:
 *
 * 1. HAS 'id' FIELD:
 *    - PatientRequestDto does NOT have 'id' (client can't set their own ID)
 *    - PatientResponseDto HAS 'id' (client needs it for GET/UPDATE/DELETE)
 *
 * 2. NO VALIDATION ANNOTATIONS:
 *    - We don't need @NotBlank/@Email here because this data comes FROM our
 *      database — it was already validated when it was created. We trust our
 *      own data.
 *
 * 3. NO registeredDate:
 *    - This field is intentionally excluded from the response. The API
 *      consumer doesn't need to see the registration date in the response.
 *      This is a great example of how DTOs let you control the API shape
 *      independently of the database schema.
 *
 * 4. ALL FIELDS ARE STRINGS:
 *    - Even 'id' (which is UUID internally) and 'dateOfBirth' (which is
 *      LocalDate internally) are Strings here. WHY? Because:
 *      a) JSON is text-based — everything becomes a string eventually
 *      b) Using String gives us full control over formatting
 *      c) Avoids Jackson serialization issues with complex types
 *      d) The UUID is converted to "123e4567-e89b-..." string in the Mapper
 *
 * WHY NOT JUST RETURN THE ENTITY?
 * --------------------------------
 * If we returned the Patient entity directly:
 *   ❌ The 'Address' field's capital 'A' would leak into the JSON as "Address"
 *   ❌ registeredDate would be exposed (we might not want that)
 *   ❌ The id would be a UUID object, not a clean string
 *   ❌ Dates would be serialized as objects: {"year":1985,"month":"JUNE",...}
 *   ❌ Any future DB column would auto-appear in the API
 *   ❌ JPA lazy-loading proxies could cause serialization issues
 *
 * DATA FLOW VISUALIZATION:
 * -------------------------
 * DB Row → Patient Entity → PatientMapper.toDto() → PatientResponseDto → JSON Response
 *           ↑                                                              ↑
 *     Internal format                                               Client-facing format
 *     (UUID, LocalDate)                                             (String, String)
 */
public class PatientResponseDto {

    /**
     * The unique identifier of the patient.
     * This is the UUID from the database, converted to a String by the mapper.
     * The client uses this ID for subsequent operations (GET by ID, UPDATE, DELETE).
     *
     * WHY STRING AND NOT UUID?
     * In JSON, UUIDs are represented as strings anyway ("123e4567-e89b-...").
     * Using String here means Jackson doesn't need special UUID serialization
     * and the client sees a clean, consistent format.
     */
    private String id;

    /** The patient's full name */
    private String name;

    /** The patient's email address */
    private String email;

    /** The patient's physical address */
    private String address;

    /**
     * The patient's date of birth as a string (format: "yyyy-MM-dd").
     * Converted from LocalDate by the mapper using toString(),
     * which produces ISO-8601 format by default.
     */
    private String dateOfBirth;

    // ========================================================================
    // GETTERS AND SETTERS
    // ========================================================================
    // Required by Jackson for JSON SERIALIZATION (converting this object to JSON).
    //
    // JACKSON SERIALIZATION PROCESS (how this becomes JSON):
    // 1. Controller returns ResponseEntity.ok().body(patientResponseDto)
    // 2. Spring's HttpMessageConverter checks the Accept header (application/json)
    // 3. Jackson's ObjectMapper.writeValueAsString() is called
    // 4. Jackson uses GETTERS to discover fields:
    //    getId() → JSON key "id", getName() → JSON key "name", etc.
    // 5. Result JSON: {"id":"uuid","name":"John","email":"john@example.com",...}
    //
    // NOTE: Jackson uses getter method names to determine JSON keys.
    // If you renamed getName() to fetchName(), the JSON key would be "fetchName"!
    // To override, use @JsonProperty("custom_name") on the getter/field.
    // ========================================================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

}
