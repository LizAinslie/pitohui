FROM gradle:8.14.2-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew bot:assembleShadowDist --no-daemon

FROM eclipse-temurin:21-jre-alpine-3.23

RUN mkdir -p /bot_root/config/platforms
RUN mkdir -p /bot_root/config/modules
RUN mkdir -p /bot_root/modules

COPY --from=build /home/gradle/src/bot/build/libs/bot-all.jar /bot_root/bot.jar

COPY ./root.example.d/config/logging.example.json /bot_root/config/logging.example.json
COPY ./root.example.d/config/database.example.json /bot_root/config/database.example.json
COPY ./root.example.d/config/command_parsing.example.json /bot_root/config/command_parsing.example.json
COPY ./root.example.d/config/platforms/discord.example.json /bot_root/config/platforms/discord.example.json

ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "/bot_root/bot.jar"]
