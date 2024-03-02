FROM openjdk:17-jdk

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo Asia/Seoul > /etc/timezone

WORKDIR /app

COPY motticle-web-0.0.1-SNAPSHOT.jar motticle.jar

ENTRYPOINT ["java", "-jar", "motticle.jar"]
