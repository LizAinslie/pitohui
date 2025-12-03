package dev.lizainslie.pitohui.core.platforms.entities

import dev.lizainslie.pitohui.core.commands.CommandContext

interface PlatformResponse {
    val context: CommandContext

    suspend fun edit(newContent: String)
    suspend fun createFollowup(content: String): PlatformResponse
    suspend fun createPrivateFollowup(content: String): PlatformResponse
    suspend fun createStealthFollowup(content: String): PlatformResponse
}