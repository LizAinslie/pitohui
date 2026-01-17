package dev.lizainslie.pitohui.modules.vcnotify.data.entities

import dev.lizainslie.moeka.core.platforms.PlatformId
import dev.lizainslie.pitohui.modules.vcnotify.data.tables.VcNotifySettingsTable
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class VcNotifySettings(
    id: EntityID<CompositeID>,
) : CompositeEntity(id) {
    var communityId by VcNotifySettingsTable.communityId
    var platform by VcNotifySettingsTable.platform
    var roleId by VcNotifySettingsTable.roleId
    var messageFormat by VcNotifySettingsTable.messageFormat
    var cooldown by VcNotifySettingsTable.cooldown

    companion object : CompositeEntityClass<VcNotifySettings>(VcNotifySettingsTable) {
        fun getSettings(communityId: PlatformId): VcNotifySettings? =
            find {
                (VcNotifySettingsTable.communityId eq communityId.id) and
                    (VcNotifySettingsTable.platform eq communityId.platform.key)
            }.firstOrNull()

        fun create(
            communityId: PlatformId,
            messageFormat: String = "{role} {user} is now in {channelLink}! Join them!",
            cooldown: Duration = 30.minutes,
            roleId: PlatformId? = null,
        ): VcNotifySettings =
            new(
                CompositeID.Companion { id ->
                    id[VcNotifySettingsTable.communityId] = communityId.id
                    id[VcNotifySettingsTable.platform] = communityId.platform.key
                },
            ) {
                this.messageFormat = messageFormat
                this.cooldown = cooldown
                this.roleId = roleId?.id
            }
    }
}
