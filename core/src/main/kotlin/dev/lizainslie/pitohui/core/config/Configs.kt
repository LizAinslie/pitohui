package dev.lizainslie.pitohui.core.config

import dev.lizainslie.pitohui.core.fs.BotFS
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.reflect.KClass
import kotlin.system.exitProcess

object Configs {
    private val json = Json {
        encodeDefaults = true
    }

    val loadedConfigs = mutableListOf<Config<*>>()

    fun <TConfig : ConfigBase> getConfigFileAnnotation(klass: KClass<out TConfig>): ConfigFile {
        val annotations = klass.annotations.filterIsInstance<ConfigFile>()
        if (annotations.size != 1) {
            // todo: error.
            throw RuntimeException("${klass.simpleName} must have exactly one ConfigFile annotation!")
        }

        return annotations.first()
    }

    fun <TConfig : ConfigBase> getConfigKey(klass: KClass<out TConfig>) =
        getConfigFileAnnotation(klass).key

    inline fun <reified TConfig : ConfigBase> getConfigKey() = getConfigKey(TConfig::class)

    @Suppress("UNCHECKED_CAST")
    inline fun <reified TConfig : ConfigBase> config(): Config<TConfig> {
        val key = getConfigKey<TConfig>()
        if (loadedConfigs.any { it.key == key })
            return loadedConfigs.first { it.key == key } as Config<TConfig>
        else {
            val config = Config<TConfig>(key)
            loadedConfigs += config
            return config
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified TConfig : ConfigBase> moduleConfig(moduleName: String): Config<TConfig> {
        val key = getConfigKey<TConfig>()
        if (loadedConfigs.any { it.key == key })
            return loadedConfigs.first { it.key == key } as Config<TConfig>
        else {
            val config = Config<TConfig>(key, moduleName)
            loadedConfigs += config
            return config
        }
    }

//    fun getBotConfigFile() = BotFS.configDir.resolve("config.json")
//
//    fun getPlatformConfigFile(key: PlatformKey) = BotFS.platformConfigDir.resolve("${key.key}.json")

//    fun <TConfig : ConfigBase> loadPlatformConfig(key: PlatformKey, serializer: KSerializer<TConfig>, createDefault: () -> TConfig): TConfig {
//        val configFile = getPlatformConfigFile(key)
//
//        if (!configFile.exists()) {
//            // create & write default
//            configFile.createNewFile()
//            configFile.writeText(json.encodeToString(serializer, createDefault()))
//
//            // then warn & exit
//            println("Platform config file ${configFile.absolutePath} does not exist, but platform ${key.key} is enabled. A new default config has been generated. Please fill it out and try again.")
//            exitProcess(1)
//        }
//
//        return json.decodeFromString(serializer, configFile.readText())
//    }

    fun checkConfigs() {

        // todo: create new check logic
//        var shouldExit = false
//        val botConfig = getBotConfigFile()
//
//        if (!botConfig.exists()) {
//            shouldExit = true
//
//            // generate a default
//            botConfig.createNewFile()
//            botConfig.writeText(
//                json.encodeToString( BotConfig()),
//                Charsets.UTF_8
//            )
//
//            // print a warning
//            println(
//                """Main config was not present at ${botConfig.absolutePath}.
//                    |A new default config has been generated. Please fill it out before continuing.""".trimMargin())
//        }
//
//        if (shouldExit) exitProcess(1)
    }
}