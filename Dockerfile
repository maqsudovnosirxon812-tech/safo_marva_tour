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

# Expose port
EXPOSE 3000

# Keep enough headroom for native memory on Render's 512 MB instance.
CMD ["java", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=55", "-XX:InitialRAMPercentage=15", "-XX:MaxMetaspaceSize=128m", "-Xss512k", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
