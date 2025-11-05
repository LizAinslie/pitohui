plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":platforms:discord"))
    implementation(project(":modules:system"))

    implementation(libs.kord.core)
    implementation(libs.bundles.exposed)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}
