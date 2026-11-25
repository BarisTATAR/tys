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

# WSDL'den sınıf üretimi + JAR (testler ayrı koşulur). KBS WSDL'i repodadır; ağ yalnız
# Gradle bağımlılıklarını indirmek için gerekir.
# gradlew Windows kopyasından gelirse satır sonları CRLF, git'ten gelirse çalıştırma izni
# eksik olabilir: ikisi de derlemeden önce düzeltilir.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# Run stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Build aşamasından JAR'ı kopyala (Spring Boot tek JAR)
COPY --from=build /app/build/libs/*.jar app.jar

# Varsayılan: port 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
