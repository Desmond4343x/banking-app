# Stage 1: Build the jar using Maven
FROM maven:3.8.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run the app with JDK alpine image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# This is just documentation — Render scans for the port your app listens to
EXPOSE 8080

# This is the fix: dynamically bind the Render-provided PORT
CMD ["sh", "-c", "java -jar -Dserver.port=$PORT app.jar"]
