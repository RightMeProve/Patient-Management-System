package com.provemeright.patient_service.service;

import com.provemeright.patient_service.dto.PatientRequestDto;
import com.provemeright.patient_service.dto.PatientResponseDto;
import com.provemeright.patient_service.exception.EmailAlreadyExistsException;
import com.provemeright.patient_service.mapper.PatientMapper;
import com.provemeright.patient_service.model.Patient;
import com.provemeright.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================================
 * PATIENT SERVICE - BUSINESS LOGIC LAYER
 * ============================================================================
 *
 * WHAT IS THE SERVICE LAYER?
 * --------------------------
 * The Service layer sits BETWEEN the Controller (HTTP handling) and the
 * Repository (data access). It contains all the BUSINESS LOGIC — the
 * rules, validations, and workflows that define what the application does.
 *
 * THE THREE-TIER ARCHITECTURE:
 * ----------------------------
 * ┌─────────────────┐
 * │   Controller    │  ← Handles HTTP requests/responses (thin layer)
 * ├─────────────────┤
 * │    Service      │  ← Contains business logic (THIS CLASS - thick layer)
 * ├─────────────────┤
 * │   Repository    │  ← Handles database operations (data access layer)
 * └─────────────────┘
 *
 * WHY SEPARATE SERVICE FROM CONTROLLER?
 * --------------------------------------
 * 1. Single Responsibility Principle (SRP):
 *    - Controller should ONLY handle HTTP-specific logic (parsing requests,
 *      sending responses, HTTP status codes)
 *    - Service handles business rules (email uniqueness, data transformations)
 *
 * 2. Reusability:
 *    - The same service method can be called from a REST controller,
 *      a gRPC server, a Kafka consumer, or a scheduled job
 *    - If business logic was in the controller, you'd have to duplicate
 *      it for each entry point
 *
 * 3. Testability:
 *    - Service can be unit-tested without starting an HTTP server
 *    - Mock the repository, test the business logic in isolation
 *
 * 4. Future-proofing:
 *    - When we add gRPC (Section 28-29) and Kafka (Section 34-35),
 *      they will call this SAME service layer
 *
 * @Service ANNOTATION EXPLAINED:
 * ------------------------------
 * @Service is a specialization of @Component. Functionally, it does the
 * same thing as @Component — it tells Spring to create a singleton bean
 * of this class and manage its lifecycle.
 *
 * WHY @Service INSTEAD OF @Component?
 * It's semantically meaningful — it tells developers "this class contains
 * business logic." Spring also applies different exception translation
 * for different stereotypes (@Repository gets DataAccessException translation).
 *
 * Spring Stereotype Annotations:
 *   @Component   → Generic Spring-managed bean
 *   @Service     → Business logic layer bean
 *   @Repository  → Data access layer bean
 *   @Controller  → Spring MVC web controller
 *   @RestController → @Controller + @ResponseBody (REST API controller)
 */
@Service
public class PatientService {

    /**
     * DEPENDENCY INJECTION - THE CORE OF SPRING
     * ------------------------------------------
     * Instead of creating PatientRepository ourselves with 'new PatientRepository()',
     * we declare it as a field and let Spring "inject" (provide) it automatically.
     *
     * WHY? This is called Inversion of Control (IoC):
     * - WITHOUT DI: PatientService creates its own dependencies → tightly coupled
     * - WITH DI: Spring provides dependencies → loosely coupled, testable, flexible
     *
     * In tests, we can inject a MOCK repository instead of a real one,
     * so our tests don't need a real database.
     */
    private PatientRepository patientRepository;

    /**
     * CONSTRUCTOR INJECTION (PREFERRED OVER FIELD INJECTION)
     * -------------------------------------------------------
     * Spring uses this constructor to inject the PatientRepository bean.
     *
     * THREE WAYS TO DO DEPENDENCY INJECTION IN SPRING:
     *
     * 1. CONSTRUCTOR INJECTION (used here — RECOMMENDED):
     *    public PatientService(PatientRepository repo) { this.repo = repo; }
     *    ✅ Dependencies are required (enforced at compile time)
     *    ✅ Immutable (can use 'final' keyword)
     *    ✅ Easy to test (just pass mocks in constructor)
     *    ✅ Fails fast if dependency is missing
     *
     * 2. FIELD INJECTION (using @Autowired on the field):
     *    @Autowired private PatientRepository repo;
     *    ❌ Hides dependencies (not visible in constructor)
     *    ❌ Cannot use 'final' (Spring sets it after construction)
     *    ❌ Harder to test (need reflection to inject mocks)
     *    ❌ Spring team officially discourages this approach
     *
     * 3. SETTER INJECTION:
     *    @Autowired public void setRepo(PatientRepository repo) { ... }
     *    ⚠️ Good for optional dependencies only
     *    ❌ Object can be in a partially-initialized state
     *
     * NOTE: When a class has a SINGLE constructor, Spring Boot automatically
     * uses it for injection — no @Autowired annotation needed. This implicit
     * behavior was added in Spring 4.3. With multiple constructors, you'd
     * need @Autowired to tell Spring which one to use.
     *
     * @param patientRepository The JPA repository bean, auto-created by Spring Data
     */
    public PatientService(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }

    /**
     * GET ALL PATIENTS
     * ----------------
     * Retrieves all patients from the database and converts them to DTOs.
     *
     * FLOW:
     * 1. patientRepository.findAll() → Executes "SELECT * FROM patient"
     *    Returns a List<Patient> (entity objects populated from DB rows)
     *
     * 2. .stream() → Converts the List into a Java Stream for functional processing
     *    WHY STREAMS? They allow declarative, pipeline-style transformations:
     *    - More readable than for-loops
     *    - Can be parallelized with .parallelStream() if needed
     *    - Lazy evaluation (though .toList() forces eager evaluation here)
     *
     * 3. .map(PatientMapper::toDto) → Transforms each Patient entity into
     *    a PatientResponseDto using the mapper's static method.
     *    '::' is a METHOD REFERENCE — shorthand for lambda: (p) -> PatientMapper.toDto(p)
     *    WHY? The controller should never return entity objects directly.
     *    The DTO controls exactly what data the API consumer sees.
     *
     * 4. .toList() → Collects the stream back into a List<PatientResponseDto>
     *    NOTE: .toList() (Java 16+) returns an UNMODIFIABLE list.
     *    If you need a modifiable list, use .collect(Collectors.toList()) instead.
     *
     * @return List of all patients as response DTOs
     */
    public List<PatientResponseDto> getPatients(){
        List<Patient> patients = patientRepository.findAll();

        return patients.stream()
                .map(PatientMapper::toDto).toList();
    }

    /**
     * CREATE A NEW PATIENT
     * --------------------
     * Validates business rules, saves the patient to the database,
     * and returns the saved patient as a DTO.
     *
     * BUSINESS LOGIC FLOW:
     * 1. CHECK EMAIL UNIQUENESS (Business Rule):
     *    - Queries the DB to check if any existing patient has this email
     *    - If yes → throws EmailAlreadyExistsException (custom exception)
     *    - WHY HERE AND NOT IN CONTROLLER? Email uniqueness is a BUSINESS RULE,
     *      not an HTTP concern. The controller shouldn't know about business rules.
     *    - WHY NOT RELY ON DB UNIQUE CONSTRAINT ALONE? Because:
     *      a) DB constraint throws an ugly SQL exception (not user-friendly)
     *      b) We want to throw a custom exception with a clean error message
     *      c) The GlobalExceptionHandler catches our custom exception and
     *         returns a proper JSON error response
     *
     * 2. MAP DTO → ENTITY:
     *    - Converts the incoming PatientRequestDto to a Patient entity
     *    - WHY? The repository.save() method expects an Entity, not a DTO
     *    - The mapper handles type conversions (String dates → LocalDate)
     *
     * 3. SAVE TO DATABASE:
     *    - patientRepository.save() executes an INSERT statement
     *    - Hibernate auto-generates the UUID primary key
     *    - The save() method returns the PERSISTED entity (with generated ID)
     *    - WHY SAVE RETURNS THE ENTITY? Because the DB may modify the entity
     *      (auto-generated ID, auto-set timestamps, DB defaults, triggers)
     *
     * 4. MAP ENTITY → RESPONSE DTO:
     *    - Converts the saved entity (with generated ID) back to a DTO
     *    - Returns it to the controller for the HTTP response
     *
     * EXCEPTION HANDLING:
     * - EmailAlreadyExistsException is an UNCHECKED exception (extends RuntimeException)
     * - It "bubbles up" through the controller to the GlobalExceptionHandler
     * - The @ExceptionHandler in GlobalExceptionHandler catches it and returns
     *   an HTTP 400 Bad Request with a JSON error body
     *
     * @param patientRequestDto The validated request data from the controller
     * @return The created patient as a response DTO (includes generated ID)
     * @throws EmailAlreadyExistsException if a patient with this email already exists
     */
    public PatientResponseDto createPatient(PatientRequestDto patientRequestDto){
        // Step 1: Business rule validation — check for duplicate email
        if(patientRepository.existsByEmail(patientRequestDto.getEmail()))
        {
            throw new EmailAlreadyExistsException("A patient with this email already exists: " +
                    patientRequestDto.getEmail());
        }

        // Step 2 & 3: Convert DTO → Entity and persist to database
        // PatientMapper.toModel() handles the conversion from String dates to LocalDate
        // save() generates the ID and returns the managed (persisted) entity
        Patient newPatient = patientRepository.save(
                PatientMapper.toModel(patientRequestDto)
        );

        // Step 4: Convert the persisted entity back to a response DTO
        // This includes the auto-generated UUID that the client needs
        return PatientMapper.toDto(newPatient);

    }
}
