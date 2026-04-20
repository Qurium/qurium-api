# Qurium API — Quarkus fast-jar (JVM), Java 25 (Eclipse Temurin).

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build

RUN apk add --no-cache bash

COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw \
    && ./mvnw -B -e -DskipTests package

FROM eclipse-temurin:25-jre-alpine

ENV LANGUAGE='en_US:en' \
    JAVA_OPTS_APPEND='-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager'

RUN addgroup -g 185 -S quarkus && adduser -u 185 -S quarkus -G quarkus

WORKDIR /deployments

COPY --from=build --chown=185:185 /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185:185 /build/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185:185 /build/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185:185 /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENTRYPOINT exec java $JAVA_OPTS_APPEND -jar /deployments/quarkus-run.jar
