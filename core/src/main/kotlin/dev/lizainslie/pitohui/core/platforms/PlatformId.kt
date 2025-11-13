package dev.lizainslie.pitohui.core.platforms

data class PlatformId(
    val platform: PlatformKey,
    val id: String
) {
    override fun toString() = "[$platform] $id"
}
