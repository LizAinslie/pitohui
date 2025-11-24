plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.shadow)

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}
dependencies {
    implementation(project(":core"))
    implementation(project(":platforms:discord"))
    implementation(project(":modules:system"))
    implementation(project(":modules:admin"))

    implementation(libs.bundles.kord)
    implementation(libs.bundles.exposed)
    implementation(libs.bundles.logging)

    implementation(libs.postgresql)
    implementation(libs.clikt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "dev.lizainslie.pitohui.MainKt"
}
