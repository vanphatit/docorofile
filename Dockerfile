# Build stage
FROM maven:latest AS build
WORKDIR /app
COPY pom.xml /app/
COPY src /app/src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar /app/docorofile-app.jar
COPY uploads /app/uploads
EXPOSE 5001
ENTRYPOINT ["java", "-jar", "docorofile-app.jar"]
