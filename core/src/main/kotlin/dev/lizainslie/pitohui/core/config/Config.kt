package dev.lizainslie.pitohui.core.config

import dev.lizainslie.pitohui.core.fs.BotFS
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Config<TConfig : ConfigBase>(
    val key: String,
    private val serializer: KSerializer<out TConfig>,
    private val klass: KClass<out TConfig>,
    val moduleName: String? = null
) {
    lateinit var currentValue: TConfig
        private set

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        // todo: define some other defaults
    }

    private val annotation = Configs.getConfigFileAnnotation(klass)

    private val file: File = when (annotation.type) {
        ConfigType.MODULE -> {
            if (moduleName == null) throw RuntimeException("Error loading module config ${klass.simpleName}: moduleName is null")
            BotFS.moduleConfigDir.resolve(moduleName).resolve(annotation.name)
        }
        ConfigType.ROOT -> BotFS.configDir.resolve(annotation.name)
        ConfigType.PLATFORM -> BotFS.platformConfigDir.resolve(annotation.name)
    }

    init {
        load()
    }

    fun load() {
        val contents = file.readText(Charsets.UTF_8)
        val configValue = json.decodeFromString(serializer, contents)

        // todo: make this error better and  refactor validation system to
        //          return better errors
        if (!configValue.validate())
            throw RuntimeException("Invalid config value in $file:\n $contents")

        currentValue = configValue
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): TConfig = currentValue
}

inline fun <reified TConfig : ConfigBase> Config(key: String, moduleName: String? = null) =
    Config(
        key = key,
        serializer = Json.serializersModule.serializer<TConfig>(),
        klass = TConfig::class,
        moduleName = moduleName
    )