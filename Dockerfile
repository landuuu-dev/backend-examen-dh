# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiamos archivos necesarios primero (esto permite cache de dependencias)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias
RUN ./mvnw -q dependency:go-offline

# Ahora copiamos el código
COPY src ./src

# Construimos la app
RUN ./mvnw -q clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el .jar compilado
COPY --from=builder /app/target/*.jar app.jar

# Puerto que expone Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
