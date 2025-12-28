dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// Bot & core
include(":bot")
include(":core")

// Platform integrations
include(":platforms:discord")

// Modules
include(":modules:system")
include(":modules:starboard")
include(":modules:greeting")
include(":modules:admin")
include(":modules:vcnotify")
include(":modules:vcdisconnect")
include(":modules:autorole")
include(":modules:message-embedder")

rootProject.name = "pitohui"
