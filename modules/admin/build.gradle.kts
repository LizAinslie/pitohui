plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    api(project(":core"))
    api(project(":modules:system"))
    api(project(":platforms:discord"))

    implementation(libs.kord.core)
    implementation(libs.bundles.exposed)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}
