package dev.lizainslie.pitohui.core.config

import dev.lizainslie.pitohui.core.fs.BotFS
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Config<TConfig : ConfigBase>(
    val key: String,
    private val serializer: KSerializer<out TConfig>,
    klass: KClass<out TConfig>,
    val moduleName: String? = null
) {
    lateinit var currentValue: TConfig
        private set

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        // todo: define some other defaults
    }

    private val log = LoggerFactory.getLogger(javaClass)

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
        log.info("${if (::currentValue.isInitialized) "Rel" else "L"}oading config with key '$key' from $file")
        val contents = file.readText(Charsets.UTF_8)
        val configValue = json.decodeFromString(serializer, contents)

        // todo: make this error better and  refactor validation system to
        //          return better errors
        if (!configValue.validate())
            throw RuntimeException("Invalid config value in $file:\n $contents")

        currentValue = configValue

        log.debug("Calling onLoad hook for config with key '$key'")
        currentValue.onLoad()
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