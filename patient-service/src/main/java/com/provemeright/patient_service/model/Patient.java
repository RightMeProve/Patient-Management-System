package com.provemeright.patient_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * ============================================================================
 * PATIENT ENTITY - THE CORE DOMAIN MODEL
 * ============================================================================
 *
 * WHAT IS AN ENTITY?
 * ------------------
 * An Entity is a Java class that maps directly to a DATABASE TABLE. Each
 * instance of this class represents ONE ROW in the 'patient' table. Each
 * field in this class maps to a COLUMN in that table.
 *
 * WHY DO WE NEED A MODEL/ENTITY?
 * -------------------------------
 * In any application, we need a way to represent real-world "things" in code.
 * A Patient is a real-world concept — they have a name, email, address, etc.
 * The Entity class is the Java representation of that concept, AND it tells
 * the ORM (Hibernate/JPA) how to store it in the database.
 *
 * ENTITY vs DTO - KEY DIFFERENCE:
 * --------------------------------
 * - Entity (this class): Represents how data is STORED in the database.
 *   Contains database-specific annotations (@Id, @Column, @GeneratedValue).
 *   Should NEVER be directly exposed to API consumers.
 *
 * - DTO (Data Transfer Object): Represents how data is TRANSFERRED over the
 *   network (API requests/responses). Contains validation annotations for input
 *   and formatted data for output.
 *
 * WHY NOT EXPOSE ENTITY DIRECTLY?
 *   1. Security: Entity might have fields you don't want to expose (e.g., passwords)
 *   2. Decoupling: If DB schema changes, your API contract shouldn't break
 *   3. Flexibility: Response can have computed/formatted fields not in the DB
 *   4. Validation: Request validation rules differ from DB constraints
 *
 * JPA (Java Persistence API) EXPLAINED:
 * --------------------------------------
 * JPA is a SPECIFICATION (not an implementation) that defines how Java objects
 * should be mapped to relational database tables. Hibernate is the most popular
 * IMPLEMENTATION of JPA. When we use JPA annotations, Hibernate does the actual
 * work of generating SQL queries, managing connections, and persisting data.
 *
 * WHY 'jakarta.persistence' NOT 'javax.persistence'?
 * ---------------------------------------------------
 * In 2017, Oracle transferred Java EE to the Eclipse Foundation, which renamed
 * it to "Jakarta EE". Starting with Jakarta EE 9 (and Spring Boot 3.x),
 * all 'javax.*' packages were renamed to 'jakarta.*'. This is just a package
 * rename — same functionality, new namespace.
 */

/*
 * @Entity — This annotation is REQUIRED for JPA to recognize this class as a
 * database entity. Without it, Hibernate would completely ignore this class.
 *
 * WHAT HAPPENS BEHIND THE SCENES:
 * When Spring Boot starts, Hibernate scans for @Entity classes and:
 * 1. Creates (or validates) the corresponding table in the database
 * 2. Maps each field to a column (using naming strategies)
 * 3. Registers this entity in its internal metadata cache
 *
 * TABLE NAMING CONVENTION:
 * By default, Hibernate converts CamelCase class names to snake_case table names.
 * 'Patient' → 'patient' table. If you wanted a different name, you'd use
 * @Table(name = "custom_name").
 */
@Entity
public class Patient {

    /**
     * PRIMARY KEY FIELD
     * -----------------
     * @Id — Marks this field as the PRIMARY KEY of the 'patient' table.
     * Every JPA entity MUST have exactly one @Id field. Without it,
     * Hibernate throws: "No identifier specified for entity: Patient"
     *
     * WHY? A primary key uniquely identifies each row in a table.
     * Without it, how would you find, update, or delete a specific patient?
     *
     * @GeneratedValue(strategy = GenerationType.AUTO)
     * ------------------------------------------------
     * Tells Hibernate to AUTOMATICALLY generate the ID value — we don't
     * set it manually. 'GenerationType.AUTO' lets the JPA provider (Hibernate)
     * pick the best strategy based on the database:
     *   - For H2 (our dev DB): Uses a sequence-based approach
     *   - For PostgreSQL (our prod DB): Uses a sequence or UUID generation
     *
     * OTHER STRATEGIES YOU COULD USE:
     *   - IDENTITY: Auto-increment column (MySQL-style). DB generates the ID.
     *   - SEQUENCE: Uses a DB sequence object (PostgreSQL/Oracle-style).
     *   - TABLE: Uses a separate table to track IDs (portable but slow).
     *   - UUID: Spring can generate UUIDs automatically.
     *
     * WHY UUID INSTEAD OF LONG/INTEGER?
     * ----------------------------------
     * UUIDs are used instead of auto-incrementing integers because:
     * 1. Globally unique: No conflicts when merging data from multiple services
     * 2. Unpredictable: Attackers can't guess valid IDs (security benefit)
     * 3. Microservice-friendly: Each service can generate IDs independently
     *    without coordinating with a central database
     * 4. No sequential scanning: Users can't enumerate all patients by ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * PATIENT NAME
     * ------------
     * @NotNull — Jakarta Bean Validation constraint. Ensures this field
     * is not null when the entity is validated. If someone tries to save
     * a Patient with name=null, validation will fail.
     *
     * NOTE: @NotNull vs @NotBlank vs @NotEmpty:
     * - @NotNull: Only checks that the value is not null (allows "")
     * - @NotBlank: Checks not null AND not empty AND not just whitespace
     * - @NotEmpty: Checks not null AND not empty (allows whitespace)
     *
     * WHY @NotNull HERE BUT @NotBlank IN THE DTO?
     * The entity uses @NotNull as a database-level safety net.
     * The DTO uses @NotBlank for stricter API validation (no blank strings).
     * This is defense-in-depth: validate at the API layer first, then
     * the entity layer acts as a backup.
     */
    @NotNull
    private String name;

    /**
     * PATIENT EMAIL - WITH UNIQUE CONSTRAINT
     * ---------------------------------------
     * @Email — Validates that the string follows email format (e.g., x@y.z)
     * Uses a regex internally to check the pattern.
     *
     * @Column(unique = true) — This creates a UNIQUE CONSTRAINT on the
     * 'email' column in the database. The database itself will reject any
     * INSERT or UPDATE that would create a duplicate email.
     *
     * WHY BOTH @Column(unique=true) AND existsByEmail() CHECK?
     * --------------------------------------------------------
     * 1. @Column(unique=true): Database-level protection. Even if our Java
     *    code has a bug, the DB will prevent duplicates. This is the LAST
     *    line of defense. But it throws an ugly SQL exception.
     * 2. existsByEmail() in service layer: Application-level check that gives
     *    us a CLEAN, user-friendly error message. This is the FIRST line of
     *    defense. It checks BEFORE attempting the insert.
     *
     * This is the "Belt AND Suspenders" approach — redundant safety measures
     * at different layers ensure data integrity even if one layer fails.
     */
    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    /**
     * PATIENT ADDRESS
     * ---------------
     * NOTE: The field name starts with an uppercase 'A' — 'Address' instead
     * of 'address'. This violates Java naming conventions (fields should be
     * camelCase starting with lowercase). However, Hibernate handles this
     * gracefully by mapping it to the 'address' column in the database.
     *
     * In a production codebase, you should use 'address' (lowercase) to
     * follow Java Bean conventions. The getter/setter already follow the
     * correct pattern: getAddress()/setAddress().
     */
    @NotNull
    private String Address;

    /**
     * DATE OF BIRTH
     * -------------
     * LocalDate is Java 8's modern date class. It represents a date WITHOUT
     * time or timezone (e.g., 1985-06-15).
     *
     * WHY LocalDate INSTEAD OF java.util.Date?
     * -----------------------------------------
     * 1. Immutable: LocalDate is immutable (thread-safe). java.util.Date is mutable.
     * 2. No time component: We only care about the date, not hours/minutes.
     * 3. No timezone confusion: java.util.Date includes timezone info, leading to bugs.
     * 4. Modern API: Part of java.time package (JSR-310), the recommended approach.
     * 5. JPA 2.2+ natively supports LocalDate without converters.
     *
     * DATABASE MAPPING:
     * Hibernate maps LocalDate to the SQL 'DATE' type, which stores only
     * the date part (YYYY-MM-DD) — perfect for date of birth.
     */
    @NotNull
    private LocalDate dateOfBirth;

    /**
     * REGISTRATION DATE
     * -----------------
     * Tracks when the patient was registered in the system.
     * Uses LocalDate for the same reasons as dateOfBirth.
     *
     * NOTE: In a real system, you might auto-generate this using
     * @PrePersist callback or @CreatedDate from Spring Data Auditing,
     * rather than requiring it in the request. This would be set
     * automatically when the entity is first saved.
     */
    @NotNull
    private LocalDate registeredDate;

    // ========================================================================
    // GETTERS AND SETTERS
    // ========================================================================
    //
    // WHY DO WE NEED THESE?
    // ----------------------
    // JPA/Hibernate requires getters and setters to:
    // 1. READ field values when constructing SQL INSERT/UPDATE statements
    // 2. WRITE field values when mapping database rows back to Java objects
    //
    // Hibernate uses reflection to access these methods. Without them,
    // Hibernate can't populate the entity from query results or extract
    // values for persistence. The getter/setter naming MUST follow JavaBean
    // conventions: getFieldName() / setFieldName() / isFieldName() for booleans.
    //
    // ALTERNATIVE: Lombok's @Getter/@Setter or Java Records could eliminate
    // this boilerplate. However, JPA entities cannot be records (records are
    // immutable and final, but JPA needs mutable, non-final classes).
    // Lombok's @Data or @Getter/@Setter are commonly used in production.
    // ========================================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }


}
