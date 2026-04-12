package com.provemeright.patient_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ============================================================================
 * APPLICATION SMOKE TEST — CONTEXT LOADING VERIFICATION
 * ============================================================================
 *
 * WHAT IS THIS TEST?
 * ------------------
 * This is a "smoke test" — the most basic test that verifies the Spring
 * Boot application can START UP without errors. If this test fails, it
 * means something fundamental is broken (misconfigured beans, missing
 * dependencies, invalid properties, circular dependencies, etc.).
 *
 * WHY IS IT CALLED A "SMOKE TEST"?
 * The term comes from hardware testing — when you plug in a new circuit
 * board and turn it on, if smoke comes out, something is fundamentally wrong.
 * Similarly, if this test fails, something is fundamentally misconfigured.
 *
 * @SpringBootTest EXPLAINED:
 * ---------------------------
 * This annotation tells JUnit to:
 * 1. Start the ENTIRE Spring Boot application context (all beans, configs)
 * 2. Load application.properties
 * 3. Create all beans (@Service, @Repository, @Controller, etc.)
 * 4. Initialize the database (run data.sql)
 * 5. Start the embedded web server (unless told otherwise)
 *
 * This is an INTEGRATION TEST (not a unit test) because it boots up
 * the whole application. It's slow but verifies everything works together.
 *
 * WHAT contextLoads() ACTUALLY TESTS:
 * If the method body is EMPTY, what's the point? The value is in what
 * happens BEFORE the method runs. @SpringBootTest tries to create the
 * entire ApplicationContext. If ANY of the following fail, this test fails:
 * - Bean creation errors (missing dependencies, circular references)
 * - Configuration errors (invalid properties, bad YAML syntax)
 * - Database connection failures
 * - Component scanning issues
 * - Auto-configuration conflicts
 *
 * JUNIT 5 (JUnit Jupiter):
 * - @Test marks this method as a test case
 * - JUnit 5 uses 'org.junit.jupiter.api.Test' (not 'org.junit.Test' from JUnit 4)
 * - No @RunWith needed (JUnit 5 uses @ExtendWith, and @SpringBootTest includes it)
 */
@SpringBootTest
class PatientServiceApplicationTests {

	@Test
	void contextLoads() {
		// This method is intentionally empty.
		// The test passes if the Spring ApplicationContext loads successfully.
		// The test fails if the context fails to start (config errors, missing beans, etc.).
	}

}
