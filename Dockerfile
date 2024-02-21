FROM openjdk:17-alpine

WORKDIR /motticle

COPY motticle-0.0.1-SNAPSHOT.jar motticle.jar

ENTRYPOINT ["java", "jar", "motticle.jar"]
