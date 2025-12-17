package dev.lizainslie.pitohui.modules.vcdisconnect.data

import dev.lizainslie.pitohui.core.platforms.PlatformId
import kotlinx.datetime.Instant

data class DisconnectTimer(
    val user: PlatformId,
    var channel: PlatformId,
    val disconnectAt: Instant,
)
