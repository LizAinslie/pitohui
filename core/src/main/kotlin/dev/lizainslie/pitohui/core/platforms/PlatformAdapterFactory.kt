package dev.lizainslie.pitohui.core.platforms

import dev.lizainslie.pitohui.core.config.ConfigBase
import dev.lizainslie.pitohui.core.config.Configs
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.system.exitProcess

abstract class PlatformAdapterFactory<TConfig: ConfigBase, TPlatform: PlatformAdapter<TConfig>> {
    private lateinit var platform: TPlatform
    private fun loadConfig(): TConfig = Configs.loadPlatformConfig(key, configSerializer, ::createDefaultConfig)

    // unlike for `key` this can fetch the displayName from the `platform` instance
    val displayName: String get() = platform.displayName
    var loaded = false
        private set

    abstract fun initialize(config: TConfig): TPlatform
    abstract fun createDefaultConfig(): TConfig
    abstract val configSerializer: KSerializer<TConfig>
    abstract val key: PlatformKey
    abstract val platformClass: KClass<out TPlatform>

    fun load() {
        val config = loadConfig()

        if (!config.validate()) {
            println("Config for platform $displayName not valid at ${Configs.getPlatformConfigFile(key).absolutePath}. Cannot continue.")
            exitProcess(1)
        }

        platform = initialize(config)
        loaded = true
    }

    fun get(): TPlatform = platform
}