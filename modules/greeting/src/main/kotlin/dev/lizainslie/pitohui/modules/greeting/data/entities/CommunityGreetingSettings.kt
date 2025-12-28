package dev.lizainslie.pitohui.modules.greeting.data.entities

import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.modules.greeting.data.tables.CommunityGreetingSettingsTable
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class CommunityGreetingSettings(id: EntityID<CompositeID>) : CompositeEntity(id) {
    val platform by CommunityGreetingSettingsTable.platform
    val communityId by CommunityGreetingSettingsTable.communityId

    var welcomeChannelId by CommunityGreetingSettingsTable.welcomeChannelId
    var goodbyeChannelId by CommunityGreetingSettingsTable.goodbyeChannelId

    var welcomeMessage by CommunityGreetingSettingsTable.welcomeMessage
    var goodbyeMessage by CommunityGreetingSettingsTable.goodbyeMessage

    var welcomeColor by CommunityGreetingSettingsTable.welcomeColor
    var goodbyeColor by CommunityGreetingSettingsTable.goodbyeColor

    var welcomeImageUrl by CommunityGreetingSettingsTable.welcomeImageUrl
    var goodbyeImageUrl by CommunityGreetingSettingsTable.goodbyeImageUrl

    var embedWelcome by CommunityGreetingSettingsTable.embedWelcome
    var embedGoodbye by CommunityGreetingSettingsTable.embedGoodbye

    companion object : CompositeEntityClass<CommunityGreetingSettings>(CommunityGreetingSettingsTable) {
        fun create(
            communityId: PlatformId,
            welcomeChannelId: PlatformId? = null,
            goodbyeChannelId: PlatformId? = null,

            welcomeMessage: String? = null,
            goodbyeMessage: String? = null,

            welcomeColor: String? = null,
            goodbyeColor: String? = null,

            welcomeImageUrl: String? = null,
            goodbyeImageUrl: String? = null,

            embedWelcome: Boolean = false,
            embedGoodbye: Boolean = false
        ) = new(CompositeID { id ->
            id[CommunityGreetingSettingsTable.platform] = communityId.platform.key
            id[CommunityGreetingSettingsTable.communityId] = communityId.id
        }) {
            this.welcomeChannelId = welcomeChannelId?.id
            this.goodbyeChannelId = goodbyeChannelId?.id

            this.welcomeMessage = welcomeMessage
            this.goodbyeMessage = goodbyeMessage

            this.welcomeColor = welcomeColor
            this.goodbyeColor = goodbyeColor

            this.welcomeImageUrl = welcomeImageUrl
            this.goodbyeImageUrl = goodbyeImageUrl

            this.embedWelcome = embedWelcome
            this.embedGoodbye = embedGoodbye
        }

        fun findByCommunityId(
            communityId: PlatformId
        ) = find {
            (CommunityGreetingSettingsTable.platform eq communityId.platform.key) and
            (CommunityGreetingSettingsTable.communityId eq communityId.id)
        }.firstOrNull()
    }
}