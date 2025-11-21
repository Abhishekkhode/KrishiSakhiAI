# --- 1. BUILDER STAGE ---
FROM eclipse-temurin:21-jdk-jammy AS builder

# Set the current working directory inside the container
WORKDIR /app

# Copy the Maven pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application using Maven. The '-DskipTests' flag skips running tests.
# The 'clean package' command creates the final JAR file in 'target/'
RUN ./mvnw clean package -DskipTests

# --- 2. RUNTIME STAGE ---
# Use a lightweight JRE image for the final runtime environment
FROM eclipse-temurin:21-jre-jammy

# Set the current working directory
WORKDIR /app

# Copy the built JAR file from the builder stage
# Assuming your JAR name is target/AI-0.0.1-SNAPSHOT.jar (adjust if necessary)
COPY --from=builder /app/target/AI-*.jar krishisakhi.jar

# Define the entry point to run the application
# This command automatically reads environment variables for configuration.
ENTRYPOINT ["java", "-jar", "krishisakhi.jar"]