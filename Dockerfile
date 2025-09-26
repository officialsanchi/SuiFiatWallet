# --- Build stage ---
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
# only copy what rebuilds less often first (speeds caching)
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app
# copy jar produced by maven build (adjust path if different)
COPY --from=build /app/target/*.jar app.jar

# Expose a port for clarity (Render uses PORT env var at runtime)
EXPOSE 8080

# set the JVM to use the PORT env var from Render
# server.port=$PORT ensures Spring uses Render's assigned port
ENTRYPOINT ["sh", "-c", "java -Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom -Dserver.port=$PORT -jar /app/app.jar"]
