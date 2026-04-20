package com.provemeright.patient_service.service;

import com.provemeright.patient_service.dto.PatientRequestDto;
import com.provemeright.patient_service.dto.PatientResponseDto;
import com.provemeright.patient_service.exception.EmailAlreadyExistsException;
import com.provemeright.patient_service.exception.PatientNotFoundException;
import com.provemeright.patient_service.grpc.BillingServiceGrpcClient;
import com.provemeright.patient_service.kafka.KafkaProducer;
import com.provemeright.patient_service.mapper.PatientMapper;
import com.provemeright.patient_service.model.Patient;
import com.provemeright.patient_service.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
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
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

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
    public PatientService(PatientRepository patientRepository,BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer){
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient =billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
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

        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());
        // Step 4: Convert the persisted entity back to a response DTO
        // This includes the auto-generated UUID that the client needs

        kafkaProducer.sendEvent(newPatient);
        return PatientMapper.toDto(newPatient);

    }


    /**
     * UPDATE AN EXISTING PATIENT
     * --------------------------
     * Updates an existing patient's details. Enforces business rules like
     * ensuring the new email address isn't already taken by ANOTHER patient.
     *
     * BUSINESS LOGIC FLOW:
     * 1. FIND EXISTING: Try to fetch the patient from the DB by ID.
     *    - If not found, throw our custom PatientNotFoundException.
     *    - `findById` returns an Optional<Patient>. We use `.orElseThrow()` to
     *      elegantly unwrap the Optional or throw an exception in one line.
     *
     * 2. CHECK EMAIL UNIQUENESS (Excluding Self):
     *    - When updating, a patient might keep their existing email, or change
     *      it to a new one. 
     *    - If they keep it, `existsByEmail(email)` would return true (because
     *      they own it!), which is wrong! 
     *    - We MUST check if the email exists AND belongs to a DIFFERENT patient ID.
     *      Hence, we use the derived query: `existsByEmailAndIdNot(email, id)`.
     *
     * 3. UPDATE ENTITY FIELDS:
     *    - We update the persistent entity object fetched in step 1.
     *    - Note: Because this entity is "managed" by Hibernate (it's attached 
     *      to the current persistence context), any changes to its setters will
     *      technically be flushed to the database automatically at the end of
     *      the transaction (called "Dirty Checking").
     *
     * 4. EXPLICIT SAVE (Optional but good practice):
     *    - Even with Dirty Checking, calling `save()` makes the code's intent
     *      explicit and returns the updated entity reference.
     *
     * @param id The ID of the patient to update
     * @param patientRequestDto The new data for the patient
     * @return The updated patient mapped as a Response DTO
     * @throws PatientNotFoundException if the ID doesn't exist in the database
     * @throws EmailAlreadyExistsException if the new email belongs to another patient
     */
    public PatientResponseDto updatePatient(UUID id, PatientRequestDto patientRequestDto) {
        
        // Step 1: Find the patient
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with id: " + id)
        );

        // Step 2: Ensure email is unique across OTHER patients
        if(patientRepository.existsByEmailAndIdNot(patientRequestDto.getEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email " + patientRequestDto.getEmail() + " already exists!"
            );
        }

        // Step 3: Update fields
        patient.setName(patientRequestDto.getName());
        patient.setEmail(patientRequestDto.getEmail());
        patient.setAddress(patientRequestDto.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDto.getDateOfBirth()));

        // Step 4: Persist and map
        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDto(updatedPatient);
    }

    /**
     * DELETE A PATIENT
     * ----------------
     * Deletes a patient from the database by their unique ID.
     *
     * HOW `deleteById` WORKS:
     * 1. Spring Data JPA typically performs a SELECT query first to ensure the
     *    entity exists.
     * 2. If it exists, it performs the DELETE query.
     * 3. If it does not exist, it historically threw an EmptyResultDataAccessException
     *    (though in newer Spring Boot 3 / Hibernate 6, it may just silently do nothing).
     *
     * FOR PRODUCTION CONSIDERATIONS (Soft Delete):
     * In real healthcare systems, you rarely actually DELETE records from a
     * database (Hard Delete) due to audit and compliance reasons (like HIPAA). 
     * Instead, you would use a "Soft Delete":
     *   - Add a `boolean deleted = false` flag to the entity.
     *   - Set it to true instead of calling `deleteById`.
     *   - Filter out `deleted = true` in all your `SELECT` queries.
     *
     * @param id The UUID of the patient to delete
     */
    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }

}
