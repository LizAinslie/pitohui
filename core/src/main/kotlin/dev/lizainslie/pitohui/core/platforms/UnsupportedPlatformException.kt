package dev.lizainslie.pitohui.core.platforms

class UnsupportedPlatformException(
    val currentPlatform: PlatformAdapterFactory<*, *>,
    vararg val allowedPlatforms: PlatformAdapterFactory<*, *>
) : Exception(
    "Unsupported platform ${currentPlatform.displayName}. Allowed platforms: ${allowedPlatforms.joinToString(", ") { it.displayName }}."
)