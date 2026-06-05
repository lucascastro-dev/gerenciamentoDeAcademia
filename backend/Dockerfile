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

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/certificados && chown -R app:app /app

USER app

EXPOSE 8000

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
