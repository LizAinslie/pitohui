package dev.lizainslie.pitohui.core.platforms.entities

interface PlatformResponse {
    suspend fun edit(newContent: String)
    suspend fun createFollowup(content: String)
    suspend fun createPrivateFollowup(content: String)
}