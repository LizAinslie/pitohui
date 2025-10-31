package dev.lizainslie.pitohui.core.platforms

import dev.lizainslie.pitohui.core.config.Configs
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

abstract class PlatformAdapterFactory<TConfig: Any, TPlatform: PlatformAdapter<TConfig>> {
    private lateinit var platform: TPlatform
    private fun loadConfig(): TConfig = Configs.loadPlatformConfig(key, configSerializer)

    // unlike for `key` this can fetch the displayName from the `platform` instance
    val displayName: String get() = platform.displayName
    var loaded = false
        private set

    abstract fun initialize(config: TConfig): TPlatform
    abstract val configSerializer: KSerializer<TConfig>
    abstract val key: PlatformKey
    abstract val platformClass: KClass<out TPlatform>

    fun load() {
        platform = initialize(loadConfig())
        loaded = true
    }
    fun get(): TPlatform = platform
}