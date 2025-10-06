FROM eclipse-temurin:17-jre-alpine AS runtime

# Set the expected JAR path. If your artifactId/version differs, update the path.
ARG JAR_FILE=target/shoes-backend-1.0.0.jar

WORKDIR /app
COPY ${JAR_FILE} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]