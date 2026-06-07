FROM public.ecr.aws/docker/library/eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Certificados + shell: evita falhas de HTTPS e do mvnw no Alpine
RUN apk add --no-cache ca-certificates bash

COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

# go-offline falha com frequência (rede, plugins JaCoCo, etc.); resolve é mais estável
RUN ./mvnw -B -ntp dependency:resolve dependency:resolve-plugins -DskipTests

COPY src ./src
RUN ./mvnw -B -ntp clean package -DskipTests

FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/certificados && chown -R app:app /app

USER app

EXPOSE 8000

# Heap fixo + G1: evita consumir toda a RAM do host quando o container não tem limite
ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD curl -fsS http://localhost:8000/srv-gerenciaracademia/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
