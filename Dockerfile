# ✅ Multi-stage build (Best Practice)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY target/sharehub-0.0.1-SNAPSHOT.jar app.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]