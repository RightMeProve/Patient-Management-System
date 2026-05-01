# 💰 Billing Service

The **Billing Service** is a dedicated microservice that handles financial transactions and account management for patients. In our architecture, it specifically showcases how to implement **gRPC (gRPC Remote Procedure Calls)** for highly performant, synchronous inter-service communication.

## 🏗️ Architecture & gRPC Deep Dive

Unlike the Patient Service which primarily exposes a REST API to the outside world, the Billing Service exposes a **gRPC Server** that is called internally by other microservices (like the Patient Service).

### Why gRPC over REST?
- **Performance**: gRPC uses HTTP/2, which allows multiplexing (multiple requests over a single TCP connection) and server push.
- **Payload Size**: It uses **Protocol Buffers (Protobuf)** instead of JSON. Protobuf is a strongly typed, binary serialization format. Because it's binary and compressed, the payloads are significantly smaller than JSON strings.
- **Contract-First Development**: You define the API contract in a `.proto` file. The gRPC compiler then auto-generates the client stubs and server interfaces in any supported language. This guarantees that the client and server agree on the data structure and types.

## 📂 Core Components

### Protobuf Definition (`billing.proto`)
This file is the single source of truth for the service contract.
- It defines the `BillingService` with an RPC method: `rpc CreateBillingAccount (CreateBillingAccountRequest) returns (CreateBillingAccountResponse);`
- It defines the structure of the Request and Response messages.

### gRPC Server Implementation (`BillingGrpcService.java`)
- Extends the auto-generated `BillingServiceGrpc.BillingServiceImplBase`.
- Uses `@GrpcService` (from the `net.devh` spring-boot-starter) to tell Spring to expose this class as a gRPC endpoint.
- Overrides the `createBillingAccount` method to implement the actual business logic.
- Instead of returning a value normally, gRPC uses an asynchronous `StreamObserver` to stream the response back to the client (`responseObserver.onNext()` and `responseObserver.onCompleted()`).

## 🔄 Interaction Flow
1. A user calls `POST /patients` on the **Patient Service**.
2. The Patient Service saves the patient to its database.
3. The Patient Service (acting as a gRPC Client) synchronously calls the `CreateBillingAccount` method on the **Billing Service**.
4. The Billing Service receives the binary Protobuf request, processes it, creates an account, and sends a binary response back.
5. The Patient Service waits for this response before finalizing its own workflow.

## 🚀 Running the Service

The Billing Service runs as a standard Spring Boot application but exposes a gRPC port.
- **gRPC Port**: Typically `9090` (configured in `application.yml` via `grpc.server.port`).
- **Testing**: Since it's not a REST API, you cannot test it with tools like Postman or `curl` directly. You need a gRPC client like **grpcurl** or **BloomRPC**.

## 📚 Educational Takeaways
1. **Synchronous Coupling**: Using gRPC creates temporal coupling (if the Billing Service is down, the Patient Service will fail to create a patient). This is a deliberate design choice when strong consistency is required immediately.
2. **Schema Evolution**: Protobuf uses field tags (e.g., `string patient_id = 1;`). This allows you to add or deprecate fields over time without breaking backwards compatibility with older clients.
