FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace

# Копируем root pom и pom'ы модулей для кеша зависимостей
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY hotel-service/pom.xml hotel-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml

RUN mvn -q -DskipTests dependency:go-offline

# Копируем исходники и собираем только нужный модуль (и зависимости)
COPY . .
RUN mvn -q -DskipTests -pl eureka-server -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/eureka-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]
