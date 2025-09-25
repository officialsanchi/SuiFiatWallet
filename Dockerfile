# Use Maven to build the app
FROM maven:3.8.7 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use OpenJDK to run the app
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Use the PORT environment variable provided by the platform
ENV PORT=1946
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=$PORT app.jar"]
