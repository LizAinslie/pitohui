package dev.lizainslie.pitohui.core.modules

import kotlinx.serialization.Serializable

@Serializable
data class ModuleManifest(
    val mainClass: String,
)
