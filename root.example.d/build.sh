#!/usr/bin/env bash

cd ..

./gradlew bot:assembleShadowDist

./gradlew modules:autorole:jar
./gradlew modules:vcnotify:jar

cd root.example.d