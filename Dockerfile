# Use an official Maven image as the base image
FROM maven:3.8.4-openjdk-21-slim AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src

# Build the application using Maven
RUN mvn clean package -DskipTests

# Java 17 kullanarak bir temel imajdan başla
FROM openjdk:21

# Uygulamayı çalıştıracağımız dizini oluştur
WORKDIR /app

# Maven build sonucu oluşan JAR dosyasını kopyala (Örnek: my-app.jar)
COPY --from=build /app/target/*.jar app.jar

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"]