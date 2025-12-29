package dev.lizainslie.pitohui.modules.starboard.data.entities

import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.modules.starboard.data.tables.StarboardTable
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class Starboard(id: EntityID<CompositeID>) : CompositeEntity(id) {
    val platform by StarboardTable.platform
    val communityId by StarboardTable.communityId
    val channelId by StarboardTable.channelId
    var starThreshold by StarboardTable.starThreshold
    var emoji by StarboardTable.emoji
    var selfStarAllowed by StarboardTable.selfStarAllowed

    companion object : CompositeEntityClass<Starboard>(StarboardTable) {
        fun create(
            communityId: PlatformId,
            channelId: PlatformId,
            starThreshold: Int = StarboardTable.DEFAULT_STAR_THRESHOLD,
            emoji: String = StarboardTable.DEFAULT_EMOJI,
            selfStarAllowed: Boolean = false,
        ) = new(CompositeID { id ->
            id[StarboardTable.platform] = communityId.platform.key
            id[StarboardTable.communityId] = communityId.id
            id[StarboardTable.channelId] = channelId.id
        }) {
            this.starThreshold = starThreshold
            this.emoji = emoji
            this.selfStarAllowed = selfStarAllowed
        }

        fun findByReactionForCommunity(
            communityId: PlatformId,
            emoji: String,
        ) = find {
            (StarboardTable.platform eq communityId.platform.key) and
            (StarboardTable.communityId eq communityId.id) and
            (StarboardTable.emoji eq emoji)
        }.firstOrNull()

        fun findByCreateArgs(
            communityId: PlatformId,
            channelId: PlatformId,
        ) = find {
            (StarboardTable.platform eq communityId.platform.key) and
            (StarboardTable.communityId eq communityId.id) and
            (StarboardTable.channelId eq channelId.id)
        }.firstOrNull()

        fun existsByEmoji(
            communityId: PlatformId,
            emoji: String
        ) = find {
            (StarboardTable.platform eq communityId.platform.key) and
            (StarboardTable.communityId eq communityId.id) and
            (StarboardTable.emoji eq emoji)
        }.any()
    }
}