FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

# Copiar el proyecto ms-bff con su propio Maven Wrapper
COPY Stack/Backend/ms-bff /build

RUN chmod +x ./mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline
RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && addgroup --system gea && adduser --system --ingroup gea gea
USER gea

COPY --from=builder /build/target/*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]