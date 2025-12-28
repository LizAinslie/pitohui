package dev.lizainslie.pitohui.modules.greeting.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable

object CommunityGreetingSettingsTable : CompositeIdTable("community_greeting_settings") {
    val platform = varchar("platform", 32).entityId()
    val communityId = varchar("platform_id", 255).entityId()
    val greetingMessage = varchar("greeting_message", 512)

    override val primaryKey = PrimaryKey(platform, communityId)

}