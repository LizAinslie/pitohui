package dev.lizainslie.pitohui.modules.vcnotify.data

import dev.lizainslie.pitohui.core.platforms.PlatformId
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.kotlin.datetime.duration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class VcNotifySettings(id: EntityID<CompositeID>) : CompositeEntity(id) {
    var communityId by VcNotifySettingsTable.communityId
    var platform by VcNotifySettingsTable.platform
    var roleId by VcNotifySettingsTable.roleId
    var messageFormat by VcNotifySettingsTable.messageFormat
    var cooldown by VcNotifySettingsTable.cooldown

    companion object : CompositeEntityClass<VcNotifySettings>(VcNotifySettingsTable) {
        fun getSettings(
            communityId: PlatformId,
        ): VcNotifySettings? {
            return find {
                (VcNotifySettingsTable.communityId eq communityId.id) and
                        (VcNotifySettingsTable.platform eq communityId.platform.key)
            }.firstOrNull()
        }

        fun create(
            communityId: PlatformId,
            messageFormat: String = "{role} {user} is now in {channelLink}! Join them!",
            cooldown: Duration = 30.minutes,
            roleId: PlatformId? = null,
        ): VcNotifySettings {
            return new(CompositeID { id ->
                id[VcNotifySettingsTable.communityId] = communityId.id
                id[VcNotifySettingsTable.platform] = communityId.platform.key
            }) {
                this.messageFormat = messageFormat
                this.cooldown = cooldown
                this.roleId = roleId?.id
            }
        }
    }
}

object VcNotifySettingsTable : CompositeIdTable("vc_notify_guild_settings") {
    val platform = varchar("platform", 32).entityId()
    val communityId = varchar("community_id", 255).entityId()

    val messageFormat = varchar("message_format", 255).default("{role} {user} is now in {channelLink}! Join them!")
    val cooldown = duration("cooldown").default(30.minutes)

    val roleId = varchar("role_id", 255).nullable().default(null)

    override val primaryKey = PrimaryKey(platform, communityId)
}
