# Image d'un microservice. Construire d'abord les jars : mvn -DskipTests package
# puis : docker compose up --build
FROM eclipse-temurin:17-jre
ARG SERVICE
RUN useradd --system --uid 1001 ecivil
WORKDIR /app
COPY services/${SERVICE}/target/${SERVICE}.jar app.jar
USER ecivil
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
