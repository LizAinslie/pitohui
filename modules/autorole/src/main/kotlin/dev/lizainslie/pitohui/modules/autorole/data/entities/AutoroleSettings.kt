package dev.lizainslie.pitohui.modules.autorole.data.entities

import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.modules.autorole.data.tables.AutoroleSettingsTable
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class AutoroleSettings(id: EntityID<CompositeID>) : CompositeEntity(id) {
    val platform by AutoroleSettingsTable.platform
    val communityId by AutoroleSettingsTable.communityId
    var memberRoleId by AutoroleSettingsTable.memberRoleId
    var botRoleId by AutoroleSettingsTable.botRoleId

    companion object : CompositeEntityClass<AutoroleSettings>(AutoroleSettingsTable) {
        fun create(
            communityId: PlatformId,
            memberRole: String? = null,
            botRole: String? = null,
        ) = new(CompositeID.Companion { id ->
            id[AutoroleSettingsTable.platform] = communityId.platform.key
            id[AutoroleSettingsTable.communityId] = communityId.id
        }) {
            memberRoleId = memberRole
            botRoleId = botRole
        }

        fun getAutoroleSettings(communityId: PlatformId) = find {
            (AutoroleSettingsTable.platform eq communityId.platform.key) and
                    (AutoroleSettingsTable.communityId eq communityId.id)
        }.firstOrNull()
    }
}