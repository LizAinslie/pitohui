package dev.lizainslie.pitohui.modules.vcnotify.data

import kotlinx.datetime.Instant

data class VcNotifyRecord(
    val time: Instant,
    val user: String, // todo: switch to PlatformUser, but a username is sufficient for now
)
