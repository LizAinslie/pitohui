package dev.lizainslie.pitohui.core.config

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess

object Configs {
    fun getBaseDir() = File(Bot::class.java.protectionDomain.codeSource.location.path).parentFile

    fun getConfigDir() = getBaseDir()
        .resolve("config")
        .apply { if (!exists()) mkdirs() }

    fun getPlatformsConfigDir() = getConfigDir()
        .resolve("platforms")
        .apply { if (!exists()) mkdirs() }


    fun <T : Any> loadPlatformConfig(key: PlatformKey, serializer: KSerializer<T>): T {
        val configFile = getPlatformsConfigDir().resolve(key.key)
        if (!configFile.exists()) {
            System.err.println("Config file ${configFile.absolutePath} does not exist, but platform ${key.key} is enabled. Please create the file and try again.")
            exitProcess(1)
        }

        return Json.decodeFromString(serializer, configFile.readText())
    }

    fun buildConfigStructures() {

    }
}