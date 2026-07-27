# Etapa 1: Construcción (Builder)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
# Copiar el wrapper de Maven y los archivos de configuración
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
# Dar permisos de ejecución al wrapper y compilar el proyecto omitiendo los tests
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests
# Etapa 2: Imagen final para producción (ligera)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiar solo el archivo .jar generado en la etapa anterior
COPY --from=builder /app/target/*.jar app.jar
# Exponer el puerto (por defecto Spring Boot usa 8080)
EXPOSE 8080
# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]