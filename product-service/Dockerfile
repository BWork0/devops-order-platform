FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.31.1/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/opentelemetry-javaagent.jar", "-jar", "app.jar"]