package dev.lizainslie.pitohui.modules.autorole.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable

object AutoroleSettingsTable : CompositeIdTable("autorole_settings") {
    val platform = varchar("platform", 255).entityId()
    val communityId = varchar("community_id", 255).entityId()
    val memberRoleId = varchar("member_role_id", 255).nullable()
    val botRoleId = varchar("bot_role_id", 255).nullable()

    override val primaryKey = PrimaryKey(platform, communityId)
}
