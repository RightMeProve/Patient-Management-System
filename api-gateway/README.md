# 🌐 API Gateway

The **API Gateway** acts as the single point of entry for all external client requests in our microservices architecture. Built on **Spring Cloud Gateway**, it handles routing, security filtering, and load balancing.

## 🏗️ Architecture & Gateway Pattern

In a distributed system, you don't want your frontend clients (React, Mobile apps) to know the IP addresses and ports of all your individual microservices.

### Why use an API Gateway?
- **Single Entry Point**: Clients only need to know one URL (the Gateway URL).
- **Security & Authentication**: We can validate JWT tokens at the gateway level before the request even reaches the internal network.
- **Cross-Cutting Concerns**: We can handle CORS, rate limiting, logging, and metrics in one place.
- **Protocol Translation**: The gateway can accept HTTP externally, but talk gRPC or WebSockets internally if needed.

## 📂 Core Components Deep Dive

### Reactive Stack (Spring WebFlux)
Spring Cloud Gateway is built on **Project Reactor** and **Netty** (Spring WebFlux).
- **Non-Blocking**: It does not allocate a dedicated thread per request like traditional Spring MVC (Tomcat). Instead, it uses an event loop.
- **Why?** Gateways handle massive amounts of concurrent connections. If it used traditional threads, it would run out of memory quickly. Non-blocking I/O allows a single thread to handle thousands of connections.

### Route Configuration (`application.yml`)
The gateway routes are configured declaratively in the `application.yml` file.
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: patient-service
          uri: http://localhost:4000
          predicates:
            - Path=/patients/**
```
When a request hits `http://localhost:8080/patients/1`, the gateway matches the `Path` predicate and forwards the request to `http://localhost:4000/patients/1`.

### Custom JWT Validation Filter (`JwtValidationGatewayFilterFactory.java`)
We implemented a custom filter to secure our routes.
- **AbstractGatewayFilterFactory**: Extending this class allows Spring to register our custom logic as a Gateway Filter.
- **WebClient**: Because the gateway is reactive, we cannot use `RestTemplate`. We use `WebClient` to make a non-blocking HTTP call to the **Auth Service** (`/validate` endpoint).
- **The Flow**: 
  1. Intercept the incoming request.
  2. Extract the `Authorization: Bearer <token>` header.
  3. Send the token to the Auth Service.
  4. If Auth Service returns `200 OK`, forward the request to the target microservice (e.g., Patient Service).
  5. If Auth Service returns `401 Unauthorized`, abort the request and return an error to the client immediately.

## 🚀 Running the Service

The gateway runs on **Port 8080** (Standard HTTP port for local dev).
- Ensure your backend services (Auth Service, Patient Service) are running first.
- Send requests through the gateway instead of directly to the microservices.

## 📚 Educational Takeaways
1. **Centralized Security**: By moving token validation to the gateway, our backend microservices (like Patient Service) remain completely unaware of JWT specifics. They just trust that any request coming from the gateway has already been authenticated.
2. **Reactive Programming**: The custom filter demonstrates how to chain asynchronous operations (`.then(chain.filter(exchange))`) without blocking the main thread.
