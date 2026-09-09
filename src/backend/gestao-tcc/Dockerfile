# syntax=docker/dockerfile:1

# Build da aplicação
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia arquivos de configuração, garante permissão de execução e baixa dependências
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copia o código-fonte e gera o pacote
COPY src src
RUN ./gradlew bootJar --no-daemon

# Runtime: imagem apenas com JRE
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Usuário sem privilégios
RUN useradd --uid 1001 --create-home spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]