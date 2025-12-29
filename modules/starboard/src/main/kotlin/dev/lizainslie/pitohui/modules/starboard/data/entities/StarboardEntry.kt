package dev.lizainslie.pitohui.modules.starboard.data.entities

import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.modules.starboard.data.tables.StarboardEntryTable
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class StarboardEntry(id: EntityID<CompositeID>) : CompositeEntity(id) {
    val platform by StarboardEntryTable.platform
    val communityId by StarboardEntryTable.communityId
    val starboardChannelId by StarboardEntryTable.starboardChannelId
    val starboardChannelMessageId by StarboardEntryTable.starboardChannelMessageId
    val messageId by StarboardEntryTable.messageId
    var channelId by StarboardEntryTable.channelId
    var starCount by StarboardEntryTable.starCount

    companion object : CompositeEntityClass<StarboardEntry>(StarboardEntryTable) {
        fun create(
            communityId: PlatformId,
            starboardChannelId: PlatformId,
            starboardChannelMessageId: String,
            channelId: PlatformId,
            messageId: PlatformId,
            starCount: Int = 0,
        ) = new(CompositeID { id ->
            id[StarboardEntryTable.platform] = communityId.platform.key
            id[StarboardEntryTable.communityId] = communityId.id
            id[StarboardEntryTable.starboardChannelId] = starboardChannelId.id
            id[StarboardEntryTable.starboardChannelMessageId] = starboardChannelMessageId
            id[StarboardEntryTable.messageId] = messageId.id
        }) {
            this.starCount = starCount
            this.channelId = channelId.id
        }

        fun findByMessageId(
            communityId: PlatformId,
            starboardChannelId: PlatformId,
            messageId: PlatformId,
        ) = find {
            (StarboardEntryTable.platform eq communityId.platform.key) and
            (StarboardEntryTable.communityId eq communityId.id) and
            (StarboardEntryTable.starboardChannelId eq starboardChannelId.id) and
            (StarboardEntryTable.messageId eq messageId.id)
        }.firstOrNull()

        fun findByStarboardMessageId(
            communityId: PlatformId,
            starboardChannelId: PlatformId,
            starboardChannelMessageId: PlatformId,
        ) = find {
            (StarboardEntryTable.platform eq communityId.platform.key) and
            (StarboardEntryTable.communityId eq communityId.id) and
            (StarboardEntryTable.starboardChannelId eq starboardChannelId.id) and
            (StarboardEntryTable.starboardChannelMessageId eq starboardChannelMessageId.id)
        }.firstOrNull()
    }
}