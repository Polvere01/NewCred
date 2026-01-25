# ===== BUILD =====
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# ===== RUNTIME =====
FROM eclipse-temurin:21-jre

# instala ffmpeg
RUN apt-get update \
 && apt-get install -y ffmpeg \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:InitialRAMPercentage=25 -XX:+UseG1GC"

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
