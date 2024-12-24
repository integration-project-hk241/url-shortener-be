FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY pom.xml
COPY src ./src

COPY mvnw ./
COPY .mvn/ .mvn/
RUN chmod +x mvnw

# Install dependencies and build the JAR
RUN ./mvnw clean package -DskipTests

# Runtime image with only JRE
FROM eclipse-temurin:17-jre-jammy AS final

# Create a non-privileged user for security
ARG UID=10001
RUN adduser --disabled-password --gecos "" --home "/nonexistent" --shell "/sbin/nologin" --no-create-home --uid "${UID}" appuser
USER appuser

# Copy the JAR from the builder stage
COPY --from=builder /app/target/url-shortener-be-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port and define the entrypoint
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
