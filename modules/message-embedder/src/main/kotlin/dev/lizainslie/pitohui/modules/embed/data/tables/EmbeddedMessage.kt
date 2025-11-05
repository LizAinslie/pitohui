package dev.lizainslie.pitohui.modules.embed.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object EmbeddedMessageTable : CompositeIdTable("embedded_messages") {
    val communityId = ulong("community_id").entityId()
    val channelId = ulong("channel_id").entityId()
    val messageId = ulong("message_id").entityId()

    val title = varchar("title", 255)
    val description = text("description")
    val color = integer("color")
    val thumbnailUrl = varchar("thumbnail_url", 255)
    val imageUrl = varchar("image_url", 255)
    val authorName = varchar("author_name", 255)
    val authorUrl = varchar("author_url", 255)
    val authorIconUrl = varchar("author_icon_url", 255)
    val footerText = varchar("footer_text", 255)
    val footerIconUrl = varchar("footer_icon_url", 255)
    val timestamp = datetime("timestamp")

    override val primaryKey = PrimaryKey(communityId)
}
