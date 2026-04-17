package com.provemeright.patient_service.repository;

import com.provemeright.patient_service.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * ============================================================================
 * PATIENT REPOSITORY - DATA ACCESS LAYER (DAL)
 * ============================================================================
 *
 * WHAT IS THE REPOSITORY PATTERN?
 * --------------------------------
 * The Repository pattern abstracts all database operations behind an interface.
 * The rest of the application (Service, Controller) never writes SQL or knows
 * which database is being used. They just call methods like save(), findAll(),
 * findById(), deleteById(), etc.
 *
 * WHY IS THIS AN INTERFACE AND NOT A CLASS?
 * -------------------------------------------
 * This is one of the most powerful features of Spring Data JPA. We declare
 * an interface, and Spring AUTOMATICALLY GENERATES the implementation class
 * at runtime using dynamic proxies (specifically, JDK Proxy or CGLIB).
 *
 * WHAT DOES THIS MEAN? We write ZERO implementation code. No SQL. No JDBC.
 * Spring creates a concrete class called something like
 * 'SimpleJpaRepository' that implements all the methods for us.
 *
 * HOW DOES THIS MAGIC WORK? (UNDER THE HOOD):
 * ---------------------------------------------
 * 1. At startup, Spring scans for interfaces extending JpaRepository
 * 2. For each one, it creates a proxy object using java.lang.reflect.Proxy
 * 3. This proxy intercepts all method calls
 * 4. For standard methods (save, findAll, etc.), it delegates to SimpleJpaRepository
 * 5. For custom query methods (like existsByEmail), it parses the method name
 *    and generates the JPQL/SQL query automatically
 *
 * JpaRepository<Patient, UUID> EXPLAINED:
 * ----------------------------------------
 * We extend JpaRepository with TWO type parameters:
 *   - Patient: The ENTITY class this repository manages
 *   - UUID: The TYPE of the primary key (@Id) field in that entity
 *
 * WHAT WE GET FOR FREE (INHERITED METHODS):
 * ------------------------------------------
 * JpaRepository extends PagingAndSortingRepository, which extends CrudRepository.
 * This inheritance chain gives us these methods without writing any code:
 *
 * From CrudRepository:
 *   - save(entity)          → INSERT or UPDATE (if already exists)
 *   - saveAll(entities)     → Batch INSERT/UPDATE
 *   - findById(id)          → SELECT WHERE id = ?
 *   - existsById(id)        → SELECT COUNT(*) WHERE id = ?
 *   - findAll()             → SELECT * (all rows)
 *   - count()               → SELECT COUNT(*)
 *   - deleteById(id)        → DELETE WHERE id = ?
 *   - delete(entity)        → DELETE by entity reference
 *   - deleteAll()           → DELETE * (truncate)
 *
 * From PagingAndSortingRepository:
 *   - findAll(Sort sort)    → SELECT * ORDER BY ...
 *   - findAll(Pageable)     → SELECT * LIMIT ... OFFSET ... (pagination)
 *
 * From JpaRepository:
 *   - flush()               → Force pending changes to DB immediately
 *   - saveAndFlush(entity)  → Save + flush in one call
 *   - deleteInBatch(entities) → Batch delete (more efficient than one-by-one)
 *
 * @Repository ANNOTATION:
 * -----------------------
 * Marks this interface as a Spring-managed bean in the persistence layer.
 * While Spring Data JPA can detect repository interfaces without this
 * annotation (thanks to auto-configuration), adding it explicitly:
 * 1. Makes the intent clear to other developers
 * 2. Enables Spring's DataAccessException translation (converts vendor-specific
 *    database exceptions into Spring's unified exception hierarchy)
 *
 * NOTE: Technically, @Repository is optional here because Spring Data JPA
 * auto-registers interfaces extending JpaRepository. But it's a good
 * practice for readability and consistency.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * DERIVED QUERY METHOD - SPRING DATA MAGIC
     * -----------------------------------------
     * This method checks if a patient with the given email already exists.
     *
     * HOW DOES SPRING KNOW WHAT SQL TO GENERATE?
     * Spring Data JPA parses the method name following a strict naming convention:
     *
     *   existsByEmail(String email)
     *   ├── "exists" → Return type: boolean (SELECT COUNT(*) > 0 or EXISTS subquery)
     *   ├── "By"     → Separator: marks the start of the WHERE clause
     *   └── "Email"  → Field name in the Patient entity to filter by
     *
     * GENERATED SQL (approximately):
     *   SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
     *   FROM patient
     *   WHERE email = ?
     *
     * OTHER QUERY METHOD EXAMPLES YOU COULD WRITE:
     *   - findByName(String name)                → WHERE name = ?
     *   - findByNameAndEmail(String n, String e)  → WHERE name = ? AND email = ?
     *   - findByDateOfBirthBefore(LocalDate date) → WHERE date_of_birth < ?
     *   - countByAddress(String address)          → SELECT COUNT(*) WHERE address = ?
     *   - deleteByEmail(String email)             → DELETE WHERE email = ?
     *   - findByNameContaining(String keyword)    → WHERE name LIKE '%keyword%'
     *   - findByNameOrderByRegisteredDateDesc()   → ORDER BY registered_date DESC
     *
     * WHY USE THIS INSTEAD OF WRITING SQL?
     * 1. Type-safe: Compile-time verification that the field 'email' exists
     * 2. DB-agnostic: Works with H2, PostgreSQL, MySQL, etc. without changes
     * 3. Less code: One line replaces 30+ lines of JDBC boilerplate
     * 4. Less error-prone: No string-based SQL that can have typos
     *
     * @param email The email address to check for existence
     * @return true if a patient with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * DERIVED QUERY METHOD - MULTIPLE CONDITIONS (AND + Not)
     * ------------------------------------------------------
     * This method checks if an email exists but EXCLUDING a specific patient ID.
     * 
     * WHY DO WE NEED THIS FOR 'UPDATE'?
     * When updating a patient (e.g., ID 123), we must check if their new email
     * is already taken. BUT if they keep their existing email, the simple
     * `existsByEmail("john@example.com")` would return true (because ID 123
     * currently has it!). This would falsely trigger our duplicate email error.
     * 
     * To safely check for duplicates during an update, we must ask the database:
     * "Does this email exist AND does it belong to someone OTHER THAN me?"
     * 
     * METHOD NAME PARSING:
     *   existsByEmailAndIdNot(String email, UUID id)
     *   ├── "existsBy" → SELECT COUNT(*) > 0 WHERE...
     *   ├── "Email"    → email = ?1
     *   ├── "And"      → AND
     *   ├── "Id"       → id
     *   └── "Not"      → != ?2
     * 
     * GENERATED SQL (approximately):
     *   SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
     *   FROM patient
     *   WHERE email = ? AND id != ?
     *
     * @param email The email address to check
     * @param id The ID to exclude from the check
     * @return true if the email is used by another patient, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, UUID id);
}
