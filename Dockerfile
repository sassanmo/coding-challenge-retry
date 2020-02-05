FROM 3.6.3-jdk-11 AS MAVEN_BUILD
COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build/
RUN mvn package

FROM openjdk:11-jdk-alpine
EXPOSE 8080
WORKDIR /app
COPY --from=MAVEN_BUILD /build/target/retry-0.0.1-SNAPSHOT.jar /app/
ENTRYPOINT ["java", "-jar", "retry-0.0.1-SNAPSHOT.jar"]