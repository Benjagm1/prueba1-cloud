# Etapa 1: Build con Maven y JDK 25
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar pom padre y módulos comunes requeridos
COPY Stack/Backend/pom.xml ./pom.xml
COPY Stack/Backend/ms-common-security ./ms-common-security
COPY Stack/Backend/ms-bff ./ms-bff

# Compilar solo ms-bff y sus dependencias
RUN mvn clean package -pl ms-bff -am -DskipTests

# Etapa 2: Runtime ligero
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/ms-bff/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]