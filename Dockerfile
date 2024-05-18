# Use an official OpenJDK runtime as a parent image for building the application
FROM openjdk:17-slim AS build

# Set the working directory in the Docker container
WORKDIR /app

# Copy the Gradle wrapper and project files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY .env .
COPY src src

# Grant execution permissions to the Gradlew script
RUN chmod +x ./gradlew

# Build the application using the Gradle wrapper
RUN ./gradlew build

# Use another official OpenJDK runtime as a parent image for the runtime
FROM openjdk:17-slim

# Set the working directory in the Docker container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar /app/chetai-telegram-bot.jar

# Copy the .env file to the runtime container
COPY .env .

# Run the application
CMD ["java", "-jar", "chetai-telegram-bot.jar"]
