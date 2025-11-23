package dev.lizainslie.pitohui.core.platforms

import dev.lizainslie.pitohui.core.config.ConfigBase
import dev.lizainslie.pitohui.core.config.Configs
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.system.exitProcess

abstract class PlatformAdapterFactory<TPlatform: PlatformAdapter> {
    private lateinit var platform: TPlatform

    // unlike for `key` this can fetch the displayName from the `platform` instance
    val displayName: String get() = platform.displayName
    var loaded = false
        private set

    abstract fun initialize(): TPlatform
    abstract val key: PlatformKey
    abstract val platformClass: KClass<out TPlatform>

    fun load() {
        platform = initialize()
        loaded = true
    }

    fun get(): TPlatform = platform
}