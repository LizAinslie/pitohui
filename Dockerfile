FROM gradle:8.14.2-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew bot:assembleShadowDist --no-daemon

FROM openjdk:21-slim

RUN mkdir /app

COPY --from=build /home/gradle/src/bot/build/libs/bot-all.jar /app/bot.jar
COPY config.example.json /app/config.json

ENTRYPOINT ["java", "-jar", "/app/bot.jar", "--config=/app/config.json"]
