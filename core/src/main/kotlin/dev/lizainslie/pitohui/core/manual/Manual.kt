package dev.lizainslie.pitohui.core.manual

import kotlinx.serialization.Serializable

@Serializable
data class Manual(
    val identifier: String,
    val title: String,
    val pages: List<ManualPage>
)