# Build stage - Gradle ile derleme
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Gradle wrapper ve build dosyalarını kopyala
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle* ./

# Kaynak kodu kopyala
COPY src src

# WSDL generate + JAR oluştur (test atlanır, ağ erişimi WSDL için gerekir)
RUN ./gradlew bootJar --no-daemon -x test

# Run stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Build aşamasından JAR'ı kopyala (Spring Boot tek JAR)
COPY --from=build /app/build/libs/*.jar app.jar

# Varsayılan: port 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
