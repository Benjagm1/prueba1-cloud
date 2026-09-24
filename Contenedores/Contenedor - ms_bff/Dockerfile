# Etapa 1: Build con Maven y JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar únicamente el módulo ms-bff
COPY Stack/Backend/ms-bff ./

# Descargar dependencias y compilar el JAR de forma independiente
RUN mvn clean package -DskipTests

# Etapa 2: Runtime ligero
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]gi