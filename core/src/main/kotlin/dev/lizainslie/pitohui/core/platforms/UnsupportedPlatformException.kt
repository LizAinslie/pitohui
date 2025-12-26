package dev.lizainslie.pitohui.core.platforms

class UnsupportedPlatformException(
    val currentPlatform: AnyPlatformAdapter,
    vararg val allowedPlatforms: AnyPlatformAdapter
) : Exception(
    "Unsupported platform ${currentPlatform.displayName}. Allowed platforms: ${allowedPlatforms.joinToString(", ") { it.displayName }}."
)