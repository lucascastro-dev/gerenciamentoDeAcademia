FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copia arquivos do Maven primeiro para aproveitar cache
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn

# Permissão do wrapper + conversão de quebra de linha
RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

# Baixa dependências antes de copiar o código
RUN ./mvnw dependency:go-offline

# Copia código fonte
COPY src ./src

# Build com logs detalhados
RUN ./mvnw clean package -DskipTests -X

# =====================================================

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cria usuário sem privilégios
RUN addgroup -S app && adduser -S app -G app

# Copia jar gerado
COPY --from=build /app/target/*.jar app.jar

# Diretório de certificados
RUN mkdir -p /app/certificados && chown -R app:app /app

USER app

EXPOSE 8000

# Melhor visibilidade de logs Java
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]