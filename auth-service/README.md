# 🔐 Auth Service

The **Auth Service** is responsible for identity management and securing the entire microservices ecosystem. It implements **Stateless Authentication** using **JSON Web Tokens (JWT)**.

## 🏗️ Architecture & Security Pattern

Instead of using traditional stateful sessions (where the server stores a session ID in memory or a database and sends a cookie to the browser), we use a stateless, token-based approach.

### Why JWT (JSON Web Tokens)?
- **Stateless**: The server does not need to store the token in a database. All the information required to verify the user is cryptographically signed inside the token itself.
- **Scalable**: Because the Auth Service doesn't store session state, we can easily run 10 instances of the Auth Service behind a load balancer without configuring "sticky sessions."
- **Decoupled**: Once the Auth Service issues a token, the API Gateway can verify its authenticity simply by checking the cryptographic signature, without needing to query a database for every single request.

## 📂 Core Components Deep Dive

### Security Configuration (`SecurityConfig.java`)
- Uses **Spring Security** to configure the HTTP firewall.
- By default, Spring Security secures *every* endpoint. We configure it to permit unauthenticated access to the `/login` and `/validate` endpoints (and Swagger docs), while securing everything else.
- Uses `BCryptPasswordEncoder` to securely hash passwords before storing them in the database, ensuring that even if the database is compromised, raw passwords are safe.

### Token Generation & Validation (`JwtUtil.java`)
- **Generation**: When a user logs in, we use the `io.jsonwebtoken` (JJWT) library to construct a token. It contains:
  - **Header**: Defines the algorithm (e.g., HS256).
  - **Payload**: Contains "Claims" (like the user's ID, username, and token expiration time).
  - **Signature**: A cryptographic hash created using our server's secret key.
- **Validation**: When the API Gateway asks us to validate a token, `JwtUtil` attempts to parse it using our secret key. If the signature is invalid, or the expiration time has passed, it throws an exception (resulting in a `401 Unauthorized`).

### Controller Endpoints (`AuthController.java`)
- `POST /login`: Accepts username/password. If valid, returns a generated JWT.
- `GET /validate`: Accepts an `Authorization: Bearer <token>` header. Returns `200 OK` if the token is mathematically valid and not expired.

## 🔄 Interaction Flow (The "Login" Scenario)
1. **Client** sends `POST /login` with `{ "username": "admin", "password": "password" }`.
2. **Auth Service** checks the database. If the password matches the BCrypt hash, it generates a JWT string (e.g., `eyJhbG...`) and returns it.
3. **Client** stores this token (usually in local storage or an HttpOnly cookie).
4. **Client** wants to fetch patients. It sends `GET /patients` to the **API Gateway**, including the header `Authorization: Bearer eyJhbG...`.
5. **API Gateway** halts the request and forwards the token to `GET /validate` on the **Auth Service**.
6. **Auth Service** mathematically verifies the signature. It returns `200 OK`.
7. **API Gateway** resumes the request and forwards it to the **Patient Service**.
8. **Patient Service** returns the list of patients to the Gateway, which returns it to the Client.

## 🚀 Running the Service

- Runs on an independent port (typically configured in its `application.yml`).
- Depends on a PostgreSQL database to store the `User` table securely.

## 📚 Educational Takeaways
1. **Never Store Plaintext Passwords**: Always hash passwords using a slow hashing algorithm like BCrypt, PBKDF2, or Argon2.
2. **Do Not Store Sensitive Info in JWTs**: The payload of a JWT is simply Base64 encoded, **not encrypted**. Anyone who intercepts the token can decode the payload. Never put passwords or SSNs inside the token payload!
