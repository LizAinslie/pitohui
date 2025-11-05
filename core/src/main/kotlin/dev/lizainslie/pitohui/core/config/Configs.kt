package dev.lizainslie.pitohui.core.config

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess

object Configs {
    private val json = Json {
        encodeDefaults = true
    }

    fun getBaseDir() = File(Bot::class.java.protectionDomain.codeSource.location.path).parentFile

    fun getConfigDir() = getBaseDir()
        .resolve("config")

    fun getBotConfigFile() = getConfigDir().resolve("config.json")

    fun getPlatformsConfigDir() = getConfigDir()
        .resolve("platforms")

    fun getPlatformConfigFile(key: PlatformKey) = getPlatformsConfigDir().resolve("${key.key}.json")

    fun <TConfig : ConfigBase> loadPlatformConfig(key: PlatformKey, serializer: KSerializer<TConfig>, createDefault: () -> TConfig): TConfig {
        val configFile = getPlatformConfigFile(key)

        if (!configFile.exists()) {
            // create & write default
            configFile.createNewFile()
            configFile.writeText(json.encodeToString(serializer, createDefault()))

            // then warn & exit
            println("Platform config file ${configFile.absolutePath} does not exist, but platform ${key.key} is enabled. A new default config has been generated. Please fill it out and try again.")
            exitProcess(1)
        }

        return json.decodeFromString(serializer, configFile.readText())
    }

    fun generateBaseStructure() {
        var shouldExit = false;

        // create the config directory if it doesn't exist
        if (!getConfigDir().exists())
            getConfigDir().mkdirs()

        val botConfig = getBotConfigFile()
        if (!botConfig.exists()) {
            shouldExit = true

            // generate a default
            botConfig.createNewFile()
            botConfig.writeText(
                json.encodeToString( BotConfig()),
                Charsets.UTF_8
            )

            // print a warning
            println(
                """Main config was not present at ${botConfig.absolutePath}.
                    |A new default config has been generated. Please fill it out before continuing.""".trimMargin())
        }

        if (!getPlatformsConfigDir().exists())
            getPlatformsConfigDir().mkdirs()

        if (shouldExit) exitProcess(1)
    }
}