package dev.lizainslie.pitohui.modules.starboard.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable

object StarboardTable : CompositeIdTable("starboards") {
    const val DEFAULT_EMOJI = "star"
    const val DEFAULT_STAR_THRESHOLD = 5

    val platform = varchar("platform", 255).entityId()
    val communityId = varchar("community_id", 255).entityId()
    val channelId = varchar("channel_id", 255).entityId()
    val starThreshold = integer("star_threshold").default(DEFAULT_STAR_THRESHOLD)
    val emoji = varchar("emoji", 255).default(DEFAULT_EMOJI)
    val selfStarAllowed = bool("self_star_allowed").default(false)

    override val primaryKey = PrimaryKey(platform, communityId, channelId)
}
