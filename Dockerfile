# Use an official Maven image as the base image
FROM maven:3.9.5-amazoncorretto-21 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src

# Build the application using Maven
RUN mvn clean package -DskipTests

# Java 21 kullanarak bir temel imajdan başla
FROM openjdk:21-slim

# Uygulamayı çalıştıracağımız dizini oluştur
WORKDIR /app

# Maven build sonucu oluşan JAR dosyasını kopyala (Örnek: my-app.jar)
COPY --from=build /app/target/*.jar app.jar

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"]