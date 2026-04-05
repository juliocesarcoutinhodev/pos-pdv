FROM eclipse-temurin:25-jre AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN ./mvnw dependency:go-offline -B
COPY src/ src/
RUN ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

USER nobody:nogroup

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
