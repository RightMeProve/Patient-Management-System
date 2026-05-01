# 🏥 Patient Service

The **Patient Service** is the core microservice in our architecture, responsible for managing the lifecycle of patient records. It demonstrates fundamental enterprise patterns including robust REST API design, validation, data persistence, and inter-service communication using both synchronous (gRPC) and asynchronous (Kafka) paradigms.

## 🏗️ Architecture & Design Patterns

### The Three-Tier Architecture
This service rigidly follows a standard Layered (Three-Tier) Architecture, which promotes the **Single Responsibility Principle (SRP)**:
1. **Controller Layer (`PatientController.java`)**: The entry point. Handles HTTP specifics, parsing requests, and setting HTTP status codes. It relies on Bean Validation (`@Valid`) to catch bad data before it hits business logic.
2. **Service Layer (`PatientService.java`)**: The brain. Contains business rules (e.g., checking if an email is already registered). It coordinates between the database, external gRPC clients, and Kafka producers.
3. **Repository Layer (`PatientRepository.java`)**: The data interface. Extends `JpaRepository` to provide auto-generated CRUD operations and custom derived queries (like `existsByEmail`).

### Important Patterns Used
- **DTO (Data Transfer Object) Pattern**: We never expose our JPA `@Entity` directly to the API consumer. `PatientRequestDto` and `PatientResponseDto` act as a firewall, ensuring API contracts remain stable even if database schemas change.
- **Mapper Pattern**: Centralized conversion logic (`PatientMapper.java`) keeps our Services and Controllers clean from repetitive boilerplate code mapping fields from DTOs to Entities.
- **Global Exception Handling**: Utilizing Spring's `@ControllerAdvice` (`GlobalExceptionHandler.java`), all exceptions (like `PatientNotFoundException` or validation failures) are intercepted and translated into standardized JSON error responses.

## 🔄 Inter-Service Communication

### Synchronous: gRPC (Billing Service)
When a new patient is created, we need to synchronously ensure a billing account is established. We use a **gRPC Client** (`BillingServiceGrpcClient.java`).
- **Why gRPC?** It uses HTTP/2 and binary Protobuf serialization, making it significantly faster and lighter than traditional REST. It provides strong type-safety via generated stubs.

### Asynchronous: Apache Kafka (Analytics Service)
After a patient is successfully created and persisted, we fire a `PatientEvent` to a Kafka topic.
- **Why Kafka?** This is the **Fire-and-Forget** approach. The patient creation shouldn't fail or slow down if the Analytics system is offline. The `KafkaProducer.java` serializes the event using Protobuf and publishes it asynchronously.

## 📂 Core Components Deep Dive

### Entity (`Patient.java`)
- Mapped to the database using JPA annotations (`@Entity`, `@Table`).
- Uses a `UUID` as the primary key. UUIDs are excellent for distributed systems because they can be generated independently without collision, unlike sequential database IDs.
- Includes auditing fields (e.g., `registeredDate`) configured to default to `LocalDate.now()`.

### Validations
- **Syntactic Validation**: Handled by annotations like `@NotBlank`, `@Email`, `@Past` on the DTO. Handled at the Controller layer.
- **Semantic Validation**: Handled by the Service layer. For example, checking the database via `existsByEmailAndIdNot` to ensure uniqueness during an update.

## 🚀 Running the Service

The service runs on **Port 4000**. It uses an in-memory **H2 database** for rapid development without external dependencies.
- **Data Seeding**: A `data.sql` file in `src/main/resources` automatically executes on startup, seeding the database with initial records for immediate testing.
- **H2 Console**: Accessible at `http://localhost:4000/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`).

## 📚 Educational Takeaways
1. **Dependency Injection**: Always favor Constructor Injection over Field Injection (`@Autowired`) for better testability and immutability.
2. **REST Semantics**: `POST` for creation (returns 201 Created), `PUT` for full updates, `GET` for retrieval.
3. **Robust Error Handling**: Never leak internal exception stack traces to the client. Always return clean, actionable error messages.
