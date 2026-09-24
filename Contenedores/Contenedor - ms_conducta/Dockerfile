FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

COPY Stack/Backend /build

RUN chmod +x ./mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline
RUN ./mvnw -q -DskipTests -Pdocker -pl ms-conducta -am package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/* \
	&& addgroup --system gea && adduser --system --ingroup gea gea
USER gea
COPY --from=builder /build/ms-conducta/target/ms-conducta.jar ./app.jar
EXPOSE 8055
ENTRYPOINT ["java", "-jar", "app.jar"]
