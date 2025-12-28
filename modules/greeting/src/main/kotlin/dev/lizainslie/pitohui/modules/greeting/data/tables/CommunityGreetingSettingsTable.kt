package dev.lizainslie.pitohui.modules.greeting.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable

object CommunityGreetingSettingsTable : CompositeIdTable("community_greeting_settings") {
    val platform = varchar("platform", 32).entityId()
    val communityId = varchar("platform_id", 255).entityId()

    // channels
    val welcomeChannelId = varchar("welcome_channel_id", 255).nullable().default(null)
    val goodbyeChannelId = varchar("goodbye_channel_id", 255).nullable().default(null)

    // messages
    val welcomeMessage = varchar("welcome_message", 512).nullable().default(null)
    val goodbyeMessage = varchar("goodbye_message", 512).nullable().default(null)

    // colors
    val welcomeColor = varchar("welcome_color", 16).nullable().default(null)
    val goodbyeColor = varchar("goodbye_color", 16).nullable().default(null)

    // images
    val welcomeImageUrl = varchar("welcome_image_url", 1024).nullable().default(null)
    val goodbyeImageUrl = varchar("goodbye_image_url", 1024).nullable().default(null)

    // embed toggles
    val embedWelcome = bool("embed_welcome").default(false)
    val embedGoodbye = bool("embed_goodbye").default(false)

    override val primaryKey = PrimaryKey(platform, communityId)
}