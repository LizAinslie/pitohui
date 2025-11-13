package dev.lizainslie.pitohui.core.platforms

@JvmInline
value class PlatformKey(val key: String) {
    override fun toString() = key.uppercase()
}