package com.provemeright.patient_service.mapper;

import com.provemeright.patient_service.dto.PatientRequestDto;
import com.provemeright.patient_service.dto.PatientResponseDto;
import com.provemeright.patient_service.model.Patient;

import java.time.LocalDate;

/**
 * ============================================================================
 * PATIENT MAPPER - DATA TRANSFORMATION UTILITY
 * ============================================================================
 *
 * WHAT IS A MAPPER?
 * -----------------
 * A Mapper is responsible for converting (mapping) data between different
 * representations. In our case, it converts between:
 *   - PatientRequestDto  → Patient entity  (toModel: for saving to DB)
 *   - Patient entity     → PatientResponseDto (toDto: for API responses)
 *
 * WHY DO WE NEED A SEPARATE MAPPER CLASS?
 * ----------------------------------------
 * 1. Single Responsibility: The Service shouldn't know HOW to convert
 *    between DTOs and Entities — it should focus on business logic.
 *
 * 2. Reusability: The same mapping logic can be used by multiple services
 *    or different parts of the application.
 *
 * 3. Testability: Mapper methods are pure functions (static, no side effects).
 *    They can be unit-tested in isolation without mocking anything.
 *
 * 4. Centralized: If the mapping logic changes (e.g., adding a new field),
 *    you change it in ONE place instead of hunting through the codebase.
 *
 * WHY STATIC METHODS?
 * -------------------
 * The mapper has no state (no fields, no dependencies, no injections).
 * All its methods are pure functions: given the same input, they always
 * produce the same output. This is a perfect use case for static methods.
 *
 * Static methods:
 *   ✅ Don't need an instance to call (PatientMapper.toDto() works directly)
 *   ✅ Can be used as method references (PatientMapper::toDto)
 *   ✅ Thread-safe (no shared mutable state)
 *   ✅ No Spring bean management overhead
 *
 * WHY NOT USE MapStruct OR ModelMapper?
 * --------------------------------------
 * In production, you might use:
 * - MapStruct: Generates mapping code at COMPILE TIME. Zero runtime overhead.
 *   You just define an interface, and MapStruct generates the implementation.
 * - ModelMapper: Uses reflection at RUNTIME to auto-map fields by name.
 *   Convenient but slower and can have unexpected behavior.
 *
 * Manual mapping (this approach) is simpler to understand and debug,
 * which makes it perfect for learning. For large projects with many DTOs,
 * MapStruct is strongly recommended.
 *
 * DATA FLOW DIAGRAM:
 * ==================
 *
 * CREATE FLOW (POST /patients):
 * JSON → [Jackson] → PatientRequestDto → [Mapper.toModel()] → Patient → [Repository.save()] → DB
 *                                                                                               ↓
 * JSON ← [Jackson] ← PatientResponseDto ← [Mapper.toDto()] ← Patient (with generated ID) ← DB
 *
 * READ FLOW (GET /patients):
 * DB → Patient → [Mapper.toDto()] → PatientResponseDto → [Jackson] → JSON Response
 */
public class PatientMapper {

    /**
     * ENTITY → RESPONSE DTO CONVERSION
     * ---------------------------------
     * Converts a Patient entity (database representation) to a
     * PatientResponseDto (API response representation).
     *
     * WHY .toString() ON EVERY FIELD?
     * --------------------------------
     * - patient.getId().toString():
     *   UUID.toString() converts UUID to its string representation:
     *   "123e4567-e89b-12d3-a456-426614174000"
     *
     * - patient.getAddress().toString():
     *   This .toString() on a String is REDUNDANT — String.toString()
     *   returns itself. It doesn't cause errors but adds unnecessary
     *   method call overhead. In production, you'd remove this.
     *
     * - patient.getEmail().toString():
     *   Same as above — redundant on a String field.
     *
     * - patient.getName().toString():
     *   Same — redundant. Only getId() and getDateOfBirth() actually
     *   need .toString() because they convert UUID and LocalDate to String.
     *
     * - patient.getDateOfBirth().toString():
     *   LocalDate.toString() returns ISO-8601 format: "1985-06-15"
     *   This is a meaningful conversion (LocalDate → String).
     *
     * POTENTIAL NULL POINTER EXCEPTION:
     * If any field is null, calling .toString() on it will throw
     * NullPointerException. In production, you'd add null checks:
     *   patientResponseDto.setId(patient.getId() != null ? patient.getId().toString() : null);
     * Or use Optional: Optional.ofNullable(patient.getId()).map(UUID::toString).orElse(null)
     *
     * @param patient The entity object from the database
     * @return A response DTO suitable for the API response
     */
    public static PatientResponseDto toDto(Patient patient){
        PatientResponseDto patientResponseDto = new PatientResponseDto();

        // Convert UUID to String — essential conversion
        patientResponseDto.setId(patient.getId().toString());

        // These .toString() calls are redundant since getAddress/getEmail/getName
        // already return String. They work fine but add unnecessary overhead.
        patientResponseDto.setAddress(patient.getAddress().toString());
        patientResponseDto.setEmail(patient.getEmail().toString());
        patientResponseDto.setName(patient.getName().toString());

        // Convert LocalDate to String — essential conversion
        // LocalDate.toString() produces ISO-8601 format: "1985-06-15"
        patientResponseDto.setDateOfBirth(patient.getDateOfBirth().toString());

        return patientResponseDto;
    }

    /**
     * REQUEST DTO → ENTITY CONVERSION
     * --------------------------------
     * Converts a PatientRequestDto (client input) to a Patient entity
     * (database representation) for persistence.
     *
     * IMPORTANT DETAILS:
     *
     * 1. NO ID SETTING:
     *    Notice we never call patient.setId(). WHY?
     *    Because the ID is auto-generated by the database via
     *    @GeneratedValue(strategy = GenerationType.AUTO).
     *    If we set an ID, JPA might try to UPDATE an existing record
     *    instead of INSERT-ing a new one. (JPA uses the ID to determine
     *    if an entity is new or existing.)
     *
     * 2. LocalDate.parse() — STRING → DATE CONVERSION:
     *    Parses the ISO-8601 date string (e.g., "1995-09-09") into a
     *    LocalDate object. If the string format is invalid, this throws
     *    DateTimeParseException (unchecked exception).
     *
     *    EXAMPLE:
     *    "1995-09-09" → LocalDate.of(1995, 9, 9)
     *
     *    POTENTIAL ISSUE: If the client sends a malformed date like
     *    "99/99/9999" or "not-a-date", this method will crash with
     *    DateTimeParseException. In production, you'd want to:
     *    a) Catch the exception and throw a custom validation error
     *    b) Or use @DateTimeFormat annotation with @Valid
     *    c) Or add a custom validator for date fields in the DTO
     *
     * 3. FIELD MAPPING:
     *    name     → name          (String to String — direct copy)
     *    address  → Address       (Note: entity field is capitalized!)
     *    email    → email         (String to String — direct copy)
     *    dateOfBirth → dateOfBirth (String to LocalDate — parsed)
     *    registeredDate → registeredDate (String to LocalDate — parsed)
     *
     * @param patientRequestDto The validated request DTO from the controller
     * @return A new Patient entity ready to be saved to the database
     */
    public static Patient toModel(PatientRequestDto patientRequestDto){
        Patient patient = new Patient();

        // Direct String-to-String mapping — no conversion needed
        patient.setName(patientRequestDto.getName());
        patient.setAddress(patientRequestDto.getAddress());
        patient.setEmail(patientRequestDto.getEmail());

        // String-to-LocalDate conversion — parses ISO-8601 format
        // "1995-09-09" → LocalDate(1995, 9, 9)
        patient.setDateOfBirth(LocalDate.parse(patientRequestDto.getDateOfBirth()));
        patient.setRegisteredDate(LocalDate.parse(patientRequestDto.getRegisteredDate()));

        return patient;
    }
}
