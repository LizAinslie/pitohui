package dev.lizainslie.pitohui.core.platforms.entities

import dev.lizainslie.pitohui.core.platforms.PlatformId

interface PlatformMessage {
    val content: String
    val authorId: PlatformId
}