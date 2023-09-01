FROM eclipse-temurin:17.0.8.1_1-jdk-jammy

RUN mkdir -p /app
COPY base/server/target/base-server-jar-with-dependencies.jar /app/

CMD ["java", "-jar", "/app/base-server-jar-with-dependencies.jar"]