package dev.lizainslie.pitohui.core.manual

import kotlinx.serialization.Serializable

@Serializable
data class ManualPage(
    val title: String,
    val sections: List<ManualPageSection>,
    val image: String? = null,
    val summary: String? = null,
)