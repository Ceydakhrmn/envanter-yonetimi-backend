# Temel imaj
FROM eclipse-temurin:21-jdk

# Çalışma dizini oluştur
WORKDIR /app

# Maven wrapper ve proje dosyalarını kopyala
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Tüm kaynak kodunu kopyala ve derle
COPY . .
RUN ./mvnw clean package -DskipTests

# Uygulamayı başlat (sh -c ile glob desteği)
CMD ["sh", "-c", "java -jar target/*.jar"]
