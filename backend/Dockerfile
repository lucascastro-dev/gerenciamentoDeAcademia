# syntax=docker/dockerfile:1
FROM public.ecr.aws/docker/library/eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache ca-certificates bash

COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp dependency:resolve dependency:resolve-plugins -DskipTests

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp clean package -DskipTests

FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/certificados && chown -R app:app /app

USER app

EXPOSE 8000

ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=20s --timeout=8s --start-period=60s --retries=6 \
  CMD curl -fsS http://localhost:8000/srv-gerenciaracademia/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
