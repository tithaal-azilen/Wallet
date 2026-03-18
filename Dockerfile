# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
# Copy the maven wrapper and source code
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
# Build the application, skipping tests to speed up the process
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar
# Expose the port the app runs on
EXPOSE 8081
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
