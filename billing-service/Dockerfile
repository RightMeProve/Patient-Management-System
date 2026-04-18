# ============================================================================
# BILLING SERVICE DOCKERFILE - MULTI-STAGE BUILD
# ============================================================================
#
# WHAT IS DOCKER?
# ---------------
# Docker is a platform that packages an application and all its dependencies
# (Java runtime, libraries, OS utilities) together into a standardized unit called
# a container. This ensures the app runs EXACTLY the same way on every machine.
#
# MULTI-STAGE BUILD PATTERN:
# --------------------------
# Notice we have TWO 'FROM' statements. This is a multi-stage build:
# 1. Builder Stage: Uses a heavy Maven/JDK image to build the app from source code.
# 2. Runner Stage: Uses a lightweight JRE/JDK image to run the compiled .jar file.
# WHY? The final image won't contain Maven or our source code — only the compiled app.
# This makes the final image much smaller and more secure.

# ---------------------------------------------------------
# STAGE 1: BUILD THE APPLICATION
# ---------------------------------------------------------
# We use the official Maven image which has JDK 21 pre-installed.
# 'AS builder' names this stage so we can reference it later.
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container to /app
WORKDIR /app

# Copy ONLY the pom.xml first.
# WHY? Docker caches layers. If we copy source code too early, every code change
# would invalidate the maven dependency download cache.
COPY pom.xml .

# Download all dependencies into the local .m2 cache inside the container.
# -B means batch mode (less log output).
# Because we only copied pom.xml so far, this step will be CACHED by Docker
# on subsequent builds unless pom.xml itself changes!
RUN mvn dependency:go-offline -B

# NOW copy the actual source code.
COPY src ./src

# Compile the application and package it into a .jar file.
RUN mvn clean package

# ---------------------------------------------------------
# STAGE 2: RUN THE APPLICATION
# ---------------------------------------------------------
# We start a NEW, clean image based just on the JDK 21.
# It does not have Maven or our source code.
FROM eclipse-temurin:21-jdk AS runner

WORKDIR /app

# Copy the compiled .jar file from the 'builder' stage into this stage.
COPY --from=builder ./app/target/billing-service-0.0.1-SNAPSHOT.jar ./app.jar

# EXPOSE is just documentation. It tells developers and Docker that the app
# inside this container will be listening on port 4001 for HTTP and 9001 for gRPC.
# It does NOT publish the port to the host machine automatically (-p does that).
EXPOSE 4001
EXPOSE 9001

# ENTRYPOINT defines the default command to run when the container starts.
# We are simply running: java -jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
