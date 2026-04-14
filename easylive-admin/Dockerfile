FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build
COPY pom.xml .
COPY easylive-common/pom.xml easylive-common/pom.xml
COPY easylive-web/pom.xml easylive-web/pom.xml
COPY easylive-admin/pom.xml easylive-admin/pom.xml
COPY easylive-common/src easylive-common/src
COPY easylive-admin/src easylive-admin/src

RUN mvn -pl easylive-admin -am clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/easylive-admin/target/easylive-admin-1.0.jar app.jar

EXPOSE 7070

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]

