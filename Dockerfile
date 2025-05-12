# ======== Build Stage ========
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copy all project files
COPY . .

# Package the shaded JAR
RUN mvn clean package

# ======== Run Stage ========
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/java-backend-1.0-SNAPSHOT-shaded.jar app.jar

# Run the application
CMD ["java", "-jar", "app.jar"]
