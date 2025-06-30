package dev.lizainslie.pitohui.modules.vcnotify.data

import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.Platforms
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
    var platformId by VcNotifySettingsTable.platformId
    var platform by VcNotifySettingsTable.platform
    var roleId by VcNotifySettingsTable.roleId
    var messageFormat by VcNotifySettingsTable.messageFormat
    var cooldown by VcNotifySettingsTable.cooldown

    companion object : CompositeEntityClass<VcNotifySettings>(VcNotifySettingsTable) {
        fun getSettings(
            platformId: PlatformId,
        ) : VcNotifySettings? {
            return find {
                (VcNotifySettingsTable.platformId eq platformId.id) and
                (VcNotifySettingsTable.platform eq platformId.platform)
            }.firstOrNull()
        }

        fun create(
            platformId: PlatformId,
            messageFormat: String = "{role} {user} is now in {channelLink}! Join them!",
            cooldown: Duration = 30.minutes,
            roleId: ULong? = null,
        ): VcNotifySettings {
            return new(CompositeID { id ->
                id[VcNotifySettingsTable.platformId] = platformId.id
                id[VcNotifySettingsTable.platform] = platformId.platform
            }) {
                this.messageFormat = messageFormat
                this.cooldown = cooldown
                this.roleId = roleId
            }
        }
    }
}

object VcNotifySettingsTable : CompositeIdTable("vc_notify_guild_settings") {
    val platform = enumerationByName<Platforms>("platform", 32).entityId()
    val platformId = varchar("platform_id", 255).entityId()

    val messageFormat = varchar("message_format", 255).default("{role} {user} is now in {channelLink}! Join them!")
    val cooldown = duration("cooldown").default(30.minutes)

    // todo: this is pretty discord-specific, consider making it more generic or
    //  use a separate platform-specific hierarchical table
    val roleId = ulong("role_id").nullable().default(null)

    override val primaryKey = PrimaryKey(platform, platformId)
}