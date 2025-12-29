package dev.lizainslie.pitohui.modules.starboard.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable

object StarboardEntryTable : CompositeIdTable("starboard_entries") {
    val platform = varchar("platform", 255).entityId()
    val communityId = varchar("community_id", 255).entityId()
    val starboardChannelId = varchar("starboard_channel_id", 255).entityId()
    val starboardChannelMessageId = varchar("starboard_channel_message_id", 255).entityId()
    val messageId = varchar("message_id", 255).entityId()
    val channelId = varchar("channel_id", 255)
    val starCount = integer("star_count")

    override val primaryKey = PrimaryKey(platform, communityId, starboardChannelId, starboardChannelMessageId, messageId)
}