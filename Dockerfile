FROM eclipse-temurin:21-jdk

# instala ffmpeg
RUN apt-get update \
 && apt-get install -y ffmpeg \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

EXPOSE 8080
CMD ["sh", "-c", "java -jar target/*.jar"]