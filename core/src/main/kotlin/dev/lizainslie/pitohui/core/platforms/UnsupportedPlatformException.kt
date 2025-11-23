package dev.lizainslie.pitohui.core.platforms

class UnsupportedPlatformException(
    val currentPlatform: PlatformAdapter,
    vararg val allowedPlatforms: PlatformAdapter
) : Exception(
    "Unsupported platform ${currentPlatform.displayName}. Allowed platforms: ${allowedPlatforms.joinToString(", ") { it.displayName }}."
)