package dev.lizainslie.pitohui.modules.starboard.data

import dev.lizainslie.pitohui.core.platforms.PlatformId
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

const val DEFAULT_EMOJI = "star"
const val DEFAULT_STAR_THRESHOLD = 5

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
            starThreshold: Int = DEFAULT_STAR_THRESHOLD,
            emoji: String = DEFAULT_EMOJI,
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

object StarboardTable : CompositeIdTable("starboards") {
    val platform = varchar("platform", 255).entityId()
    val communityId = varchar("community_id", 255).entityId()
    val channelId = varchar("channel_id", 255).entityId()
    val starThreshold = integer("star_threshold").default(DEFAULT_STAR_THRESHOLD)
    val emoji = varchar("emoji", 255).default(DEFAULT_EMOJI)
    val selfStarAllowed = bool("self_star_allowed").default(false)

    override val primaryKey = PrimaryKey(platform, communityId, channelId)
}