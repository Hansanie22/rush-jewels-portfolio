# Build Stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY uploads ./uploads
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "-Xmx384m", "-Dserver.port=8080", "-Dserver.address=0.0.0.0", "-jar", "app.jar"]

