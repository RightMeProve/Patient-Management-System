# 🏥 Patient Management System — Microservices Architecture

> A **Healthcare Patient Management Platform** built from the ground up using **Spring Boot Microservices**. The system manages patient records through a distributed architecture featuring REST APIs, gRPC for inter-service communication, Kafka for event streaming, an API Gateway with JWT authentication, and cloud deployment on AWS.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue?logo=docker)](https://www.docker.com/)
[![gRPC](https://img.shields.io/badge/gRPC-Communication-purple)](https://grpc.io/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-black?logo=apachekafka)](https://kafka.apache.org/)
[![AWS](https://img.shields.io/badge/AWS-Cloud%20Deploy-orange?logo=amazonaws)](https://aws.amazon.com/)

---

## 📋 Table of Contents

- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Development Roadmap](#-development-roadmap)
- [Getting Started](#-getting-started)
- [API Endpoints](#-api-endpoints)
- [Design Patterns & Principles](#-design-patterns--principles)
- [How It Works](#-how-it-works)

---

## 🏗️ System Architecture

The platform is designed as a **microservices ecosystem** — each service is independently deployable, owns its own database, and communicates via REST, gRPC, or Kafka depending on the use case.

```
                                    ┌─────────────────┐
                                    │   API Gateway    │
                                    │  (Spring Cloud)  │
                                    │   + JWT Filter   │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
           ┌───────▼───────┐       ┌────────▼───────┐      ┌────────▼───────┐
           │   Patient     │       │     Auth       │      │   Auth Docs    │
           │   Service     │       │    Service     │      │   (Swagger)    │
           │  (REST API)   │       │  (JWT/Login)   │      │                │
           └───────┬───────┘       └────────┬───────┘      └────────────────┘
                   │                        │
          ┌────────┼────────┐               │
          │        │        │       ┌───────▼────────┐
    ┌─────▼──┐ ┌───▼───┐ ┌─▼──┐   │  PostgreSQL    │
    │PostgreSQL│ │ gRPC  │ │Kafka│   │  (Auth DB)     │
    │(Patient │ │Client │ │Prod.│   └────────────────┘
    │   DB)   │ │       │ │    │
    └─────────┘ └───┬───┘ └──┬─┘
                    │        │
             ┌──────▼──────┐ │
             │   Billing   │ │
             │   Service   │ │
             │(gRPC Server)│ │
             └─────────────┘ │
                             │
                    ┌────────▼────────┐
                    │   Analytics     │
                    │    Service      │
                    │(Kafka Consumer) │
                    └─────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **Language** | Java 21 | Core programming language |
| **Framework** | Spring Boot 3.5 | Application framework with auto-configuration |
| **ORM** | Spring Data JPA + Hibernate | Database abstraction & Object-Relational Mapping |
| **Database (Dev)** | H2 (In-Memory) | Lightweight dev database — zero setup |
| **Database (Prod)** | PostgreSQL | Production-grade relational database |
| **Validation** | Jakarta Bean Validation | Declarative input validation (@NotBlank, @Email) |
| **API Communication** | REST + gRPC | Synchronous inter-service communication |
| **Event Streaming** | Apache Kafka | Asynchronous event-driven messaging |
| **API Gateway** | Spring Cloud Gateway | Routing, filtering, and auth integration |
| **Authentication** | JWT + Spring Security | Stateless token-based authentication |
| **Containerization** | Docker | Service isolation and deployment |
| **IaC / Cloud** | AWS (ECS, MSK, RDS) + CloudFormation | Cloud infrastructure and deployment |
| **Local Cloud** | LocalStack | AWS services emulation for local development |
| **API Docs** | OpenAPI / Swagger | Interactive API documentation |
| **Build Tool** | Maven | Dependency management and build automation |
| **Testing** | JUnit 5 + Integration Tests | Unit and integration testing |

---

## 📁 Project Structure

```
Patient-Management-System/
│
├── README.md                              # Project documentation
│
├── api-requests/                          # HTTP request files for manual API testing
│   └── patient-service/
│       ├── create-patients.http           # POST request to create a patient
│       └── get-patients.http             # GET request to list all patients
│
├── patient-service/                       # 🏥 Core Patient Microservice
│   ├── pom.xml                           # Maven dependencies & build config
│   └── src/
│       ├── main/
│       │   ├── java/com/provemeright/patient_service/
│       │   │   ├── PatientServiceApplication.java    # Spring Boot entry point
│       │   │   ├── controller/
│       │   │   │   └── PatientController.java        # REST API endpoints
│       │   │   ├── dto/
│       │   │   │   ├── PatientRequestDto.java        # Input validation DTO
│       │   │   │   └── PatientResponseDto.java       # API response DTO
│       │   │   ├── exception/
│       │   │   │   ├── EmailAlreadyExistsException.java  # Custom business exception
│       │   │   │   └── GlobalExceptionHandler.java       # Centralized error handling
│       │   │   ├── mapper/
│       │   │   │   └── PatientMapper.java            # Entity ↔ DTO conversion
│       │   │   ├── model/
│       │   │   │   └── Patient.java                  # JPA Entity (DB table mapping)
│       │   │   ├── repository/
│       │   │   │   └── PatientRepository.java        # Data access interface (Spring Data)
│       │   │   └── service/
│       │   │       └── PatientService.java           # Business logic layer
│       │   └── resources/
│       │       ├── application.properties            # App configuration
│       │       └── data.sql                          # Seed data (15 sample patients)
│       └── test/
│           └── java/.../PatientServiceApplicationTests.java  # Smoke test
│
├── billing-service/                       # 💰 Billing Microservice
├── analytics-service/                     # 📊 Analytics Microservice
├── auth-service/                          # 🔐 Auth Microservice
├── api-gateway/                           # 🌐 API Gateway
```

---

## 🌟 Features & Capabilities

This project is a finalized, production-grade microservices implementation demonstrating best practices in scalable architecture.

### ✅ Core Capabilities
- **Patient Management**: Full CRUD REST APIs with validation and global exception handling.
- **Authentication & Security**: Dedicated Auth Service with Spring Security, BCrypt, and stateless JWT generation/validation.
- **Inter-service Communication**: 
  - Synchronous gRPC communication between Patient and Billing services for fast provisioning.
  - Asynchronous event streaming via Apache Kafka from Patient to Analytics service.
- **API Gateway**: Spring Cloud Gateway routing with centralized JWT filter protection.
- **Database Architecture**: 
  - Domain-driven PostgreSQL/H2 databases per service.
  - Entity-DTO mapping patterns to prevent data leakage.
- **Containerization**: Fully Dockerized services designed for cloud orchestration.

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK) — [Download](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
- **Docker** — [Download](https://www.docker.com/products/docker-desktop/) *(needed for later services)*
- **IDE** — IntelliJ IDEA (recommended) or VS Code with Java extensions

### Run the Patient Service

```bash
# Clone the repository
git clone https://github.com/rightmeprove/Patient-Management-System.git
cd Patient-Management-System/patient-service

# Build the project (downloads dependencies + compiles)
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The service starts on **http://localhost:4000**

### Verify It's Running

```bash
# Get all patients (returns 15 pre-seeded patients)
curl http://localhost:4000/patients

# Create a new patient
curl -X POST http://localhost:4000/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "address": "123 Test St",
    "dateOfBirth": "1995-01-01",
    "registeredDate": "2024-01-01"
  }'
```

### H2 Database Console
Access the in-memory database UI at: **http://localhost:4000/h2-console**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `admin_viewer`
- Password: `password`

---

## 📡 API Endpoints

### Patient Service (Port 4000)

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| `GET` | `/patients` | List all patients | ✅ Live |
| `POST` | `/patients` | Create a new patient | ✅ Live |
| `PUT` | `/patients/{id}` | Update a patient | ✅ Live |
| `DELETE` | `/patients/{id}` | Delete a patient | ✅ Live |

### Request Body Example (POST `/patients`)

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "address": "123 Main Street",
  "dateOfBirth": "1995-09-09",
  "registeredDate": "2024-11-28"
}
```

### Response Example

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "John Doe",
  "email": "john@example.com",
  "address": "123 Main Street",
  "dateOfBirth": "1995-09-09"
}
```

### Error Responses

**Validation Error (400):**
```json
{
  "name": "Name is required",
  "email": "Email should be valid",
  "address": "Address is required"
}
```

**Duplicate Email Error (400):**
```json
{
  "message": "Email already exists!"
}
```

---

## 🎯 Design Patterns & Principles

### Architecture Patterns
| Pattern | Where Used | Why |
|---------|-----------|-----|
| **Three-Tier Architecture** | Controller → Service → Repository | Separation of concerns |
| **DTO Pattern** | PatientRequestDto, PatientResponseDto | Decouple API contract from DB schema |
| **Repository Pattern** | PatientRepository | Abstract data access behind an interface |
| **Mapper Pattern** | PatientMapper | Centralized, testable data transformation |
| **Global Exception Handling** | GlobalExceptionHandler (@ControllerAdvice) | Consistent error responses across all endpoints |

### SOLID Principles
| Principle | How It's Applied |
|-----------|-----------------|
| **S** — Single Responsibility | Each class has one job (Controller=HTTP, Service=logic, Repo=data) |
| **O** — Open/Closed | New exception types can be added without modifying existing handlers |
| **L** — Liskov Substitution | JpaRepository interface allows swapping implementations |
| **I** — Interface Segregation | PatientRepository exposes only the methods we need |
| **D** — Dependency Inversion | Service depends on the Repository interface, not a concrete class |

---

## ⚙️ How It Works

### Request Flow

```
Client Request → Tomcat → DispatcherServlet → Controller → Service → Repository → Database
                                                  ↓              ↓
                                            @Valid runs    Business logic
                                           (Bean Validation)  (email check)
                                                  ↓
                                         On failure → GlobalExceptionHandler → Error JSON
```

### Layered Architecture

```
┌──────────────────────────────────────────────────────┐
│                    HTTP Request                       │
└───────────────────────┬──────────────────────────────┘
                        ▼
┌──────────────────────────────────────────────────────┐
│  @RestController — PatientController                  │
│  Receives requests, triggers validation, delegates.   │
└───────────────────────┬──────────────────────────────┘
                        ▼
┌──────────────────────────────────────────────────────┐
│  @Service — PatientService                            │
│  Business logic: duplicate email check, orchestration │
└───────────────────────┬──────────────────────────────┘
                        ▼
┌──────────────────────────────────────────────────────┐
│  @Repository — PatientRepository                      │
│  Auto-generated CRUD + custom query (existsByEmail)   │
└───────────────────────┬──────────────────────────────┘
                        ▼
┌──────────────────────────────────────────────────────┐
│  H2 Database (Dev) / PostgreSQL (Prod)                │
│  15 pre-seeded patient records via data.sql           │
└──────────────────────────────────────────────────────┘
```

---

## 🤝 Contributing

This project is actively under development. Contributions are welcome!
- ⭐ Star the repo if you find it useful
- 🐛 Open issues for bugs or suggestions
- 🔀 Submit PRs for improvements
- 📖 Check the code comments — every file is thoroughly documented

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

<div align="center">
  <b>Built with ❤️ — a microservices deep-dive, one service at a time</b>
</div>
