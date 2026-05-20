FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar file
COPY --from=build /app/target/tour-1.0.0.jar ./app.jar

# Copy static configuration files needed at runtime
COPY packages_media.json .
COPY settings.json .

# Expose port (Render sets PORT env variable automatically, defaults to 3000 in our properties)
EXPOSE 3000

# Run the application
CMD ["java", "-jar", "app.jar"]
