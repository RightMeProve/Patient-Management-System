package com.provemeright.patient_service.controller;

import com.provemeright.patient_service.dto.PatientRequestDto;
import com.provemeright.patient_service.dto.PatientResponseDto;
import com.provemeright.patient_service.dto.validators.CreatePatientValidationGroup;
import com.provemeright.patient_service.service.PatientService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * PATIENT CONTROLLER - REST API ENDPOINT LAYER
 * ============================================================================
 *
 * WHAT IS A CONTROLLER?
 * ---------------------
 * The Controller is the ENTRY POINT for all HTTP requests. When a client
 * (browser, Postman, frontend app) sends an HTTP request to our application,
 * Spring's DispatcherServlet routes it to the appropriate controller method.
 *
 * CONTROLLER'S RESPONSIBILITY (SHOULD BE THIN):
 * -----------------------------------------------
 * 1. Receive the HTTP request (parse body, headers, path variables)
 * 2. Delegate to the Service layer for business logic
 * 3. Return the HTTP response (status code, body, headers)
 *
 * WHAT THE CONTROLLER SHOULD NOT DO:
 *   ❌ Database queries (that's the Repository's job)
 *   ❌ Business logic (that's the Service's job)
 *   ❌ Data transformation (that's the Mapper's job)
 *   ❌ Complex validation (basic validation via @Valid is fine)
 *
 * HTTP REQUEST LIFECYCLE (HOW A REQUEST REACHES THIS METHOD):
 * -----------------------------------------------------------
 * 1. Client sends: GET http://localhost:4000/patients
 * 2. Tomcat (embedded server) receives the raw HTTP request
 * 3. DispatcherServlet (Spring's front controller) catches it
 * 4. HandlerMapping finds which controller method handles this URL
 * 5. Argument resolvers parse request body, headers, params
 * 6. Validation runs (@Valid triggers Bean Validation API)
 * 7. Controller method executes
 * 8. ReturnValueHandler converts the response object to JSON
 * 9. HTTP response is sent back to the client
 *
 * @RestController ANNOTATION:
 * ----------------------------
 * This is a meta-annotation combining:
 *   @Controller → Marks this as a Spring MVC controller (handles HTTP requests)
 *   @ResponseBody → Tells Spring to serialize return values directly to the
 *                    HTTP response body (as JSON), instead of resolving a view
 *                    template (like Thymeleaf/JSP).
 *
 * WITHOUT @ResponseBody: Spring would try to find a View (HTML template)
 * matching the return value. WITH @ResponseBody (or @RestController):
 * Spring uses Jackson (JSON library) to convert the Java object to JSON.
 *
 * @RequestMapping("/patients"):
 * ------------------------------
 * Sets the BASE URL path for ALL endpoint methods in this controller.
 * Every method's URL will be prefixed with '/patients'.
 * So @GetMapping maps to GET /patients, @PostMapping maps to POST /patients.
 *
 * REST API DESIGN CONVENTIONS:
 * - Use plural nouns for resources: /patients (not /patient)
 * - Use HTTP verbs for actions: GET (read), POST (create), PUT (update), DELETE (delete)
 * - Use path variables for specific resources: /patients/{id}
 * - Use query parameters for filtering: /patients?status=active
 */
@RestController
@RequestMapping("/patients")
public class PatientController {

    /**
     * Service dependency — injected via constructor.
     * The controller delegates ALL business logic to the service layer.
     */
    private PatientService patientService;

    /**
     * CONSTRUCTOR INJECTION
     * ---------------------
     * Spring automatically injects the PatientService bean.
     * Since this is the ONLY constructor, @Autowired is implicit (Spring 4.3+).
     *
     * WHY NOT @Autowired ON FIELD DIRECTLY?
     * Constructor injection makes dependencies explicit, immutable, and testable.
     * See PatientService.java for a detailed comparison of injection types.
     *
     * @param patientService The service bean containing business logic
     */
    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    /**
     * GET ALL PATIENTS
     * ----------------
     * HTTP Method: GET
     * URL: /patients
     * Response: 200 OK with JSON array of all patients
     *
     * @GetMapping EXPLAINED:
     * Shorthand for @RequestMapping(method = RequestMethod.GET).
     * Binds this method to HTTP GET requests on the base URL (/patients).
     *
     * RETURN TYPE: ResponseEntity<List<PatientResponseDto>>
     * ----------------------------------------------------
     * ResponseEntity is Spring's wrapper for an entire HTTP response. It allows
     * you to control:
     *   - Status code (200, 201, 404, 500, etc.)
     *   - Response body (the JSON payload)
     *   - Response headers (Content-Type, Cache-Control, etc.)
     *
     * WHY ResponseEntity INSTEAD OF JUST RETURNING THE LIST?
     * You COULD return List<PatientResponseDto> directly, and Spring would auto-wrap
     * it in a 200 OK response. But ResponseEntity gives you explicit control:
     *   - ResponseEntity.ok()          → 200 OK
     *   - ResponseEntity.created(uri)  → 201 Created (for POST endpoints)
     *   - ResponseEntity.notFound()    → 404 Not Found
     *   - ResponseEntity.noContent()   → 204 No Content (for DELETE endpoints)
     *
     * This makes the code self-documenting — you can see the HTTP status
     * code right in the Java code rather than relying on implicit defaults.
     *
     * JSON SERIALIZATION:
     * -------------------
     * When this method returns, Spring uses Jackson (ObjectMapper) to convert
     * List<PatientResponseDto> into a JSON array:
     * [
     *   {"id": "uuid-1", "name": "John", "email": "john@example.com", ...},
     *   {"id": "uuid-2", "name": "Jane", "email": "jane@example.com", ...}
     * ]
     *
     * Jackson converts each field to a JSON key using the getter method names:
     * getName() → "name", getEmail() → "email", etc.
     *
     * @return ResponseEntity containing a list of all patients as DTOs
     */
    @GetMapping
    public ResponseEntity<List<PatientResponseDto>> getPatients(){
        // Delegate to service layer — controller stays thin
        List<PatientResponseDto> patients = patientService.getPatients();
        // .ok() sets status 200, .body(patients) sets the response body
        return ResponseEntity.ok().body(patients);
    }

    /**
     * CREATE A NEW PATIENT
     * --------------------
     * HTTP Method: POST
     * URL: /patients
     * Request Body: JSON matching PatientRequestDto
     * Response: 200 OK with the created patient (including generated ID)
     *
     * @PostMapping EXPLAINED:
     * Shorthand for @RequestMapping(method = RequestMethod.POST).
     * POST is used for CREATING new resources (REST convention).
     *
     * @Valid ANNOTATION - INPUT VALIDATION:
     * --------------------------------------
     * This is the TRIGGER for Jakarta Bean Validation. Without @Valid, the
     * validation annotations (@NotBlank, @Email, @Size) on PatientRequestDto
     * would be COMPLETELY IGNORED.
     *
     * HOW IT WORKS:
     * 1. Spring receives the HTTP request body (JSON)
     * 2. Jackson deserializes the JSON into a PatientRequestDto object
     * 3. @Valid triggers the Bean Validation API to check ALL constraints
     *    on PatientRequestDto (@NotBlank, @Email, @Size, etc.)
     * 4a. If validation PASSES → method executes normally
     * 4b. If validation FAILS → Spring throws MethodArgumentNotValidException
     *     BEFORE this method even executes. The exception contains ALL
     *     validation errors. Our GlobalExceptionHandler catches this and
     *     returns a 400 Bad Request with error details.
     *
     * WHY VALIDATE AT THE CONTROLLER LEVEL?
     * This is the FIRST LINE OF DEFENSE — reject bad input before it reaches
     * the service or database layer. "Fail fast" principle — don't waste
     * processing time on invalid data.
     *
     * @RequestBody ANNOTATION:
     * -------------------------
     * Tells Spring to deserialize the HTTP request body (JSON) into a Java
     * object. Spring uses Jackson's ObjectMapper to:
     *   1. Read the raw JSON string from the request body
     *   2. Create a new PatientRequestDto instance
     *   3. Use setter methods to populate fields from JSON keys
     *
     * Example JSON input:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "address": "123 Main St",
     *   "dateOfBirth": "1995-09-09",
     *   "registeredDate": "2024-11-28"
     * }
     *
     * NOTE ON HTTP STATUS CODE:
     * Using ResponseEntity.ok() returns 200. In strict REST API design,
     * a resource creation should return 201 CREATED with a Location header
     * pointing to the new resource:
     *   return ResponseEntity.created(URI.create("/patients/" + id)).body(dto);
     * This will be refined in later sections.
     *
     * @param patientRequestDto The validated request body deserialized from JSON
     * @return ResponseEntity containing the created patient with generated ID
     */
    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(
            @Validated({Default.class, CreatePatientValidationGroup.class})
            @RequestBody PatientRequestDto patientRequestDto){
        // Delegate to service — the service handles business logic (email uniqueness check)
        PatientResponseDto patientResponseDto = patientService.createPatient(patientRequestDto);
        // Return the created patient (includes the auto-generated UUID)
        return ResponseEntity.ok().body(patientResponseDto);
    }

    /**
     * UPDATE AN EXISTING PATIENT
     * --------------------------
     * HTTP Method: PUT
     * URL: /patients/{id}
     * Request Body: JSON matching PatientRequestDto
     * Response: 200 OK with the updated patient DTO
     *
     * @PutMapping("/{id}"):
     * Maps HTTP PUT requests to this method. The "{id}" part is a URI TEMPLATE
     * VARIABLE. It tells Spring to capture whatever value is in that part of the
     * URL (e.g., /patients/123e4567...) and pass it to the method.
     *
     * @PathVariable:
     * Binds the "{id}" from the URL strictly to the UUID id parameter here.
     * Spring automatically attempts to convert the string in the URL to a Java UUID.
     * If the string isn't a valid UUID format, Spring throws a TypeMismatchException.
     *
     * WHY PUT AND NOT PATCH?
     * - PUT: Replaces the ENTIRE resource. The client must send the complete representation.
     * - PATCH: Partially updates the resource. The client sends only the fields to change.
     * Here, our DTO enforces @NotBlank on all fields, so we expect a full representation,
     * making PUT the correct semantic choice.
     *
     * @param id The UUID of the patient to update, extracted from the URL
     * @param patientRequestDto The validated JSON request body containing the new data
     * @return ResponseEntity with 200 OK and the updated patient data
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @PathVariable UUID id,
            @Validated({Default.class}) @RequestBody PatientRequestDto patientRequestDto){
        
        // Delegate all update logic (fetching, validation, saving) to the service
        PatientResponseDto patientResponseDto = patientService.updatePatient(id, patientRequestDto);
        return ResponseEntity.ok().body(patientResponseDto);
    }

    /**
     * DELETE A PATIENT
     * ----------------
     * HTTP Method: DELETE
     * URL: /patients/{id}
     * Response: 204 No Content
     *
     * @DeleteMapping("/{id}"):
     * Maps HTTP DELETE requests for a specific patient ID to this method.
     *
     * WHY RETURN 204 NO CONTENT?
     * When a deletion is successful, we don't have a resource to return to the
     * client anymore (because it was just deleted!). 
     * 204 No Content is the standard REST convention for a successful operation 
     * that intentionally returns nothing in the response body.
     * 
     * .build() creates a ResponseEntity with no body and the specified status.
     *
     * @param id The UUID of the patient to delete
     * @return ResponseEntity with 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        // Delegate deletion logic to the service
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }





}
