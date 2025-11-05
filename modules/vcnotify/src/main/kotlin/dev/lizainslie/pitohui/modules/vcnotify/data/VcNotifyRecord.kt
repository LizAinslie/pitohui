package dev.lizainslie.pitohui.modules.vcnotify.data

import dev.lizainslie.pitohui.core.platforms.PlatformId
import kotlinx.datetime.Instant

data class VcNotifyRecord(
    val time: Instant,
    val user: PlatformId
)
