FROM maven:3.9.0-eclipse-temurin-20
WORKDIR /app
COPY src/ src/
COPY pom.xml .
RUN mvn clean package -DskipTests
EXPOSE 8080
CMD ["java","-jar","target/shoes-backend-1.0.0.jar"]
