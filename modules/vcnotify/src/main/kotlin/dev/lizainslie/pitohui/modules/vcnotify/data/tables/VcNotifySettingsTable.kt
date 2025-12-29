package dev.lizainslie.pitohui.modules.vcnotify.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.duration
import kotlin.time.Duration.Companion.minutes

object VcNotifySettingsTable : CompositeIdTable("vc_notify_guild_settings") {
    val platform = varchar("platform", 32).entityId()
    val communityId = varchar("community_id", 255).entityId()

    val messageFormat = varchar("message_format", 255).default("{role} {user} is now in {channelLink}! Join them!")
    val cooldown = duration("cooldown").default(30.minutes)

    val roleId = varchar("role_id", 255).nullable().default(null)

    override val primaryKey = PrimaryKey(platform, communityId)
}