FROM eclipse-temurin:17-jdk-alpine
COPY build/libs/vote_service-1.0.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]