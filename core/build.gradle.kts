plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.mavenPublish)
}

version = "0.0.1"

dependencies {
    implementation(libs.bundles.exposed)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.slf4jApi)
    implementation(libs.bundles.okhttp)
    implementation(libs.okio)

    testImplementation(kotlin("test"))
}

publishing {
    repositories {
        maven {
            name = "lizAinslie"

            url = uri(
                if (version.toString().endsWith("SNAPSHOT"))
                    "https://repo.lizainslie.dev/repository/maven-snapshots/"
                else "https://repo.lizainslie.dev/repository/maven-releases/"
            )

            credentials(PasswordCredentials::class)
        }
    }

    mavenPublishing {
        coordinates("dev.lizainslie.pitohui", "pitohui-core", version.toString())

        pom {
            name.set("Pitohui Core")
            description.set("Core functionality for Pitohui, a multi-platform bot framework written in Kotlin")
            inceptionYear.set("2025")
            url.set("https://git.lizainslie.dev/crack-cafe/pitohui/")
            licenses {
                license {
                    name.set("The MIT License (MIT)")
                    url.set("https://opensource.org/license/mit")
                    distribution.set("https://opensource.org/license/mit")
                }
            }
            developers {
                developer {
                    id.set("mey")
                    name.set("Mey Ainslie")
                    url.set("https://git.lizainslie.dev/mey/")
                }
            }
            scm {
                url.set("https://github.com/username/mylibrary/")
                connection.set("scm:git:git://git.lizainslie.dev/crack-cafe/pitohui.git")
                developerConnection.set("scm:git:ssh://git@gitl.izainslie.dev/crack-cafe/pitohui.git")
            }
        }
    }
}
