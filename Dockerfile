# --- build stage ---
FROM maven:3.8.8-jdk-17 AS builder
WORKDIR /workspace

# copy mvnw & pom to leverage cache if using wrapper; otherwise adjust
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src ./src

# Build (skip tests to speed it up; remove -DskipTests to run tests)
RUN ./mvnw -B -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar

# Informational; Render uses the PORT env var
EXPOSE 10000

# Allow memory tuning via JAVA_OPTS and bind server to $PORT
ENV JAVA_OPTS=""
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -Dserver.address=0.0.0.0 -jar /app/app.jar"]
