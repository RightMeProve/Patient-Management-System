package com.provemeright.patient_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * PATIENT SERVICE - APPLICATION ENTRY POINT
 * ============================================================================
 *
 * WHY THIS CLASS EXISTS:
 * ---------------------
 * Every Spring Boot application needs exactly ONE class annotated with
 * {@code @SpringBootApplication} that contains a {@code main()} method.
 * This class serves as the "ignition key" — it bootstraps the entire
 * Spring Framework, setting up Dependency Injection, auto-configuration,
 * embedded Tomcat server, and component scanning.
 *
 * WHAT @SpringBootApplication DOES (IT'S A META-ANNOTATION):
 * ----------------------------------------------------------
 * {@code @SpringBootApplication} is actually a shorthand that combines
 * THREE separate annotations into one:
 *
 * 1. {@code @SpringBootConfiguration}
 *    → Marks this class as a source of bean definitions (like @Configuration).
 *    → WHY? Spring needs to know which class holds the configuration. This
 *      tells Spring: "Hey, start here to find how the app is configured."
 *
 * 2. {@code @EnableAutoConfiguration}
 *    → Tells Spring Boot to automatically configure beans based on what
 *      JAR dependencies are on the classpath.
 *    → WHY? For example, because we have 'spring-boot-starter-data-jpa' in
 *      pom.xml, Spring auto-configures a DataSource, EntityManagerFactory,
 *      and TransactionManager — all without us writing a single line of
 *      configuration. This is the "magic" of Spring Boot.
 *    → HOW? It reads META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 *      from each starter JAR and conditionally applies configurations.
 *
 * 3. {@code @ComponentScan}
 *    → Scans the current package AND all sub-packages for Spring components
 *      (@Component, @Service, @Repository, @Controller, @RestController).
 *    → WHY? This is how Spring "discovers" our PatientController,
 *      PatientService, PatientRepository, and GlobalExceptionHandler
 *      without us manually registering them.
 *    → IMPORTANT: This is why package structure matters! If PatientController
 *      was in a package OUTSIDE of 'com.provemeright.patient_service',
 *      Spring would NOT find it. Everything must be in this package or below.
 *
 * APPLICATION STARTUP FLOW:
 * -------------------------
 * 1. JVM calls main() → entry point of any Java application
 * 2. SpringApplication.run() is called → this triggers:
 *    a. Creates the ApplicationContext (the IoC/DI container)
 *    b. Performs component scanning (finds @Service, @Controller, etc.)
 *    c. Auto-configures beans (DataSource, JPA, Tomcat, etc.)
 *    d. Runs data.sql (because spring.sql.init.mode=always)
 *    e. Starts the embedded Tomcat server on port 4000 (configured in application.properties)
 *    f. Application is ready to accept HTTP requests!
 */
@SpringBootApplication
public class PatientServiceApplication {

	/**
	 * The standard Java entry point. The JVM looks for this exact signature:
	 * 'public static void main(String[] args)' to start execution.
	 *
	 * WHY 'static'? → Because the JVM needs to call this method WITHOUT
	 * creating an instance of the class first. Static methods belong to the
	 * class itself, not to any object.
	 *
	 * WHY pass 'args'? → Command-line arguments can override application
	 * properties. For example: java -jar app.jar --server.port=8080
	 * would override our configured port 4000.
	 *
	 * @param args Command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		// SpringApplication.run() does the heavy lifting:
		// 1. Creates a new SpringApplication instance
		// 2. Determines the application type (SERVLET for web apps)
		// 3. Loads application.properties/application.yml
		// 4. Creates and refreshes the ApplicationContext
		// 5. Starts the embedded web server (Tomcat by default)
		// The first argument tells Spring which class to use as the root configuration.
		// The second argument forwards any CLI args for property overriding.
		SpringApplication.run(PatientServiceApplication.class, args);
	}

}
